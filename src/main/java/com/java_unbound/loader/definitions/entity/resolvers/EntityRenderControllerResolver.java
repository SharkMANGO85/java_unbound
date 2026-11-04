package com.java_unbound.loader.definitions.entity.resolvers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.java_unbound.JavaUnbound;
import com.java_unbound.loader.definitions.entity.types.render_controller.EntityRenderControllerArrays;
import com.java_unbound.loader.definitions.entity.types.render_controller.EntityRenderControllerColor;
import com.java_unbound.loader.definitions.entity.types.render_controller.EntityRenderControllerDefinition;
import com.java_unbound.loader.definitions.entity.types.render_controller.EntityRenderControllerMaterial;
import com.java_unbound.loader.definitions.entity.types.render_controller.EntityRenderControllerPartVisibility;
import com.java_unbound.loader.definitions.entity.types.render_controller.EntityRenderControllerReference;
import com.java_unbound.loader.definitions.entity.types.render_controller.EntityRenderControllerUvAnim;
import com.java_unbound.loader.resourcepack.Folder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class EntityRenderControllerResolver {
    private static HashMap<String, EntityRenderControllerDefinition> CachedAvailableRenderControllers;
    private static final HashMap<Path, List<EntityRenderControllerReference>> CachedEntityRenderControllers = new HashMap<>();

    public static List<Path> GetRenderControllerFolders() {
        Path ConfigFolder = Folder.GetConfigFolder();
        List<Path> RenderControllerFolders = new ArrayList<>();

        if (ConfigFolder.resolve("subpacks").resolve(JavaUnbound.SUB_PACK).resolve("render_controllers").toFile().exists()) {
            RenderControllerFolders.add(ConfigFolder.resolve("subpacks").resolve(JavaUnbound.SUB_PACK).resolve("render_controllers"));
        }

        if (ConfigFolder.resolve("render_controllers").toFile().exists()) {
            RenderControllerFolders.add(ConfigFolder.resolve("render_controllers"));
        }

        return RenderControllerFolders;
    }

    private static JsonObject ReadJson(Path JsonFile) {
        try {
            return JsonParser.parseString(Files.readString(JsonFile)).getAsJsonObject();
        } catch (Exception Exception) {
            JavaUnbound.LOGGER.error("Failed to read JSON file: {}", JsonFile, Exception);
            return null;
        }
    }

    private static JsonArray GetRenderControllerReferences(Path EntityFile) {
        JsonObject Root = ReadJson(EntityFile);
        if (Root == null) {return null;}

        JsonElement ClientEntityElement = Root.get("minecraft:client_entity");

        if (ClientEntityElement == null || !ClientEntityElement.isJsonObject()) {
            JavaUnbound.LOGGER.error("No minecraft:client_entity found: {}", EntityFile);
            return null;
        }

        JsonObject Description = ReadObject(ClientEntityElement.getAsJsonObject(), "description");

        if (Description == null) {
            JavaUnbound.LOGGER.error("No entity description found: {}", EntityFile);
            return null;
        }

        JsonElement ReferencesElement = Description.get("render_controllers");
        JsonArray ReferencesArray = null;

        if (ReferencesElement != null && ReferencesElement.isJsonArray()) {
            ReferencesArray = ReferencesElement.getAsJsonArray();
        } else if (ReferencesElement != null && ReferencesElement.isJsonPrimitive()) {
            ReferencesArray = new JsonArray();
            ReferencesArray.add(ReferencesElement);
        }

        if (ReferencesArray == null) {
            JavaUnbound.LOGGER.warn("No render_controllers found: {}", EntityFile);
            return null;
        }

        return ReferencesArray;
    }

    private static List<EntityRenderControllerReference> ParseReferences(JsonArray ReferencesArray) {
        List<EntityRenderControllerReference> References = new ArrayList<>();

        for (JsonElement Element : ReferencesArray) {
            if (Element.isJsonPrimitive()) {
                String Identifier = Element.getAsString();
                if (Identifier.isBlank()) {continue;}

                EntityRenderControllerReference Reference = new EntityRenderControllerReference();
                Reference.Identifier = Identifier;
                References.add(Reference);
                continue;
            }

            if (Element.isJsonObject()) {
                for (Map.Entry<String, JsonElement> Entry : Element.getAsJsonObject().entrySet()) {
                    if (Entry.getKey().isBlank()) {continue;}
                    if (!Entry.getValue().isJsonPrimitive()) {continue;}

                    EntityRenderControllerReference Reference = new EntityRenderControllerReference();
                    Reference.Identifier = Entry.getKey();
                    Reference.Condition = Entry.getValue().getAsString();

                    //JavaUnbound.LOGGER.error("Render controller: {} | Condition: {} | Type: {}", Reference.Identifier, Reference.Condition, MolangParser.Classify(Reference.Condition));

                    References.add(Reference);
                }
            }
        }

        return References;
    }

    private static EntityRenderControllerDefinition ParseRenderController(String Identifier, JsonObject Json) {
        EntityRenderControllerDefinition Definition = new EntityRenderControllerDefinition();

        Definition.Identifier = Identifier;

        JsonObject ArraysObject = ReadObject(Json, "arrays");

        if (ArraysObject != null) {
            Definition.Arrays = ParseArrays(ArraysObject);
        }

        Definition.Geometry = ReadMolang(Json.get("geometry"));
        Definition.Textures = ParseTextures(ReadArray(Json, "textures"));
        Definition.Materials = ParseMaterials(ReadArray(Json, "materials"));
        Definition.PartVisibility = ParsePartVisibility(ReadArray(Json, "part_visibility"));

        Definition.IgnoreLighting = ReadBoolean(Json, "ignore_lighting", Definition.IgnoreLighting);
        Definition.FilterLighting = ReadBoolean(Json, "filter_lighting", Definition.FilterLighting);
        Definition.RebuildAnimationMatrices = ReadBoolean(Json, "rebuild_animation_matrices", Definition.RebuildAnimationMatrices);

        Definition.LightColorMultiplier = ReadMolang(Json.get("light_color_multiplier"));

        Definition.Color = ParseColor(Json.get("color"));
        Definition.IsHurtColor = ParseColor(Json.get("is_hurt_color"));
        Definition.OnFireColor = ParseColor(Json.get("on_fire_color"));
        Definition.OverlayColor = ParseColor(Json.get("overlay_color"));

        Definition.UvAnim = ParseUvAnim(Json.get("uv_anim"));

        return Definition;
    }

    private static EntityRenderControllerArrays ParseArrays(JsonObject Json) {
        EntityRenderControllerArrays Arrays = new EntityRenderControllerArrays();

        Arrays.Geometries = ParseArraySet(ReadObject(Json, "geometries"));
        Arrays.Materials = ParseArraySet(ReadObject(Json, "materials"));
        Arrays.Textures = ParseArraySet(ReadObject(Json, "textures"));

        return Arrays;
    }

    private static HashMap<String, List<String>> ParseArraySet(JsonObject Json) {
        HashMap<String, List<String>> Result = new HashMap<>();
        if (Json == null) {return Result;}

        for (Map.Entry<String, JsonElement> Entry : Json.entrySet()) {
            List<String> Values = ReadMolangList(Entry.getValue());
            if (Values == null) {continue;}

            Result.put(Entry.getKey(), Values);
        }

        return Result;
    }

    private static List<String> ParseTextures(JsonArray Array) {
        List<String> Textures = new ArrayList<>();
        if (Array == null) {return Textures;}

        for (JsonElement Element : Array) {
            String Texture = ReadMolang(Element);
            if (Texture != null) {Textures.add(Texture);}
        }

        return Textures;
    }

    private static List<EntityRenderControllerMaterial> ParseMaterials(JsonArray Array) {
        List<EntityRenderControllerMaterial> Materials = new ArrayList<>();
        if (Array == null) {return Materials;}

        for (JsonElement Element : Array) {
            if (!Element.isJsonObject()) {continue;}

            for (Map.Entry<String, JsonElement> Entry : Element.getAsJsonObject().entrySet()) {
                String MaterialName = ReadMolang(Entry.getValue());
                if (MaterialName == null) {continue;}

                EntityRenderControllerMaterial Material = new EntityRenderControllerMaterial();
                Material.BonePattern = Entry.getKey();
                Material.Material = MaterialName;
                Materials.add(Material);
            }
        }

        return Materials;
    }

    private static List<EntityRenderControllerPartVisibility> ParsePartVisibility(JsonArray Array) {
        List<EntityRenderControllerPartVisibility> Parts = new ArrayList<>();
        if (Array == null) {return Parts;}

        for (JsonElement Element : Array) {
            if (!Element.isJsonObject()) {continue;}

            for (Map.Entry<String, JsonElement> Entry : Element.getAsJsonObject().entrySet()) {
                String Visibility = ReadMolang(Entry.getValue());
                if (Visibility == null) {continue;}

                EntityRenderControllerPartVisibility Part = new EntityRenderControllerPartVisibility();
                Part.BonePattern = Entry.getKey();
                Part.Visibility = Visibility;
                Parts.add(Part);
            }
        }

        return Parts;
    }

    private static EntityRenderControllerColor ParseColor(JsonElement Json) {
        if (Json == null || !Json.isJsonObject()) {return null;}

        JsonObject Object = Json.getAsJsonObject();
        EntityRenderControllerColor Color = new EntityRenderControllerColor();

        Color.R = ReadMolang(Object.get("r"));
        Color.G = ReadMolang(Object.get("g"));
        Color.B = ReadMolang(Object.get("b"));
        Color.A = ReadMolang(Object.get("a"));

        return Color;
    }

    private static EntityRenderControllerUvAnim ParseUvAnim(JsonElement Json) {
        if (Json == null || !Json.isJsonObject()) {return null;}

        JsonObject Object = Json.getAsJsonObject();
        EntityRenderControllerUvAnim UvAnim = new EntityRenderControllerUvAnim();

        List<String> Offset = ReadMolangList(Object.get("offset"));
        List<String> Scale = ReadMolangList(Object.get("scale"));

        UvAnim.Offset = Offset != null ? Offset.toArray(new String[0]) : null;
        UvAnim.Scale = Scale != null ? Scale.toArray(new String[0]) : null;

        return UvAnim;
    }

    private static JsonObject ReadObject(JsonObject Parent, String Key) {
        JsonElement Element = Parent.get(Key);
        return Element != null && Element.isJsonObject() ? Element.getAsJsonObject() : null;
    }

    private static JsonArray ReadArray(JsonObject Parent, String Key) {
        JsonElement Element = Parent.get(Key);
        return Element != null && Element.isJsonArray() ? Element.getAsJsonArray() : null;
    }

    private static boolean ReadBoolean(JsonObject Parent, String Key, boolean Default) {
        JsonElement Element = Parent.get(Key);
        if (Element == null || !Element.isJsonPrimitive()) {return Default;}

        JsonPrimitive Primitive = Element.getAsJsonPrimitive();
        if (Primitive.isBoolean()) {return Primitive.getAsBoolean();}
        if (Primitive.isString()) {
            String Text = Primitive.getAsString();
            if (Text.equalsIgnoreCase("true")) {return true;}
            if (Text.equalsIgnoreCase("false")) {return false;}
        }

        return Default;
    }

    private static String ReadMolang(JsonElement Element) {
        if (Element == null || !Element.isJsonPrimitive()) {return null;}
        return Element.getAsString();
    }

    private static List<String> ReadMolangList(JsonElement Element) {
        if (Element == null || !Element.isJsonArray()) {return null;}

        JsonArray Array = Element.getAsJsonArray();
        List<String> Values = new ArrayList<>(Array.size());

        for (JsonElement Entry : Array) {
            String Value = ReadMolang(Entry);
            if (Value == null) {return null;}
            Values.add(Value);
        }

        return Values;
    }

    private static EntityRenderControllerDefinition ParseRenderControllerSafe(String Identifier, JsonObject RenderControllerObject, Path RenderControllerFile) {
        try {
            return ParseRenderController(Identifier, RenderControllerObject);
        } catch (Exception Exception) {
            JavaUnbound.LOGGER.error("Failed to deserialize render controller: {} | File: {}", Identifier, RenderControllerFile, Exception);
            return null;
        }
    }

    private static HashMap<String, EntityRenderControllerDefinition> LoadRenderControllerFiles() {
        HashMap<String, EntityRenderControllerDefinition> RenderControllers = new HashMap<>();

        List<Path> RenderControllerFolders = GetRenderControllerFolders();

        if (RenderControllerFolders.isEmpty()) {
            JavaUnbound.LOGGER.error("No render controller folders exist.");
            return RenderControllers;
        }

        for (Path RenderControllerFolder : RenderControllerFolders) {
            LoadRenderControllerFolder(RenderControllerFolder, RenderControllers);
        }

        JavaUnbound.LOGGER.debug("Finished render controller search | Found: {}", RenderControllers.size());
        return RenderControllers;
    }

    private static void LoadRenderControllerFolder(Path RenderControllerFolder, HashMap<String, EntityRenderControllerDefinition> RenderControllers) {
        if (!Files.isDirectory(RenderControllerFolder)) {
            JavaUnbound.LOGGER.error("Render controller folder does not exist: {}", RenderControllerFolder);
            return;
        }

        JavaUnbound.LOGGER.debug("Searching render controller files in: {}", RenderControllerFolder);

        try (Stream<Path> FilesStream = Files.walk(RenderControllerFolder)) {
            FilesStream
                    .filter(Files::isRegularFile)
                    .filter(File -> File.toString().endsWith(".json"))
                    .forEach(RenderControllerFile -> {
                        JsonObject Root = ReadJson(RenderControllerFile);
                        if (Root == null) {return;}

                        JsonObject ControllersObject = ReadObject(Root, "render_controllers");
                        if (ControllersObject == null) {return;}

                        for (Map.Entry<String, JsonElement> Entry : ControllersObject.entrySet()) {
                            String Identifier = Entry.getKey();

                            if (Identifier == null || Identifier.isBlank()) {continue;}
                            if (!Entry.getValue().isJsonObject()) {continue;}

                            if (RenderControllers.containsKey(Identifier)) {
                                JavaUnbound.LOGGER.debug("Skipping render controller (already loaded from higher priority folder): {} | File: {}", Identifier, RenderControllerFile);
                                continue;
                            }

                            EntityRenderControllerDefinition Definition = ParseRenderControllerSafe(Identifier, Entry.getValue().getAsJsonObject(), RenderControllerFile);
                            if (Definition == null) {continue;}

                            RenderControllers.put(Identifier, Definition);

                            JavaUnbound.LOGGER.debug("Found render controller: {} | File: {} | Materials: {} | Textures: {}", Identifier, RenderControllerFile, Definition.Materials.size(), Definition.Textures.size());
                        }
                    });
        } catch (IOException Exception) {
            JavaUnbound.LOGGER.error("Failed to search render controller files: {}", RenderControllerFolder, Exception);
        }
    }

    private static synchronized HashMap<String, EntityRenderControllerDefinition> GetAvailableRenderControllers() {
        if (CachedAvailableRenderControllers == null) {
            JavaUnbound.LOGGER.debug("Loading render controller cache...");

            CachedAvailableRenderControllers = LoadRenderControllerFiles();

            JavaUnbound.LOGGER.debug("Render controller cache loaded | Render controllers: {}", CachedAvailableRenderControllers.size());
        }

        return CachedAvailableRenderControllers;
    }

    private static List<EntityRenderControllerReference> ResolveEntityRenderControllers(Path EntityFile) {
        Path CacheKey = EntityFile.toAbsolutePath().normalize();

        List<EntityRenderControllerReference> CachedRenderControllers = CachedEntityRenderControllers.get(CacheKey);

        if (CachedRenderControllers != null) {
            return CachedRenderControllers;
        }

        JavaUnbound.LOGGER.debug("Resolving entity render controllers from: {}", EntityFile);

        List<EntityRenderControllerReference> RenderControllers = new ArrayList<>();

        JsonArray ReferencesArray = GetRenderControllerReferences(EntityFile);

        if (ReferencesArray == null) {
            CachedEntityRenderControllers.put(CacheKey, RenderControllers);
            return RenderControllers;
        }

        List<EntityRenderControllerReference> References = ParseReferences(ReferencesArray);
        HashMap<String, EntityRenderControllerDefinition> AvailableRenderControllers = GetAvailableRenderControllers();

        for (EntityRenderControllerReference Reference : References) {
            EntityRenderControllerDefinition Definition = AvailableRenderControllers.get(Reference.Identifier);

            if (Definition == null) {
                JavaUnbound.LOGGER.warn("Could not resolve render controller: {} | Entity: {}", Reference.Identifier, EntityFile);
                continue;
            }

            Reference.Definition = Definition;
            RenderControllers.add(Reference);

            JavaUnbound.LOGGER.debug("Resolved render controller: {} | Condition: {}", Reference.Identifier, Reference.Condition);
        }

        JavaUnbound.LOGGER.debug("Finished resolving entity render controllers: {} | Resolved: {}/{}", EntityFile, RenderControllers.size(), References.size());
        CachedEntityRenderControllers.put(CacheKey, RenderControllers);

        return RenderControllers;
    }

    public static synchronized List<EntityRenderControllerReference> GetEntityRenderControllers(Path EntityFile) {
        return new ArrayList<>(ResolveEntityRenderControllers(EntityFile));
    }

    public static synchronized EntityRenderControllerReference GetEntityRenderController(Path EntityFile, String Identifier) {
        if (Identifier == null || Identifier.isBlank()) {
            JavaUnbound.LOGGER.error("Cannot resolve render controller: identifier is null or blank.");
            return null;
        }

        for (EntityRenderControllerReference Reference : ResolveEntityRenderControllers(EntityFile)) {
            if (Identifier.equals(Reference.Identifier)) {return Reference;}
        }

        JavaUnbound.LOGGER.debug("Render controller not found: {} | Entity: {}", Identifier, EntityFile);
        return null;
    }

    public static synchronized void ClearCache() {
        CachedAvailableRenderControllers = null;
        CachedEntityRenderControllers.clear();

        JavaUnbound.LOGGER.debug("Cleared entity render controller cache.");
    }
}