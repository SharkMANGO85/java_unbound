package com.java_unbound.loader.definitions.attachable.resolvers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.java_unbound.JavaUnbound;

import java.nio.file.Files;
import java.nio.file.Path;

public class AttachableStringResolver {
    private static JsonObject ReadObject(JsonObject Parent, String Key) {
        JsonElement Element = Parent.get(Key);

        return Element != null && Element.isJsonObject() ? Element.getAsJsonObject() : null;
    }

    private static String ReadString(JsonElement Element) {
        if (Element == null || !Element.isJsonPrimitive() || !Element.getAsJsonPrimitive().isString()) {return null;}

        String Value = Element.getAsString().trim();

        return Value.isEmpty() ? null : Value;
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

    private static JsonObject GetStringObject(Path JsonFile) {
        JsonObject Root = ReadJson(JsonFile);
        if (Root == null) {return null;}

        JsonObject ClientAttachable = ReadObject(Root, "minecraft:client_Attachable");

        if (ClientAttachable == null) {
            JavaUnbound.LOGGER.error("No minecraft:client_Attachable found: {}", JsonFile);
            return null;
        }

        JsonObject Description = ReadObject(ClientAttachable, "description");

        if (Description == null) {
            JavaUnbound.LOGGER.error("No Attachable description found: {}", JsonFile);
            return null;
        }

        return Description;
    }

    public static String GetAttachableString(String Type, Path JsonFile) {
        if (JsonFile == null) {
            JavaUnbound.LOGGER.error("Cannot load Attachable string: JSON file path is null.");
            return null;
        }

        if (!Files.isRegularFile(JsonFile)) {
            JavaUnbound.LOGGER.error("Attachable JSON file does not exist: {}", JsonFile);
            return null;
        }

        JavaUnbound.LOGGER.debug("Loading Attachable string from: {}", JsonFile);

        JsonObject StringObject = GetStringObject(JsonFile);
        if (StringObject == null) {return null;}

        String Value = ReadString(StringObject.get(Type));

        if (Value == null) {
            JavaUnbound.LOGGER.debug("Invalid or missing Attachable string '{}' in: {}", Type, JsonFile);
        }

        return Value;
    }
}