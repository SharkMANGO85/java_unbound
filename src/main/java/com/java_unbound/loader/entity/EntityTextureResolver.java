package com.java_unbound.loader.entity;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.java_unbound.JavaUnbound;
import com.java_unbound.loader.resourcepack.Folder;

import java.nio.file.Path;
import java.util.Map;

public class EntityTextureResolver {
    private EntityTextureResolver() {

    }

    public static Path ResolveEntityTexture(JsonElement Textures) {
        JsonObject TexturesObject = Textures.getAsJsonObject();

        for (Map.Entry<String, JsonElement> Entry : TexturesObject.entrySet()) {
            String Key = Entry.getKey();
            JsonElement Value = Entry.getValue();

            Path Texture = null;

            if (Value.getAsString().startsWith("textures/entity/")) {
                Texture = Folder.GetResourceFolder().resolve("subpacks").resolve(JavaUnbound.SUBPACK).resolve(Value.getAsString() + ".png");
            } else if (Value.getAsString().startsWith("textures/blocks/")) {
                Texture = Folder.GetResourceFolder().resolve("subpacks").resolve(JavaUnbound.SUBPACK).resolve(Value.getAsString() + ".png");
            } else if (Value.getAsString().startsWith("textures/oreville/ans/")) {
                Texture = Folder.GetResourceFolder().resolve(Value.getAsString());
            }

            if (Texture == null) {
                JavaUnbound.LOGGER.error("Texture not found: " +  Value);
            }
        }

        return null;
    }
}
