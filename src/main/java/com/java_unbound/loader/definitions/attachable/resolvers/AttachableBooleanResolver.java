package com.java_unbound.loader.definitions.attachable.resolvers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.java_unbound.JavaUnbound;

import java.nio.file.Files;
import java.nio.file.Path;

public class AttachableBooleanResolver {

    private static JsonObject ReadObject(JsonObject Parent, String Key) {
        JsonElement Element = Parent.get(Key);

        return Element != null && Element.isJsonObject()
                ? Element.getAsJsonObject()
                : null;
    }

    private static JsonObject ReadJson(Path JsonFile) {
        try {
            JsonElement Element = JsonParser.parseString(Files.readString(JsonFile));

            if (!Element.isJsonObject()) {
                JavaUnbound.LOGGER.debug("Skipping JSON file (root is not an object): {}", JsonFile);
                return null;
            }

            return Element.getAsJsonObject();

        } catch (Exception Exception) {
            JavaUnbound.LOGGER.error("Failed to read JSON file: {}", JsonFile, Exception);
            return null;
        }
    }

    private static JsonObject GetBooleanObject(Path JsonFile) {
        JsonObject Root = ReadJson(JsonFile);

        if (Root == null) {
            return null;
        }

        JsonObject ClientEntity = ReadObject(Root, "minecraft:client_entity");

        if (ClientEntity == null) {
            JavaUnbound.LOGGER.error("No minecraft:client_entity found: {}", JsonFile);
            return null;
        }

        JsonObject Description = ReadObject(ClientEntity, "description");

        if (Description == null) {
            JavaUnbound.LOGGER.error("No entity description found: {}", JsonFile);
            return null;
        }

        return Description;
    }

    public static boolean GetEntityBoolean(String Type, Path JsonFile) {
        if (JsonFile == null) {
            JavaUnbound.LOGGER.error("Cannot load entity boolean: JSON file path is null.");
            return true;
        }

        if (!Files.isRegularFile(JsonFile)) {
            JavaUnbound.LOGGER.error("Entity JSON file does not exist: {}", JsonFile);
            return true;
        }

        JavaUnbound.LOGGER.debug("Loading entity boolean '{}' from: {}", Type, JsonFile);

        JsonObject BooleanObject = GetBooleanObject(JsonFile);

        if (BooleanObject == null) {
            return true;
        }

        JsonElement Element = BooleanObject.get(Type);

        if (Element == null || !Element.isJsonPrimitive() || !Element.getAsJsonPrimitive().isBoolean()) {
            JavaUnbound.LOGGER.debug("Invalid or missing entity boolean '{}' in: {}", Type, JsonFile);
            return true;
        }

        return Element.getAsBoolean();
    }
}