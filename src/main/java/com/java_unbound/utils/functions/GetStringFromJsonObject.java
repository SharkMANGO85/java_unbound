package com.java_unbound.utils.functions;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class GetStringFromJsonObject {
    public static String GetString(JsonObject Object, String Key) {
        if (!Object.has(Key) || Object.get(Key).isJsonNull()) {return null;}

        JsonElement Element = Object.get(Key);
        if (!Element.isJsonPrimitive() || !Element.getAsJsonPrimitive().isString()) {return null;}

        return Element.getAsString();
    }
}
