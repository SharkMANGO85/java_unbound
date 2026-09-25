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

public class AnimationsDefinition {
    private static final Path BaseAnimationFolder = Folder.GetConfigFolder().resolve("animations");

    private static final Path SubpackAnimationFolder0 = Folder.GetConfigFolder().resolve("subpacks").resolve("SP0").resolve("animations");
    private static final Path SubpackAnimationFolder1 = Folder.GetConfigFolder().resolve("subpacks").resolve("SP1").resolve("animations");
    private static final Path SubpackAnimationFolder2 = Folder.GetConfigFolder().resolve("subpacks").resolve("SP2").resolve("animations");

    private static final Path[] AnimationPriorityOrder = {SubpackAnimationFolder2, BaseAnimationFolder, SubpackAnimationFolder1, SubpackAnimationFolder0};

    private static void AddJsonToFiles(Path AnimationPath) {
        try {
            String JsonContent = Files.readString(AnimationPath);
            JsonElement Root = JsonParser.parseString(JsonContent);

            if (!Root.isJsonObject()) {
                JavaUnbound.LOGGER.error("Animation file root is not a JSON object: " + AnimationPath);
                return;
            }

            JsonObject RootObject = Root.getAsJsonObject();
            JsonElement AnimationsElement = RootObject.get("animations");

            if (AnimationsElement == null || !AnimationsElement.isJsonObject()) {
                JavaUnbound.LOGGER.error("Animation file has no animation object: " + AnimationPath);
                return;
            }

            JsonObject Animations = AnimationsElement.getAsJsonObject();

            for (Map.Entry<String, JsonElement> Entry : Animations.entrySet()) {
                String Identifier = Entry.getKey();
                JsonElement Animation = Entry.getValue();

                if (!Animation.isJsonObject()) {continue;}

                HashMaps.AnimationFiles.putIfAbsent(Identifier, Animation);
            }

        } catch (IOException | RuntimeException Exception) {
            JavaUnbound.LOGGER.error("Failed to load Animation file: " + AnimationPath, Exception);
        }
    }

    public static void LoadAnimations() throws IOException {
        HashMaps.AnimationFiles.clear();

        for (Path AnimationFolder : AnimationPriorityOrder) {
            if (!Files.isDirectory(AnimationFolder)) {continue;}

            try (Stream<Path> Paths = Files.walk(AnimationFolder)) {
                Paths.filter(Files::isRegularFile).filter(Path -> Path.getFileName().toString().toLowerCase().endsWith(".json")).forEach(AnimationsDefinition::AddJsonToFiles);
            }
        }
    }
}