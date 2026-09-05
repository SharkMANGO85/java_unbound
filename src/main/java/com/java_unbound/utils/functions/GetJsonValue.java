package com.java_unbound.utils.functions;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class GetJsonValue {
    private GetJsonValue() {

    }

    public static JsonElement GetValue(String Content, String Path) {
        if (Content == null || Content.isEmpty() || Path == null || Path.isEmpty()) {
            return null;
        }

        JsonElement Current;

        try {
            Current = JsonParser.parseString(Content);
        } catch (Exception Exception) {
            return null;
        }

        if (Current.isJsonObject() && Current.getAsJsonObject().has("minecraft:client_entity")) {
            Current = Current.getAsJsonObject().get("minecraft:client_entity");
        }

        for (String Key : Path.split("\\.")) {
            if (!Current.isJsonObject()) {
                return null;
            }

            JsonObject Object = Current.getAsJsonObject();

            if (!Object.has(Key)) {
                return null;
            }

            Current = Object.get(Key);
        }

        return Current;
    }
}