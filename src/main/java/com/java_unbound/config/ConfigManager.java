package com.java_unbound.config;

import com.google.gson.*;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigManager {
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("java_unbound.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static JsonObject Config = new JsonObject();

    public static void save() {
        try {
            Files.writeString(CONFIG_PATH, GSON.toJson(Config));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void load() {
        if (!Files.exists(CONFIG_PATH)) {
            Config = new JsonObject();

            Config.addProperty("LoadedVersion", "");
            Config.addProperty("Loaded", false);

            save();
            return;
        }

        try {
            String json = Files.readString(CONFIG_PATH);

            Config = JsonParser.parseString(json).getAsJsonObject();
        } catch (Exception e) {
            e.printStackTrace();

            Config = new JsonObject();
            Config.addProperty("LoadedVersion", "");
            Config.addProperty("Loaded", false);

            save();
        }
    }

    public static void ChangeValue(String Type, Object Value) {
        if (Value instanceof String) {
            Config.addProperty(Type, (String) Value);
        } else if (Value instanceof Boolean) {
            Config.addProperty(Type, (Boolean) Value);
        } else if (Value instanceof Number) {
            Config.addProperty(Type, (Number) Value);
        } else if (Value == null) {
            Config.remove(Type);
        } else {
            throw new IllegalArgumentException("Unsupported config value type: " + Value.getClass());
        }

        save();
    }

    public static Object GetValue(String Type) {
        if (!Config.has(Type)) {
            return null;
        }

        JsonElement Value = Config.get(Type);

        if (Value.isJsonPrimitive()) {
            if (Value.getAsJsonPrimitive().isBoolean()) {
                return Value.getAsBoolean();
            }

            if (Value.getAsJsonPrimitive().isNumber()) {
                return Value.getAsNumber();
            }

            if (Value.getAsJsonPrimitive().isString()) {
                return Value.getAsString();
            }
        }

        return Value;
    }
}