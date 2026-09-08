package com.java_unbound.loader.entity;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.java_unbound.JavaUnbound;
import com.java_unbound.loader.resourcepack.Folder;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class EntityTextureResolver {
    private EntityTextureResolver() {

    }

    private static final Path ResourceFolder = Folder.GetResourceFolder();
    private static final Path SubpackFolder = ResourceFolder.resolve("subpacks").resolve(JavaUnbound.SUBPACK);

    public static Path ResolveEntityTexture(JsonElement Textures) {
        if (Textures == null || !Textures.isJsonObject()) {
            JavaUnbound.LOGGER.error("Invalid entity texture data: {}", Textures);
            return null;
        }

        JsonObject TexturesObject = Textures.getAsJsonObject();

        for (Map.Entry<String, JsonElement> Entry : TexturesObject.entrySet()) {
            String Key = Entry.getKey();
            JsonElement Value = Entry.getValue();

            if (!Value.isJsonPrimitive()) {
                continue;
            }

            String TexturePath = Value.getAsString();
            Path Texture = ResolveTexture(TexturePath);

            if (Texture != null) {
                return Texture;
            }

            //JavaUnbound.LOGGER.error("Entity Texture not found: {}", TexturePath);
        }

        return null;
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

        Path Texture = SubpackFolder.resolve(TexturePath);

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