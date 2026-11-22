
package com.java_unbound.shared.utilities;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.java_unbound.JavaUnbound;

import java.nio.file.Files;
import java.nio.file.Path;

public class JsonUtilities {
    private static JsonObject ReadObject(JsonElement Element) {
        return Element != null && Element.isJsonObject() ? Element.getAsJsonObject() : null;
    }

    private static String ReadString(JsonElement Element) {
        if (Element == null || !Element.isJsonPrimitive() || !Element.getAsJsonPrimitive().isString()) {return null;}

        String Value = Element.getAsString().trim();

        return Value.isEmpty() ? null : Value;
    }

    private static JsonArray ReadArray(JsonElement Element) {
        return Element != null && Element.isJsonArray() ? Element.getAsJsonArray() : null;
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

    private static JsonElement ReadPath(JsonObject Parent, String JsonPath) {
        if (Parent == null || JsonPath == null || JsonPath.isBlank()) {return null;}

        JsonElement Current = Parent;

        for (String Key : JsonPath.split("\\.")) {
            if (!Current.isJsonObject()) {return null;}

            Current = Current.getAsJsonObject().get(Key);

            if (Current == null) {return null;}
        }

        return Current;
    }

    public static JsonElement GetElement(String Type, Path JsonPath) {
        if (JsonPath == null) {
            JavaUnbound.LOGGER.error("Cannot load JSON element: JSON file path is null.");
            return null;
        }

        if (!Files.isRegularFile(JsonPath)) {
            JavaUnbound.LOGGER.error("JSON file does not exist: {}", JsonPath);
            return null;
        }

        JavaUnbound.LOGGER.debug("Loading element from: {}", JsonPath);

        JsonObject ElementObject = ReadJson(JsonPath);
        if (ElementObject == null) {return null;}

        JsonElement Value = ReadPath(ElementObject, Type);

        if (Value == null) {
            JavaUnbound.LOGGER.debug("Missing element '{}' in: {}", Type, JsonPath);
        }

        return Value;
    }

    public static JsonObject GetObject(String Type, Path JsonPath) {
        if (JsonPath == null) {
            JavaUnbound.LOGGER.error("Cannot load JSON object: JSON file path is null.");
            return null;
        }

        if (!Files.isRegularFile(JsonPath)) {
            JavaUnbound.LOGGER.error("JSON file does not exist: {}", JsonPath);
            return null;
        }

        JavaUnbound.LOGGER.debug("Loading object from: {}", JsonPath);

        JsonObject Object = ReadJson(JsonPath);
        if (Object == null) {return null;}

        JsonObject Value = ReadObject(ReadPath(Object, Type));

        if (Value == null) {
            JavaUnbound.LOGGER.debug("Invalid or missing object '{}' in: {}", Type, JsonPath);
        }

        return Value;
    }

    public static JsonArray GetArray(String Type, Path JsonPath) {
        if (JsonPath == null) {
            JavaUnbound.LOGGER.error("Cannot load JSON array: JSON file path is null.");
            return null;
        }

        if (!Files.isRegularFile(JsonPath)) {
            JavaUnbound.LOGGER.error("JSON file does not exist: {}", JsonPath);
            return null;
        }

        JavaUnbound.LOGGER.debug("Loading array from: {}", JsonPath);

        JsonObject ArrayObject = ReadJson(JsonPath);
        if (ArrayObject == null) {return null;}

        JsonArray Value = ReadArray(ReadPath(ArrayObject, Type));

        if (Value == null) {
            JavaUnbound.LOGGER.debug("Invalid or missing array '{}' in: {}", Type, JsonPath);
        }

        return Value;
    }

    public static String GetString(String Type, Path JsonPath) {
        if (JsonPath == null) {
            JavaUnbound.LOGGER.error("Cannot load JSON string: JSON file path is null.");
            return null;
        }

        if (!Files.isRegularFile(JsonPath)) {
            JavaUnbound.LOGGER.error("JSON file does not exist: {}", JsonPath);
            return null;
        }

        JavaUnbound.LOGGER.debug("Loading string from: {}", JsonPath);

        JsonObject StringObject = ReadJson(JsonPath);
        if (StringObject == null) {return null;}

        String Value = ReadString(ReadPath(StringObject, Type));

        if (Value == null) {
            JavaUnbound.LOGGER.debug("Invalid or missing string '{}' in: {}", Type, JsonPath);
        }

        return Value;
    }
}