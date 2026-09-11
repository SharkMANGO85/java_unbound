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

public class AnimationControllerDefinition {
    private static final Map<String, JsonElement> AnimationControllerFiles = new HashMap<>();

    private static final Path BaseAnimationControllerFolder = Folder.GetConfigFolder().resolve("animation_controllers");
    private static final Path SubpackAnimationControllerFolder0 = Folder.GetConfigFolder().resolve("subpacks").resolve("SP0").resolve("animation_controllers");
    private static final Path SubpackAnimationControllerFolder1 = Folder.GetConfigFolder().resolve("subpacks").resolve("SP1").resolve("animation_controllers");
    private static final Path SubpackAnimationControllerFolder2 = Folder.GetConfigFolder().resolve("subpacks").resolve("SP2").resolve("animation_controllers");

    private static final Path[] AnimationControllerPriorityOrder = {SubpackAnimationControllerFolder2, BaseAnimationControllerFolder, SubpackAnimationControllerFolder1, SubpackAnimationControllerFolder0};

    private static void AddJsonToFiles(Path AnimationControllerPath) {
        try {
            String JsonContent = Files.readString(AnimationControllerPath);
            JsonElement Root = JsonParser.parseString(JsonContent);

            if (!Root.isJsonObject()) {
                JavaUnbound.LOGGER.error("AnimationController file root is not a JSON object: " + AnimationControllerPath);
                return;
            }

            JsonObject RootObject = Root.getAsJsonObject();

            if (!RootObject.has("animation_controllers") || !RootObject.get("animation_controllers").isJsonObject()) {
                JavaUnbound.LOGGER.error("AnimationController file has no animation_controllers object: " + AnimationControllerPath);
                return;
            }

            JsonObject AnimationControllers = RootObject.getAsJsonObject("animation_controllers");

            for (Map.Entry<String, JsonElement> Entry : AnimationControllers.entrySet()) {
                String Identifier = Entry.getKey();
                JsonElement AnimationController = Entry.getValue();

                if (!AnimationController.isJsonObject()) {continue;}

                AnimationControllerFiles.putIfAbsent(Identifier, AnimationController);
            }

        } catch (IOException | RuntimeException Exception) {
            JavaUnbound.LOGGER.error("Failed to load AnimationController file: " + AnimationControllerPath, Exception);
        }
    }

    public static void LoadAnimationControllers() throws IOException {
        AnimationControllerFiles.clear();

        for (Path AnimationControllerFolder : AnimationControllerPriorityOrder) {
            if (!Files.isDirectory(AnimationControllerFolder)) {continue;}

            try (Stream<Path> Paths = Files.walk(AnimationControllerFolder)) {

                Paths.filter(Files::isRegularFile).filter(Path -> Path.getFileName().toString().toLowerCase().endsWith(".json")).forEach(AnimationControllerDefinition::AddJsonToFiles);
            }
        }
    }

    public static String GetJavaUnboundPath(Path AnimationControllerPath) {
        Path JavaUnboundFolder = Folder.GetConfigFolder();

        return JavaUnboundFolder.relativize(AnimationControllerPath).toString().replace('\\', '/');
    }

    public static Path GetAnimationControllerByName(String AnimationControllerName) {
        JsonElement AnimationController = AnimationControllerFiles.get(AnimationControllerName);

        if (AnimationController == null) {return null;}

        return Path.of(AnimationControllerName);
    }

    public static Path GetAnimationControllerByOrevillePath(String AnimationControllerName) {
        int LastSlash = AnimationControllerName.lastIndexOf('/');

        if (LastSlash != -1) {
            AnimationControllerName = AnimationControllerName.substring(LastSlash + 1);
        }

        JsonElement AnimationController = AnimationControllerFiles.get(AnimationControllerName);

        if (AnimationController == null) {return null;}

        return Path.of(AnimationControllerName);
    }

    public static Path GetAnimationControllerByPath(String AnimationControllerPath) {
        return Path.of(AnimationControllerPath);
    }
}