package com.java_unbound.mod_menu.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.HashMap;
import java.util.Map;

public class ModConfig {
    public static ModConfig INSTANCE = new ModConfig();

    public Map<String, JsonElement> SETTINGS = new HashMap<>();
    public Map<String, JsonElement> ANIMATED_TEXTURES = new HashMap<>();

    public void RegisterSetting(String Tab, String Name, JsonElement DefaultValue) {
        if (Name == null || DefaultValue == null) {
            return;
        }

        SETTINGS.putIfAbsent(Name, DefaultValue.deepCopy());
    }

    public JsonElement GetSetting(String Name) {
        return SETTINGS.get(Name);
    }

    public void SetSetting(String Name, JsonElement Value) {
        if (Name == null || Value == null) {
            return;
        }

        SETTINGS.put(Name, Value.deepCopy());
    }

    public boolean GetBoolean(String Name, boolean DefaultValue) {
        JsonElement Value = GetSetting(Name);

        if (Value == null || !Value.isJsonPrimitive() || !Value.getAsJsonPrimitive().isBoolean()) {
            return DefaultValue;
        }

        return Value.getAsBoolean();
    }

    public String GetString(String Name, String DefaultValue) {
        JsonElement Value = GetSetting(Name);

        if (Value == null || !Value.isJsonPrimitive() || !Value.getAsJsonPrimitive().isString()) {
            return DefaultValue;
        }

        return Value.getAsString();
    }

    public void SetBoolean(String Name, boolean Value) {
        SetSetting(Name, new JsonPrimitive(Value));
    }

    public void SetString(String Name, String Value) {
        SetSetting(Name, new JsonPrimitive(Value));
    }

    public double GetDouble(String Name, double DefaultValue) {
        JsonElement Value = GetSetting(Name);

        if (Value == null || !Value.isJsonPrimitive() || !Value.getAsJsonPrimitive().isNumber()) {
            return DefaultValue;
        }

        return Value.getAsDouble();
    }

    public void SetDouble(String Name, double Value) {
        SetSetting(Name, new JsonPrimitive(Value));
    }

    public void RegisterAnimatedTexture(String Name) {
        if (Name == null) {return;}

        JsonObject Texture = new JsonObject();

        Texture.addProperty("Enabled", true);
        Texture.addProperty("FrameTime", 1);
        Texture.addProperty("Interpolate", true);

        if (!ANIMATED_TEXTURES.containsKey(Name)) {
            ANIMATED_TEXTURES.put(Name, Texture);

            ConfigManager.Save();
        }
    }

    public JsonObject GetAnimatedTexture(String Name) {
        JsonElement Texture = ANIMATED_TEXTURES.get(Name);

        if (Texture == null || !Texture.isJsonObject()) {
            return null;
        }

        return Texture.getAsJsonObject();
    }

    public void SetAnimatedTexture(String Name, boolean Enabled, int FrameTime, boolean Interpolate) {
        if (Name == null) {
            return;
        }

        JsonObject Texture = new JsonObject();

        Texture.addProperty("Enabled", Enabled);
        Texture.addProperty("FrameTime", Math.max(1, FrameTime));
        Texture.addProperty("Interpolate", Interpolate);

        ANIMATED_TEXTURES.put(Name, Texture);
    }

    public boolean IsAnimatedTextureEnabled(String Name) {
        JsonObject Texture = GetAnimatedTexture(Name);

        return Texture != null && Texture.has("Enabled") && Texture.get("Enabled").getAsBoolean();
    }

    public int GetAnimatedTextureFrameTime(String Name) {
        JsonObject Texture = GetAnimatedTexture(Name);

        return Texture != null && Texture.has("FrameTime") ? Math.max(1, Texture.get("FrameTime").getAsInt()) : 1;
    }

    public boolean GetAnimatedTextureInterpolate(String Name) {
        JsonObject Texture = GetAnimatedTexture(Name);

        return Texture != null && Texture.has("Interpolate") && Texture.get("Interpolate").getAsBoolean();
    }
}