package com.java_unbound.loader.definitions.global.materials.resolvers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.java_unbound.loader.definitions.global.materials.rendering.BedrockMaterial;
import com.java_unbound.loader.definitions.global.materials.rendering.BedrockRenderMaterial;
import com.java_unbound.loader.resourcepack.Folder;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.AddressMode;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.logging.LogUtils;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.OutputTarget;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Stream;

public final class MaterialResolver {
    private static final Logger Log = LogUtils.getLogger();

    private static final Map<String, JsonObject> Raw = new LinkedHashMap<>();
    private static final Map<String, String> ParentOf = new HashMap<>();
    private static final Map<String, ResolvedMaterial> Resolved = new LinkedHashMap<>();
    private static final Set<String> Resolving = new HashSet<>();
    private static final Map<String, ResolvedMaterial> Inferred = new HashMap<>();
    private static boolean Loaded = false;

    private MaterialResolver() {}

    public static synchronized void Load() {
        Raw.clear();
        ParentOf.clear();
        Resolved.clear();
        Resolving.clear();
        Inferred.clear();
        Loaded = true;

        Path Root = Folder.GetConfigFolder().resolve("materials");
        if (!Files.isDirectory(Root)) {
            Log.warn("[MaterialResolver] Ordner {} nicht gefunden", Root);
            return;
        }

        List<Path> Found;
        try (Stream<Path> Walk = Files.walk(Root)) {
            Found = Walk.filter(Files::isRegularFile)
                    .filter(P -> {
                        String FileName = P.getFileName().toString().toLowerCase(Locale.ROOT);
                        return FileName.endsWith(".json") || FileName.endsWith(".material");
                    })
                    .sorted()
                    .toList();
        } catch (IOException Ex) {
            Log.error("[MaterialResolver] Fehler beim Durchsuchen von {}", Root, Ex);
            return;
        }

        for (Path File : Found) {LoadFile(File);}
        for (String Name : new ArrayList<>(Raw.keySet())) {Resolve(Name);}
        Log.info("[MaterialResolver] {} Dateien, {} Materialien aufgelöst", Found.size(), Resolved.size());
    }

    private static void LoadFile(Path File) {
        try (BufferedReader Reader = Files.newBufferedReader(File)) {
            JsonObject Root = JsonParser.parseReader(Reader).getAsJsonObject();
            if (!Root.has("materials") || !Root.get("materials").isJsonObject()) {return;}

            for (Map.Entry<String, JsonElement> Entry : Root.getAsJsonObject("materials").entrySet()) {
                if (!Entry.getValue().isJsonObject()) {continue;}

                String Key = Entry.getKey();
                int Colon = Key.indexOf(':');
                String Name = Colon < 0 ? Key : Key.substring(0, Colon);

                if (Raw.containsKey(Name)) {
                    Log.warn("[MaterialResolver] {} wird durch {} überschrieben", Name, File);
                }

                if (Colon >= 0) {ParentOf.put(Name, Key.substring(Colon + 1));} else {ParentOf.remove(Name);}
                Raw.put(Name, Entry.getValue().getAsJsonObject());
            }
        } catch (IOException | RuntimeException Ex) {
            Log.error("[MaterialResolver] Fehler beim Lesen von {}", File, Ex);
        }
    }

    private static ResolvedMaterial Infer(String Name) {
        ResolvedMaterial Cached = Inferred.get(Name);
        if (Cached != null) {return Cached;}

        ResolvedMaterial Material = new ResolvedMaterial(Name);
        String Lower = Name.toLowerCase(Locale.ROOT);

        if (Lower.contains("alphatest")) {Material.Defines.add("ALPHA_TEST");}
        if (Lower.contains("emissive")) {Material.Defines.add("USE_EMISSIVE");}
        if (Lower.contains("color_mask") || Lower.contains("change_color")) {Material.Defines.add("USE_COLOR_MASK");}
        if (Lower.contains("alphablend") || (Lower.contains("multiplicative") && Lower.contains("blend"))) {
            Material.States.add("Blending");
            Material.BlendSrc = "SourceAlpha";
            Material.BlendDst = "OneMinusSrcAlpha";
        }

        boolean KeepsCulling = Lower.contains("cull");
        boolean NoCull = Lower.contains("nocull") || Lower.contains("alphatest") || Lower.contains("alphablend") || Lower.contains("emissive");
        if (NoCull && !KeepsCulling) {Material.States.add("DisableCulling");}

        Log.warn("[MaterialResolver] Parent {} ist in keiner Datei definiert, aus dem Namen abgeleitet: Defines={} States={}", Name, Material.Defines, Material.States);
        Inferred.put(Name, Material);
        return Material;
    }

    private static synchronized void EnsureLoaded() {
        if (!Loaded) {Load();}
    }

    public static ResolvedMaterial Get(String Name) {
        if (Name == null) {return null;}
        EnsureLoaded();
        return Resolved.get(Name);
    }

