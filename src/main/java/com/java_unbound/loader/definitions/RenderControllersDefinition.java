package com.java_unbound.loader.definitions;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.java_unbound.JavaUnbound;
import com.java_unbound.global.HashMaps;
import com.java_unbound.loader.resourcepack.Folder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.stream.Stream;

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

                if (!RenderController.isJsonObject()) {continue;}

                HashMaps.RenderControllerFiles.putIfAbsent(Identifier, RenderController);
            }

        } catch (IOException | RuntimeException Exception) {
            JavaUnbound.LOGGER.error("Failed to load RenderController file: " + RenderControllerPath, Exception);
        }
    }

    public static void LoadRenderControllers() throws IOException {
        HashMaps.RenderControllerFiles.clear();

        for (Path RenderControllerFolder : RenderControllerPriorityOrder) {
            if (!Files.isDirectory(RenderControllerFolder)) {continue;}

            try (Stream<Path> Paths = Files.walk(RenderControllerFolder)) {
                Paths.filter(Files::isRegularFile).filter(Path -> Path.getFileName().toString().toLowerCase().endsWith(".json")).forEach(RenderControllersDefinition::AddJsonToFiles);
            }
        }
    }
}