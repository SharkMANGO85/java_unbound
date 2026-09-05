package com.java_unbound.loader.entity;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.java_unbound.JavaUnbound;

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

            if (Value.getAsString().startsWith("textures/entity/")) {
                //Search in Subpack
            } else if (Value.getAsString().startsWith("textures/oreville/ans/")) {
                //Search in textures Folder
            }
        }

        return null;
    }
}
