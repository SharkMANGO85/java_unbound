package com.java_unbound.loader.definitions;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.java_unbound.JavaUnbound;
import com.java_unbound.loader.resourcepack.Folder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class RenderControllersDefinition {
    private static final Map<String, JsonElement> RenderControllerFiles = new HashMap<>();

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

            if (!RootObject.has("render_controllers") || !RootObject.get("render_controllers").isJsonObject()) {
                JavaUnbound.LOGGER.error("RenderController file has no render_controllers object: " + RenderControllerPath);
                return;
            }

            JsonObject RenderControllers = RootObject.getAsJsonObject("render_controllers");

            for (Map.Entry<String, JsonElement> Entry : RenderControllers.entrySet()) {
                String Identifier = Entry.getKey();
                JsonElement RenderController = Entry.getValue();

                if (!RenderController.isJsonObject()) {continue;}

                RenderControllerFiles.putIfAbsent(Identifier, RenderController);
            }

        } catch (IOException | RuntimeException Exception) {
            JavaUnbound.LOGGER.error("Failed to load RenderController file: " + RenderControllerPath, Exception);
        }
    }

    public static void LoadRenderControllers() throws IOException {
        RenderControllerFiles.clear();

        for (Path RenderControllerFolder : RenderControllerPriorityOrder) {
            if (!Files.isDirectory(RenderControllerFolder)) {continue;}

            try (Stream<Path> Paths = Files.walk(RenderControllerFolder)) {

                Paths.filter(Files::isRegularFile).filter(Path -> Path.getFileName().toString().toLowerCase().endsWith(".json")).forEach(RenderControllersDefinition::AddJsonToFiles);
            }
        }
    }

    public static String GetJavaUnboundPath(Path RenderControllerPath) {
        Path JavaUnboundFolder = Folder.GetConfigFolder();

        return JavaUnboundFolder.relativize(RenderControllerPath).toString().replace('\\', '/');
    }

    public static Path GetRenderControllerByName(String RenderControllerName) {
        JsonElement RenderController = RenderControllerFiles.get(RenderControllerName);

        if (RenderController == null) {return null;}

        return Path.of(RenderControllerName);
    }

    public static Path GetRenderControllerByOrevillePath(String RenderControllerName) {
        int LastSlash = RenderControllerName.lastIndexOf('/');

        if (LastSlash != -1) {
            RenderControllerName = RenderControllerName.substring(LastSlash + 1);
        }

        JsonElement RenderController = RenderControllerFiles.get(RenderControllerName);

        if (RenderController == null) {return null;}

        return Path.of(RenderControllerName);
    }

    public static Path GetRenderControllerByPath(String RenderControllerPath) {
        return Path.of(RenderControllerPath);
    }
}