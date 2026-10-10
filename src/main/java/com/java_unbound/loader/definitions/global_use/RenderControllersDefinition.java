package com.java_unbound.loader.definitions.global_use;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.java_unbound.JavaUnbound;
import com.java_unbound.entities.render_controller.EntityRenderControllerRegistry;
import com.java_unbound.global.HashMaps;
import com.java_unbound.loader.resourcepack.Folder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

//uploads the render controller as a json object to the global hashmap
//render controllers decide which texutes, geometries, etc. should be used
public class RenderControllersDefinition {
    private static final Path BaseRenderControllerFolder = Folder.GetConfigFolder().resolve("render_controllers");

    private static final Path SubpackRenderControllerFolder0 = Folder.GetConfigFolder().resolve("subpacks").resolve("SP0").resolve("render_controllers");
    private static final Path SubpackRenderControllerFolder1 = Folder.GetConfigFolder().resolve("subpacks").resolve("SP1").resolve("render_controllers");
    private static final Path SubpackRenderControllerFolder2 = Folder.GetConfigFolder().resolve("subpacks").resolve("SP2").resolve("render_controllers");

    private static final Path[] RenderControllerPriorityOrder = {SubpackRenderControllerFolder2, BaseRenderControllerFolder, SubpackRenderControllerFolder1, SubpackRenderControllerFolder0};

    private static void AddJsonToFiles(Path RenderControllerPath) {
        try {
            String JsonContent = Files.readString(RenderControllerPath);
            JsonElement Root = JsonParser.parseString(JsonContent);

            if (!Root.isJsonObject()) {
                JavaUnbound.LOGGER.error("RenderController file root is not a JSON object: " + RenderControllerPath);
                return;
            }

            JsonObject RootObject = Root.getAsJsonObject();
            JsonElement RenderControllersElement = RootObject.get("render_controllers");

            if (RenderControllersElement == null || !RenderControllersElement.isJsonObject()) {
                JavaUnbound.LOGGER.error("RenderController file has no render_controllers object: " + RenderControllerPath);
                return;
            }

            JsonObject RenderControllers = RenderControllersElement.getAsJsonObject();

            for (Map.Entry<String, JsonElement> Entry : RenderControllers.entrySet()) {
                String Identifier = Entry.getKey();
                JsonElement RenderController = Entry.getValue();

                if (!RenderController.isJsonObject()) {
                    continue;
                }

                if (HashMaps.RenderControllerFiles.putIfAbsent(Identifier, RenderController) == null) {
                    EntityRenderControllerRegistry Registry = new EntityRenderControllerRegistry();
                    Registry.LoadEntityRenderController(Identifier);
                    HashMaps.EntityRenderControllers.put(Identifier, Registry);
                }
            }
        } catch (IOException | RuntimeException Exception) {
            JavaUnbound.LOGGER.error("Failed to load RenderController file: " + RenderControllerPath, Exception);
        }
    }

    public static void LoadRenderControllers() throws IOException {
        HashMaps.RenderControllerFiles.clear();
        HashMaps.EntityRenderControllers.clear();

        for (Path RenderControllerFolder : RenderControllerPriorityOrder) {
            if (!Files.isDirectory(RenderControllerFolder)) {
                continue;
            }

            try (Stream<Path> Paths = Files.walk(RenderControllerFolder)) {
                Paths.filter(Files::isRegularFile).filter(Path -> Path.getFileName().toString().toLowerCase().endsWith(".json")).forEach(RenderControllersDefinition::AddJsonToFiles);
            }
        }
    }

    public static JsonElement GetRenderControllerByIdentifier(String Identifier) {
        if (Identifier == null || Identifier.isEmpty()) {return null;}

        return HashMaps.RenderControllerFiles.get(Identifier);
    }

    public static List<JsonElement> GetRenderControllersByEntityJsonFile(JsonElement EntityJsonFile) {
        List<JsonElement> RenderControllers = new ArrayList<>();

        if (EntityJsonFile == null) {
            return RenderControllers;
        }

        if (!EntityJsonFile.isJsonObject()) {
            return RenderControllers;
        }

        JsonObject EntityObject = EntityJsonFile.getAsJsonObject();
        JsonElement DescriptionElement = EntityObject.get("description");

        if (DescriptionElement == null || !DescriptionElement.isJsonObject()) {
            return RenderControllers;
        }

        JsonObject Description = DescriptionElement.getAsJsonObject();
        JsonElement RenderControllersElement = Description.get("render_controllers");

        if (RenderControllersElement == null || !RenderControllersElement.isJsonArray()) {
            return RenderControllers;
        }

        for (JsonElement RenderControllerElement : RenderControllersElement.getAsJsonArray()) {
            RenderControllers.add(RenderControllerElement);
        }

        return RenderControllers;
    }

    public static JsonArray GetRenderControllersByEntityId(String Identifier) {
        JsonElement EntityJson = HashMaps.EntityFiles.get(Identifier);

        if (EntityJson == null || !EntityJson.isJsonObject()) {
            JavaUnbound.LOGGER.error("RENDER CONTROLLER: Could not find entity '{}'", Identifier);
            return new JsonArray();
        }

        JsonElement DescriptionElement = EntityJson.getAsJsonObject().get("description");

        if (DescriptionElement == null || !DescriptionElement.isJsonObject()) {
            JavaUnbound.LOGGER.error("RENDER CONTROLLER: Entity has no description: " + Identifier);
            return new JsonArray();
        }

        JsonElement RenderControllersElement = DescriptionElement.getAsJsonObject().get("render_controllers");

        if (RenderControllersElement == null || !RenderControllersElement.isJsonArray()) {
            JavaUnbound.LOGGER.error("RENDER CONTROLLER: Entity has no render controllers array: " + Identifier);
            return new JsonArray();
        }

        return RenderControllersElement.getAsJsonArray();
    }
}