    public static Collection<String> Names() {
        EnsureLoaded();
        return Collections.unmodifiableSet(Resolved.keySet());
    }

    private static ResolvedMaterial Resolve(String Name) {
        ResolvedMaterial Done = Resolved.get(Name);
        if (Done != null) {return Done;}

        JsonObject Json = Raw.get(Name);
        if (Json == null) {return Infer(Name);}

        if (!Resolving.add(Name)) {
            Log.warn("[MaterialResolver] Zyklus in der Vererbung bei {}", Name);
            return null;
        }

        try {
            String Parent = ParentOf.get(Name);
            ResolvedMaterial Base = Parent == null ? null : Resolve(Parent);
            if (Parent != null && Base == null) {
                Log.warn("[MaterialResolver] {}: unbekannter Parent {}", Name, Parent);
            }

            ResolvedMaterial Material = Base != null ? Base.Copy(Name) : new ResolvedMaterial(Name);
            Apply(Json, Material);
            Resolved.put(Name, Material);

            JsonElement Variants = Json.get("variants");
            if (Variants != null && Variants.isJsonArray()) {
                for (JsonElement VariantElement : Variants.getAsJsonArray()) {
                    if (!VariantElement.isJsonObject()) {continue;}
                    for (Map.Entry<String, JsonElement> Variant : VariantElement.getAsJsonObject().entrySet()) {
                        if (!Variant.getValue().isJsonObject()) {continue;}
                        String VariantName = Name + "." + Variant.getKey();
                        ResolvedMaterial VariantMaterial = Material.Copy(VariantName);
                        Apply(Variant.getValue().getAsJsonObject(), VariantMaterial);
                        Resolved.put(VariantName, VariantMaterial);
                    }
                }
            }

            return Material;
        } finally {
            Resolving.remove(Name);
        }
    }

    private static void Apply(JsonObject Json, ResolvedMaterial Material) {
        ForEachString(Json, "-defines", Material.Defines::remove);
        ForEachString(Json, "-states", Material.States::remove);
        ForEachString(Json, "+defines", Material.Defines::add);
        ForEachString(Json, "+states", Material.States::add);

        if (Json.has("depthFunc")) {Material.DepthFunc = Json.get("depthFunc").getAsString();}
        if (Json.has("blendSrc")) {Material.BlendSrc = Json.get("blendSrc").getAsString();}
        if (Json.has("blendDst")) {Material.BlendDst = Json.get("blendDst").getAsString();}
        if (Json.has("stencilRef")) {Material.StencilRef = Json.get("stencilRef").getAsInt();}
        if (Json.has("stencilReadMask")) {Material.StencilReadMask = Json.get("stencilReadMask").getAsInt();}

        MergeFace(Json, "frontFace", Material.Front);
        MergeFace(Json, "backFace", Material.Back);

        JsonElement Samplers = Json.get("+samplerStates");
        if (Samplers != null && Samplers.isJsonArray()) {
            for (JsonElement SamplerElement : Samplers.getAsJsonArray()) {
                if (!SamplerElement.isJsonObject()) {continue;}
                JsonObject SamplerJson = SamplerElement.getAsJsonObject();
                if (!SamplerJson.has("samplerIndex")) {continue;}

                SamplerDef Sampler = Material.Samplers.computeIfAbsent(SamplerJson.get("samplerIndex").getAsInt(), Index -> new SamplerDef());
                if (SamplerJson.has("textureFilter")) {Sampler.Filter = SamplerJson.get("textureFilter").getAsString();}
                if (SamplerJson.has("textureWrap")) {Sampler.Wrap = SamplerJson.get("textureWrap").getAsString();}
            }
        }
    }

    private static void ForEachString(JsonObject Json, String Key, Consumer<String> Action) {
        JsonElement Element = Json.get(Key);
        if (Element == null || !Element.isJsonArray()) {return;}
        for (JsonElement Entry : Element.getAsJsonArray()) {Action.accept(Entry.getAsString());}
    }

    private static void MergeFace(JsonObject Json, String Key, Map<String, String> Target) {
        JsonElement Element = Json.get(Key);
        if (Element == null || !Element.isJsonObject()) {return;}
        for (Map.Entry<String, JsonElement> Entry : Element.getAsJsonObject().entrySet()) {
            Target.put(Entry.getKey(), Entry.getValue().getAsString());
        }
    }

    public static final class SamplerDef {
        public String Filter;
        public String Wrap;

        SamplerDef Copy() {
            SamplerDef Copy = new SamplerDef();
            Copy.Filter = Filter;
            Copy.Wrap = Wrap;
            return Copy;
        }
    }

    public static final class ResolvedMaterial implements BedrockRenderMaterial {
        public final String Name;
        public final Set<String> Defines = new LinkedHashSet<>();
        public final Set<String> States = new LinkedHashSet<>();
        public final Map<Integer, SamplerDef> Samplers = new TreeMap<>();
        public final Map<String, String> Front = new HashMap<>();
        public final Map<String, String> Back = new HashMap<>();
        public String DepthFunc;
        public String BlendSrc;
        public String BlendDst;
        public Integer StencilRef;
        public Integer StencilReadMask;

