package com.java_unbound.loader.entities;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.java_unbound.JavaUnbound;
import com.java_unbound.loader.definitions.EntitiesDefinition;
import com.java_unbound.loader.definitions.TexturesDefinition;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class EntityResolver {
    public static HashMap<String, Path> GetTextures(String Identifier) {
        JsonElement EntityFile = EntitiesDefinition.GetEntityJsonByIdentifier(Identifier);

        if (EntityFile == null) {
            JavaUnbound.LOGGER.error("No Entity file found for " + Identifier);
            return new HashMap<>();
        }

        if (!EntityFile.isJsonObject()) {
            JavaUnbound.LOGGER.error("Entity file is not a JSON object: " + Identifier);
            return new HashMap<>();
        }

        JsonObject EntityObject = EntityFile.getAsJsonObject();
        JsonElement DescriptionElement = EntityObject.get("description");

        if (DescriptionElement == null || !DescriptionElement.isJsonObject()) {
            JavaUnbound.LOGGER.error("Entity has no description object: " + Identifier);
            return new HashMap<>();
        }

        JsonObject Description = DescriptionElement.getAsJsonObject();
        JsonElement TexturesElement = Description.get("textures");

        if (TexturesElement == null || !TexturesElement.isJsonObject()) {
            JavaUnbound.LOGGER.error("Entity has no textures object: " + Identifier);
            return new HashMap<>();
        }

        JsonObject Textures = TexturesElement.getAsJsonObject();
        HashMap<String, Path> ResolvedTextures = new HashMap<>();

        for (Map.Entry<String, JsonElement> Entry : Textures.entrySet()) {
            String Key = Entry.getKey();
            String TexturePath = Entry.getValue().getAsString();
            Path Texture = TexturesDefinition.GetTextureByOrevillePath(TexturePath);

            if (Texture == null) {
                JavaUnbound.LOGGER.error("Could not resolve texture '{}' for entity '{}'", TexturePath, Identifier);
                continue;
            }

            ResolvedTextures.put(Key, Texture);
        }

        return ResolvedTextures;
    }
}