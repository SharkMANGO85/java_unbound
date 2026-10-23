package com.java_unbound.mod_menu.animated_textures;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.HashMap;

//Default check: FT = 1, Ip = true, Ebd = true
public class SetDefaultValues {
    public static final HashMap<String, JsonElement> DefaultValues = new HashMap<>();

    public static void SetDefaultValue(String Key, boolean Interpolate, int FrameTime, boolean Enabled) {
        JsonObject Values = new JsonObject();

        Values.addProperty("Interpolate", Interpolate);
        Values.addProperty("FrameTime", FrameTime);
        Values.addProperty("Enabled", Enabled);

        DefaultValues.put(Key, Values);
    }

    public static void LoadDefaultValues() {
        SetDefaultValue("textures/block/amethyst_block.png", true, 5, true);
    }
}