        private final Map<Identifier, RenderType> Cache = new ConcurrentHashMap<>();

        ResolvedMaterial(String Name) {
            this.Name = Name;
        }

        ResolvedMaterial Copy(String NewName) {
            ResolvedMaterial Copy = new ResolvedMaterial(NewName);
            Copy.Defines.addAll(Defines);
            Copy.States.addAll(States);
            Samplers.forEach((Index, Sampler) -> Copy.Samplers.put(Index, Sampler.Copy()));
            Copy.Front.putAll(Front);
            Copy.Back.putAll(Back);
            Copy.DepthFunc = DepthFunc;
            Copy.BlendSrc = BlendSrc;
            Copy.BlendDst = BlendDst;
            Copy.StencilRef = StencilRef;
            Copy.StencilReadMask = StencilReadMask;
            return Copy;
        }

        public boolean HasDefine(String Define) {return Defines.contains(Define);}
        public boolean HasState(String State) {return States.contains(State);}

        public boolean IsCulled() {return !HasState("DisableCulling");}
        public boolean IsBlending() {return HasState("Blending");}
        public boolean IsAlphaTested() {return HasDefine("ALPHA_TEST");}
        public boolean IsEmissive() {return HasDefine("USE_EMISSIVE") || HasDefine("USE_ONLY_EMISSIVE");}
        public boolean IsAdditive() {return IsBlending() && "One".equals(BlendDst);}
        public boolean IsStencilMask() {return HasState("DisableColorWrite");}

        public boolean IsFullBright() {return IsEmissive() || IsAdditive();}

        public BedrockMaterial ToBedrockMaterial() {
            if (IsAdditive()) {return BedrockMaterial.EYES;}
            if (IsBlending()) {return IsCulled() ? BedrockMaterial.TRANSLUCENT_CULL : BedrockMaterial.TRANSLUCENT;}
            if (IsEmissive()) {return BedrockMaterial.EMISSIVE_OPAQUE_CULL;}
            if (IsAlphaTested()) {return IsCulled() ? BedrockMaterial.CUTOUT_CULL : BedrockMaterial.CUTOUT;}
            return BedrockMaterial.SOLID;
        }

        public RenderType GetRenderType(Identifier Texture) {
            return Cache.computeIfAbsent(Texture, this::BuildRenderType);
        }

        private RenderType BuildRenderType(Identifier Texture) {
            RenderPipeline Pipeline = SelectPipeline();
            boolean Eyes = Pipeline == RenderPipelines.EYES;
            boolean NoLightmap = Eyes || Pipeline == RenderPipelines.ENTITY_TRANSLUCENT_EMISSIVE;
            boolean Sorted = IsBlending() || Eyes;

            RenderSetup.RenderSetupBuilder Builder = RenderSetup.builder(Pipeline);

            SamplerDef Sampler = Samplers.get(0);
            if (Sampler == null) {
                Builder.withTexture("Sampler0", Texture);
            } else {
                AddressMode Address = "Repeat".equalsIgnoreCase(Sampler.Wrap) ? AddressMode.REPEAT : AddressMode.CLAMP_TO_EDGE;
                FilterMode Filter = "Bilinear".equalsIgnoreCase(Sampler.Filter) ? FilterMode.LINEAR : FilterMode.NEAREST;
                Builder.withTexture("Sampler0", Texture, () -> RenderSystem.getSamplerCache().getSampler(Address, Address, Filter, Filter, false));
            }

            if (!NoLightmap) {Builder.useLightmap();}
            if (!Eyes) {
                Builder.useOverlay();
                Builder.affectsCrumbling();
                Builder.setOutline(RenderSetup.OutlineProperty.AFFECTS_OUTLINE);
            }
            if (Sorted) {Builder.sortOnUpload();}
            if (IsBlending() && !Eyes) {Builder.setOutputTarget(OutputTarget.ITEM_ENTITY_TARGET);}

            return RenderType.create("bedrock/" + Name, Builder.createRenderSetup());
        }

        private RenderPipeline SelectPipeline() {
            if (IsAdditive()) {return RenderPipelines.EYES;}
            if (IsBlending()) {
                if (IsEmissive()) {return RenderPipelines.ENTITY_TRANSLUCENT_EMISSIVE;}
                return IsCulled() ? RenderPipelines.ENTITY_TRANSLUCENT_CULL : RenderPipelines.ENTITY_TRANSLUCENT;
            }
            if (IsAlphaTested()) {return IsCulled() ? RenderPipelines.ENTITY_CUTOUT_CULL : RenderPipelines.ENTITY_CUTOUT;}
            return RenderPipelines.ENTITY_SOLID;
        }
    }
}