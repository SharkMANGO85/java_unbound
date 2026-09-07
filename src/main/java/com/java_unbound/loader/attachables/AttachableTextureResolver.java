package com.java_unbound.loader.attachables;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.java_unbound.JavaUnbound;
import com.java_unbound.loader.resourcepack.Folder;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class AttachableTextureResolver {
    private AttachableTextureResolver() {

    }

    private static final Path ResourceFolder = Folder.GetResourceFolder();

    private static final Path Subpack0Folder = ResourceFolder.resolve("subpacks").resolve("SP0");
    private static final Path Subpack1Folder = ResourceFolder.resolve("subpacks").resolve("SP1");
    private static final Path Subpack2Folder = ResourceFolder.resolve("subpacks").resolve("SP2");

    public static JsonElement ResolveAttachableTexture(JsonElement Textures) {
        if (Textures == null || !Textures.isJsonObject()) {
            JavaUnbound.LOGGER.error("Invalid attachable texture data: {}", Textures);
            return new JsonObject();
        }

        JsonObject TexturesObject = Textures.getAsJsonObject();
        JsonObject ResolvedTextures = new JsonObject();

        for (Map.Entry<String, JsonElement> Entry : TexturesObject.entrySet()) {
            String Key = Entry.getKey();
            JsonElement Value = Entry.getValue();

            if (!Value.isJsonPrimitive()) {
                continue;
            }

            String TexturePath = Value.getAsString();
            Path Texture = ResolveTexture(TexturePath);

            if (Texture != null) {
                ResolvedTextures.addProperty(Key, ResourceFolder.relativize(Texture).toString().replace('\\', '/'));
            } else {
                JavaUnbound.LOGGER.error("Attachable Texture not found: {}", TexturePath);
            }
        }

        return ResolvedTextures;
    }

    private static Path ResolveTexture(String TexturePath) {
        if (TexturePath == null || TexturePath.isEmpty()) {
            return null;
        }

        if (!TexturePath.startsWith("textures/")) {
            return null;
        }

        if (!TexturePath.endsWith(".png")) {
            TexturePath += ".png";
        }

        Path Texture = Subpack2Folder.resolve(TexturePath);

        if (Files.isRegularFile(Texture)) {
            return Texture;
        }

        Texture = Subpack1Folder.resolve(TexturePath);

        if (Files.isRegularFile(Texture)) {
            return Texture;
        }

        Texture = Subpack0Folder.resolve(TexturePath);

        if (Files.isRegularFile(Texture)) {
            return Texture;
        }

        Texture = ResourceFolder.resolve(TexturePath);

        if (Files.isRegularFile(Texture)) {
            return Texture;
        }

        return null;
    }
}