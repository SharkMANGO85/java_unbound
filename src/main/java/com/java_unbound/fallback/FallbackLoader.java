package com.java_unbound.fallback;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.java_unbound.JavaUnbound;
import com.java_unbound.shared.definitions.textures.TextureDefinition;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BooleanSupplier;
import java.util.stream.Stream;

public class FallbackLoader {
    private static final String ResourceRoot = "assets/resource_pack_fallback";
    private static final int DefaultTextureSize = 64;

    private static final String ModelsFolder = "models";
    private static final String RenderControllersFolder = "render_controllers";

    private static final Map<String, List<String>> RemoteFolders = Map.of(ModelsFolder, List.of("models", "models/entity"), RenderControllersFolder, List.of("render_controllers"));

    private record RawGeometry(String Name, String Parent, JsonObject Data) {}

    private static final Map<String, List<String>> FolderFiles = new HashMap<>();
    private static final Set<String> ScannedFiles = new HashSet<>();

    private static final Map<String, RawGeometry> RawGeometries = new HashMap<>();
    private static final Map<String, JsonObject> GeometryCache = new HashMap<>();
    private static final Map<String, JsonObject> RenderControllerCache = new HashMap<>();
    private static final Map<String, Path> TextureSources = new HashMap<>();
    private static final Map<String, TextureDefinition> TextureCache = new ConcurrentHashMap<>();

    private static final Object LoadLock = new Object();
    private static volatile boolean Loaded;

    private static final ExecutorService Executor = Executors.newSingleThreadExecutor(Runnable -> {
        Thread Thread = new Thread(Runnable, "JavaUnbound-FallbackPreload");

        Thread.setDaemon(true);

        return Thread;
    });

    private static CompletableFuture<Void> PreloadFuture;

    public static synchronized CompletableFuture<Void> PreloadFallbackAsync() {
        if (PreloadFuture == null) {
            PreloadFuture = CompletableFuture.runAsync(FallbackLoader::PreloadFallback, Executor);
        }

        return PreloadFuture;
    }

    public static boolean IsReady() {
        return Loaded;
    }

    public static void PreloadFallback() {
        if (Loaded) return;

        synchronized (LoadLock) {
            if (Loaded) return;

            try {
                FolderFiles.put(ModelsFolder, ListFiles(ModelsFolder));
                FolderFiles.put(RenderControllersFolder, ListFiles(RenderControllersFolder));
                IndexTextures();

                JavaUnbound.LOGGER.info("Indexed fallback resources: {} model files, {} render controller files, {} textures", FolderFiles.get(ModelsFolder).size(), FolderFiles.get(RenderControllersFolder).size(), TextureSources.size());
            } catch (Throwable Throwable) {
                JavaUnbound.LOGGER.error("Fallback preload failed", Throwable);
            } finally {
                Loaded = true;
            }
        }
    }

    public static JsonObject GetFallbackGeometry(String Identifier) {
        String Name = StripParent(Identifier);

        synchronized (LoadLock) {
            PreloadFallback();

            JsonObject Cached = GeometryCache.get(Name);
            if (Cached != null) return Cached;

            JsonObject Resolved = ResolveLegacy(Name, new HashSet<>());
            if (Resolved == null) return null;

            Cached = GeometryCache.get(Name);
            if (Cached != null) return Cached;

            JsonObject Modern = ToModern(Name, Resolved);

            GeometryCache.put(Name, Modern);

            return Modern;
        }
    }

    public static JsonObject GetFallbackRenderController(String Identifier) {
        synchronized (LoadLock) {
            PreloadFallback();

            ScanUntilFound(RenderControllersFolder, Identifier, () -> RenderControllerCache.containsKey(Identifier), FallbackLoader::IndexRenderControllerFile);

            return RenderControllerCache.get(Identifier);
        }
    }

