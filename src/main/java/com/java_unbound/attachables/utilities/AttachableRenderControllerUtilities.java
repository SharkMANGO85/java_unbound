package com.java_unbound.attachables.utilities;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AttachableRenderControllerUtilities {
    public static JsonObject GetArrays(JsonObject RenderControllerObject) {
        if (RenderControllerObject == null) {
            return new JsonObject();
        }

        JsonElement ArraysElement = RenderControllerObject.get("arrays");

        return ArraysElement != null && ArraysElement.isJsonObject() ? ArraysElement.getAsJsonObject() : new JsonObject();
    }

    public static JsonObject GetArrayType(JsonObject RenderControllerObject, String ArrayType) {
        JsonObject ArraysObject = GetArrays(RenderControllerObject);
        JsonElement ArrayTypeElement = ArraysObject.get(ArrayType);

        return ArrayTypeElement != null && ArrayTypeElement.isJsonObject() ? ArrayTypeElement.getAsJsonObject() : new JsonObject();
    }

    public static JsonArray GetArrayName(JsonObject ArrayTypeObject, String ArrayName) {
        if (ArrayTypeObject == null) {
            return new JsonArray();
        }

        JsonElement ArrayElement = ArrayTypeObject.get(ArrayName);

        return ArrayElement != null && ArrayElement.isJsonArray() ? ArrayElement.getAsJsonArray() : new JsonArray();
    }

    public static String GetGeometry(JsonObject RenderControllerObject) {
        JsonElement GeometryElement = RenderControllerObject.get("geometry");

        return GeometryElement != null && GeometryElement.isJsonPrimitive() ? GeometryElement.getAsString() : null;
    }

    public static List<String> GetTextures(JsonObject RenderControllerObject) {
        JsonElement TexturesElement = RenderControllerObject.get("textures");
        List<String> Textures = new ArrayList<>();

        if (TexturesElement != null && TexturesElement.isJsonArray()) {
            for (JsonElement TextureElement : TexturesElement.getAsJsonArray()) {
                if (TextureElement.isJsonPrimitive()) Textures.add(TextureElement.getAsString());
            }
        }

        return Textures;
    }

    public static List<Map<String, String>> GetMaterials(JsonObject RenderControllerObject) {
        JsonElement MaterialsElement = RenderControllerObject.get("materials");
        List<Map<String, String>> Materials = new ArrayList<>();

        if (MaterialsElement != null && MaterialsElement.isJsonArray()) {
            for (JsonElement MaterialElement : MaterialsElement.getAsJsonArray()) {
                if (!MaterialElement.isJsonObject()) continue;

                Map<String, String> Material = new LinkedHashMap<>();
                for (Map.Entry<String, JsonElement> Entry : MaterialElement.getAsJsonObject().entrySet()) {
                    if (Entry.getValue().isJsonPrimitive()) {
                        Material.put(Entry.getKey(), Entry.getValue().getAsString());
                    }
                }

                Materials.add(Material);
            }
        }

        return Materials;
    }
}