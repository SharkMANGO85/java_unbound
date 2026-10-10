package com.java_unbound.entities.render_controller;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.java_unbound.JavaUnbound;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//these functions return the resolved data
public class EntityRenderControllerResolver {
    public static Map<String, Map<String, List<String>>> GetArrays(String Identifier, JsonObject EntityRenderControllerObject) {
        Map<String, Map<String, List<String>>> Arrays = new HashMap<>();

        if (!EntityRenderControllerObject.has("arrays")) {
            return Arrays;
        }

        JsonElement ArraysElement = EntityRenderControllerObject.get("arrays");

        if (!ArraysElement.isJsonObject()) {
            JavaUnbound.LOGGER.error("ENTITY RENDER CONTROLLER: 'arrays' is not a JSON object for '{}'", Identifier);
            return Arrays;
        }

        JsonObject ArraysObject = ArraysElement.getAsJsonObject();

        for (Map.Entry<String, JsonElement> ArrayTypeEntry : ArraysObject.entrySet()) {
            String ArrayType = ArrayTypeEntry.getKey();
            JsonElement ArrayTypeElement = ArrayTypeEntry.getValue();

            if (!ArrayTypeElement.isJsonObject()) {
                JavaUnbound.LOGGER.error("ENTITY RENDER CONTROLLER: Array type '{}' is not a JSON object in '{}'", ArrayType, Identifier);
                continue;
            }

            Map<String, List<String>> TypeArrays = new HashMap<>();

            for (Map.Entry<String, JsonElement> ArrayEntry : ArrayTypeElement.getAsJsonObject().entrySet()) {
                if (!ArrayEntry.getValue().isJsonArray()) {
                    JavaUnbound.LOGGER.error("ENTITY RENDER CONTROLLER: Array '{}' under type '{}' is not a JSON array in '{}'", ArrayEntry.getKey(), ArrayType, Identifier);
                    continue;
                }

                List<String> Values = new ArrayList<>();
                JsonArray JsonArray = ArrayEntry.getValue().getAsJsonArray();

                for (int i = 0; i < JsonArray.size(); i++) {
                    JsonElement Value = JsonArray.get(i);

                    if (!Value.isJsonPrimitive()) {
                        JavaUnbound.LOGGER.error("ENTITY RENDER CONTROLLER: Invalid value at index {} in array '{}' under type '{}' in '{}'", i, ArrayEntry.getKey(), ArrayType, Identifier);
                        continue;
                    }

                    Values.add(Value.getAsString());
                }

                TypeArrays.put(ArrayEntry.getKey(), Values);
            }

            Arrays.put(ArrayType, TypeArrays);
        }

        return Arrays;
    }

    public static List<String> GetTextures(String Identifier, JsonObject EntityRenderControllerObject) {
        List<String> Textures = new ArrayList<>();

        if (!EntityRenderControllerObject.has("textures")) {
            JavaUnbound.LOGGER.error("ENTITY RENDER CONTROLLER: No textures defined for '{}'", Identifier);
            return Textures;
        }

        JsonElement TexturesElement = EntityRenderControllerObject.get("textures");

        if (!TexturesElement.isJsonArray()) {
            JavaUnbound.LOGGER.error("ENTITY RENDER CONTROLLER: 'textures' is not a JSON array for '{}'", Identifier);
            return Textures;
        }

        JsonArray TexturesArray = TexturesElement.getAsJsonArray();

        for (int i = 0; i < TexturesArray.size(); i++) {
            JsonElement Texture = TexturesArray.get(i);

            if (!Texture.isJsonPrimitive()) {
                JavaUnbound.LOGGER.error("ENTITY RENDER CONTROLLER: Invalid texture at index {} in '{}'", i, Identifier);
                continue;
            }

            Textures.add(Texture.getAsString());
        }

        return Textures;
    }

    public static String GetGeometries(String Identifier, JsonObject EntityRenderControllerObject) {
        if (!EntityRenderControllerObject.has("geometry")) {
            JavaUnbound.LOGGER.error("ENTITY RENDER CONTROLLER: No geometry defined for '{}'", Identifier);
            return "";
        }

        JsonElement GeometryElement = EntityRenderControllerObject.get("geometry");

        if (!GeometryElement.isJsonPrimitive() || !GeometryElement.getAsJsonPrimitive().isString()) {
            JavaUnbound.LOGGER.error("ENTITY RENDER CONTROLLER: 'geometry' is not a string for '{}'", Identifier);
            return "";
        }

        return GeometryElement.getAsString();
    }

    public static Map<String, String> GetMaterials(String Identifier, JsonObject EntityRenderControllerObject) {
        Map<String, String> Materials = new HashMap<>();

        if (!EntityRenderControllerObject.has("materials")) {
            JavaUnbound.LOGGER.error("ENTITY RENDER CONTROLLER: No materials defined for '{}'", Identifier);
            return Materials;
        }

        JsonElement MaterialsElement = EntityRenderControllerObject.get("materials");

        if (!MaterialsElement.isJsonArray()) {
            JavaUnbound.LOGGER.error("ENTITY RENDER CONTROLLER: 'materials' is not a JSON array for '{}'", Identifier);
            return Materials;
        }

        JsonArray MaterialsArray = MaterialsElement.getAsJsonArray();

        for (int i = 0; i < MaterialsArray.size(); i++) {
            JsonElement MaterialElement = MaterialsArray.get(i);

            if (!MaterialElement.isJsonObject()) {
                JavaUnbound.LOGGER.error("ENTITY RENDER CONTROLLER: Material at index {} is not a JSON object in '{}'", i, Identifier);
                continue;
            }

            for (Map.Entry<String, JsonElement> Entry : MaterialElement.getAsJsonObject().entrySet()) {
                if (!Entry.getValue().isJsonPrimitive()) {
                    JavaUnbound.LOGGER.error("ENTITY RENDER CONTROLLER: Material '{}' at index {} is not a primitive value in '{}'", Entry.getKey(), i, Identifier);
                    continue;
                }

                Materials.put(Entry.getKey(), Entry.getValue().getAsString());
            }
        }

        return Materials;
    }
}