    public static TextureDefinition GetFallbackTexture(String TexturePath) {
        PreloadFallback();

        String Key = NormalizeTexturePath(TexturePath);

        TextureDefinition Cached = TextureCache.get(Key);
        if (Cached != null) return Cached;

        String AbsolutePath;

        Path Source = TextureSources.get(Key);

        if (Source != null) {
            Path Target = GetCacheFolder().resolve(Key + ".png");

            try {
                Files.createDirectories(Target.getParent());

                try (InputStream Stream = Files.newInputStream(Source)) {
                    Files.copy(Stream, Target, StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (IOException Exception) {
                JavaUnbound.LOGGER.error("Failed to extract fallback texture: {}", Key, Exception);
                return null;
            }

            AbsolutePath = Target.toAbsolutePath().toString();
        } else {
            Path Remote = RemoteFallbackSource.GetFile(Key + ".png");

            if (Remote == null) {
                return null;
            }

            AbsolutePath = Remote.toAbsolutePath().toString();
        }

        TextureDefinition Definition = new TextureDefinition();

        Definition.TexturePath = Key;
        Definition.AbsolutePath = AbsolutePath;

        TextureDefinition Previous = TextureCache.putIfAbsent(Key, Definition);

        return Previous != null ? Previous : Definition;
    }

    public static void ClearCache() {
        synchronized (LoadLock) {
            FolderFiles.clear();
            ScannedFiles.clear();
            RawGeometries.clear();
            GeometryCache.clear();
            RenderControllerCache.clear();
            TextureSources.clear();
            TextureCache.clear();
            RemoteFallbackSource.ClearMissing();

            Loaded = false;
        }

        synchronized (FallbackLoader.class) {
            PreloadFuture = null;
        }
    }

    private static void ScanUntilFound(String Folder, String Identifier, BooleanSupplier Found, JsonObjectConsumer Consumer) {
        if (Found.getAsBoolean()) return;

        List<String> Candidates = new ArrayList<>(FolderFiles.getOrDefault(Folder, List.of()));
        String Token = RelevanceToken(Identifier);

        Candidates.sort(Comparator.comparingInt(Candidate -> Score(Candidate, Token)));

        for (String Candidate : Candidates) {
            if (!ScannedFiles.add(Candidate)) continue;

            JsonObject Root = FallbackUtilities.GetFallbackJsonObject(Candidate);

            if (Root == null) {
                JavaUnbound.LOGGER.debug("Could not load fallback JSON: {}", Candidate);
                continue;
            }

            Consumer.Accept(Root);

            if (Found.getAsBoolean()) return;
        }
    }

    private static String RelevanceToken(String Identifier) {
        String Token = StripParent(Identifier).toLowerCase();

        if (Token.startsWith("geometry.")) Token = Token.substring("geometry.".length());
        else if (Token.startsWith("controller.render.")) Token = Token.substring("controller.render.".length());

        int Dot = Token.indexOf('.');

        return Dot >= 0 ? Token.substring(0, Dot) : Token;
    }

    private static int Score(String File, String Token) {
        String Name = File.substring(File.lastIndexOf('/') + 1).toLowerCase();
        int Dot = Name.indexOf('.');
        String Base = Dot >= 0 ? Name.substring(0, Dot) : Name;

        if (Base.equals(Token)) return 0;
        if (!Base.isEmpty() && (Token.startsWith(Base) || Base.startsWith(Token))) return 1;

        return 2;
    }

    private static List<String> ListFiles(String Folder) {
        List<String> Result = new ArrayList<>();
        Set<String> Seen = new HashSet<>();

        Path Bundled = FindResource(Folder);

        if (Bundled != null) {
            try (Stream<Path> Paths = Files.walk(Bundled)) {
                Paths.filter(Files::isRegularFile)
                        .filter(Entry -> Entry.toString().endsWith(".json"))
                        .sorted()
                        .forEach(Entry -> {
                            String Relative = Folder + "/" + Bundled.relativize(Entry).toString().replace('\\', '/');

                            if (Seen.add(Relative)) Result.add(Relative);
                        });
            } catch (IOException Exception) {
                JavaUnbound.LOGGER.error("Failed to list bundled fallback folder: {}", Folder, Exception);
            }
        }

        List<String> Remote = RemoteFolders.get(Folder);

        if (Remote != null) {
            for (String RemoteFolder : Remote) {
                for (String Name : RemoteFallbackSource.ListFolder(RemoteFolder)) {
                    String Relative = RemoteFolder + "/" + Name;

                    if (Seen.add(Relative)) Result.add(Relative);
                }
            }
        }

        return Result;
    }

    private static void IndexGeometryFile(JsonObject Root) {
        for (Map.Entry<String, JsonElement> Entry : Root.entrySet()) {
            String Key = Entry.getKey();
            if (Key.equals("format_version")) continue;

            if (Key.equals("minecraft:geometry") && Entry.getValue().isJsonArray()) {
                for (JsonElement Element : Entry.getValue().getAsJsonArray()) {
                    if (!Element.isJsonObject()) continue;

                    JsonObject Geometry = Element.getAsJsonObject();
                    JsonElement Description = Geometry.get("description");

                    if (Description == null || !Description.isJsonObject()) continue;

                    JsonElement Identifier = Description.getAsJsonObject().get("identifier");
                    if (Identifier == null || !Identifier.isJsonPrimitive()) continue;

                    GeometryCache.putIfAbsent(StripParent(Identifier.getAsString()), Geometry);
                }
                continue;
            }

            if (Key.startsWith("geometry.") && Entry.getValue().isJsonObject()) {
                String Name = StripParent(Key);
                int Colon = Key.indexOf(':');
                String Parent = Colon >= 0 ? Key.substring(Colon + 1).trim() : null;

                RawGeometries.putIfAbsent(Name, new RawGeometry(Name, Parent, Entry.getValue().getAsJsonObject()));
            }
        }
    }

    private static boolean EnsureGeometryKnown(String Name) {
        ScanUntilFound(ModelsFolder, Name, () -> GeometryCache.containsKey(Name) || RawGeometries.containsKey(Name), FallbackLoader::IndexGeometryFile);

        return GeometryCache.containsKey(Name) || RawGeometries.containsKey(Name);
    }

    private static JsonObject ResolveLegacy(String Name, Set<String> Visiting) {
        if (!EnsureGeometryKnown(Name)) return null;

        JsonObject Modern = GeometryCache.get(Name);
        if (Modern != null) return ModernToLegacy(Modern);

        RawGeometry Raw = RawGeometries.get(Name);
        if (Raw == null) return null;

        if (!Visiting.add(Name)) {
            JavaUnbound.LOGGER.warn("Circular geometry inheritance at {}", Name);
            return null;
        }

        JsonObject ParentResolved = null;

        if (Raw.Parent() != null) {
            ParentResolved = ResolveLegacy(Raw.Parent(), Visiting);

            if (ParentResolved == null) {
                JavaUnbound.LOGGER.debug("Parent geometry {} of {} not found in fallback, using child only", Raw.Parent(), Name);
            }
        }

        Visiting.remove(Name);

        JsonObject Result = new JsonObject();

        if (ParentResolved != null) {
            for (Map.Entry<String, JsonElement> Entry : ParentResolved.entrySet()) {
                if (!Entry.getKey().equals("bones")) {
                    Result.add(Entry.getKey(), Entry.getValue().deepCopy());
                }
            }
        }
        for (Map.Entry<String, JsonElement> Entry : Raw.Data().entrySet()) {
            if (!Entry.getKey().equals("bones")) {
                Result.add(Entry.getKey(), Entry.getValue().deepCopy());
            }
        }

        Map<String, JsonObject> Bones = new LinkedHashMap<>();

        if (ParentResolved != null && ParentResolved.has("bones")) {
            for (JsonElement Element : ParentResolved.getAsJsonArray("bones")) {
                JsonObject Bone = Element.getAsJsonObject();

                Bones.put(Bone.get("name").getAsString(), Bone.deepCopy());
            }
        }

        JsonElement ChildBones = Raw.Data().get("bones");

        if (ChildBones != null && ChildBones.isJsonArray()) {
            for (JsonElement Element : ChildBones.getAsJsonArray()) {
                if (!Element.isJsonObject()) continue;

                JsonObject ChildBone = Element.getAsJsonObject();
                JsonElement NameElement = ChildBone.get("name");
                if (NameElement == null) continue;

                String BoneName = NameElement.getAsString();
                JsonObject Existing = Bones.get(BoneName);

                boolean Reset = ChildBone.has("reset") && ChildBone.get("reset").getAsBoolean();

                if (Existing == null || Reset) {
                    JsonObject Fresh = ChildBone.deepCopy();

                    Fresh.remove("reset");
                    Bones.put(BoneName, Fresh);
                } else {
                    for (Map.Entry<String, JsonElement> Entry : ChildBone.entrySet()) {
                        Existing.add(Entry.getKey(), Entry.getValue().deepCopy());
                    }
                }
            }
        }

        JsonArray BoneArray = new JsonArray();

        Bones.values().forEach(BoneArray::add);
        Result.add("bones", BoneArray);

        return Result;
    }

    private static JsonObject ToModern(String Identifier, JsonObject Legacy) {
        JsonObject Description = new JsonObject();
        Description.addProperty("identifier", Identifier);

        Description.addProperty("texture_width", GetInt(Legacy, "texturewidth", DefaultTextureSize));
        Description.addProperty("texture_height", GetInt(Legacy, "textureheight", DefaultTextureSize));

        for (String Key : new String[] { "visible_bounds_width", "visible_bounds_height", "visible_bounds_offset" }) {
            if (Legacy.has(Key)) {
                Description.add(Key, Legacy.get(Key).deepCopy());
            }
        }

        JsonObject Modern = new JsonObject();

        Modern.add("description", Description);
        Modern.add("bones", Legacy.get("bones"));

        return Modern;
    }

    private static JsonObject ModernToLegacy(JsonObject Modern) {
        JsonObject Legacy = new JsonObject();
        JsonElement DescriptionElement = Modern.get("description");

        if (DescriptionElement != null && DescriptionElement.isJsonObject()) {
            JsonObject Description = DescriptionElement.getAsJsonObject();

            if (Description.has("texture_width")) Legacy.add("texturewidth", Description.get("texture_width").deepCopy());
            if (Description.has("texture_height")) Legacy.add("textureheight", Description.get("texture_height").deepCopy());

            for (String Key : new String[] { "visible_bounds_width", "visible_bounds_height", "visible_bounds_offset" }) {
                if (Description.has(Key)) Legacy.add(Key, Description.get(Key).deepCopy());
            }
        }

        JsonElement Bones = Modern.get("bones");
        Legacy.add("bones", Bones != null && Bones.isJsonArray() ? Bones.deepCopy() : new JsonArray());

        return Legacy;
    }

    private static void IndexRenderControllerFile(JsonObject Root) {
        JsonElement ControllersElement = Root.get("render_controllers");
        if (ControllersElement == null || !ControllersElement.isJsonObject()) return;

        for (Map.Entry<String, JsonElement> Entry : ControllersElement.getAsJsonObject().entrySet()) {
            if (!Entry.getValue().isJsonObject()) continue;

            RenderControllerCache.putIfAbsent(Entry.getKey(), Entry.getValue().getAsJsonObject());
        }
    }

    private static void IndexTextures() {
        Path TexturesRoot = FindResource("textures");
        if (TexturesRoot == null) return;

        try (Stream<Path> Paths = Files.walk(TexturesRoot)) {
            Paths.filter(Files::isRegularFile)
                    .filter(Path -> Path.getFileName().toString().toLowerCase().endsWith(".png"))
                    .forEach(Path -> {
                        String Relative = TexturesRoot.relativize(Path).toString().replace('\\', '/');

                        TextureSources.putIfAbsent(NormalizeTexturePath("textures/" + Relative), Path);
                    });
        } catch (IOException Exception) {
            JavaUnbound.LOGGER.error("Failed to scan fallback textures", Exception);
        }
    }

    private static String NormalizeTexturePath(String TexturePath) {
        String Normalized = TexturePath.replace('\\', '/');

        while (Normalized.startsWith("/")) Normalized = Normalized.substring(1);

        if (Normalized.toLowerCase().endsWith(".png")) {
            Normalized = Normalized.substring(0, Normalized.length() - 4);
        }

        return Normalized;
    }

    private static Path GetCacheFolder() {
        return FabricLoader.getInstance().getConfigDir().resolve("java_unbound").resolve("fallback_cache");
    }

    private static Path FindResource(String Folder) {
        Optional<Path> Found = FabricLoader.getInstance().getModContainer(JavaUnbound.MOD_ID).flatMap(Container -> Container.findPath(ResourceRoot + "/" + Folder));

        if (Found.isEmpty()) {
            JavaUnbound.LOGGER.debug("No bundled fallback folder: {}/{}", ResourceRoot, Folder);
            return null;
        }

        return Found.get();
    }

    private static String StripParent(String Identifier) {
        int Colon = Identifier.indexOf(':');

        return (Colon >= 0 ? Identifier.substring(0, Colon) : Identifier).trim();
    }

    private static int GetInt(JsonObject Object, String Key, int Default) {
        JsonElement Element = Object.get(Key);

        return Element != null && Element.isJsonPrimitive() ? Element.getAsInt() : Default;
    }

    @FunctionalInterface
    private interface JsonObjectConsumer {
        void Accept(JsonObject JsonObject);
    }
}