package com.java_unbound.mod_menu.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

public class ConfigManager {

    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("java_unbound_mod").resolve("java_unbound_saves.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static Object GetValue(String Type, String Key) {
        if ("SETTING".equals(Type)) {
            return ModConfig.INSTANCE.SETTINGS.get(Key);
        }

        if ("ANIMATED_TEXTURE".equals(Type)) {
            return ModConfig.INSTANCE.ANIMATED_TEXTURES.get(Key);
        }

        return null;
    }

    public static void Save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());

            Files.writeString(CONFIG_PATH, GSON.toJson(ModConfig.INSTANCE));
        } catch (IOException Exception) {
            Exception.printStackTrace();
        }
    }

    public static void Load() {
        if (!Files.exists(CONFIG_PATH)) {
            Save();
            return;
        }

        try {
            String Json = Files.readString(CONFIG_PATH);
            ModConfig Loaded = GSON.fromJson(Json, ModConfig.class);

            if (Loaded != null) {
                if (Loaded.SETTINGS == null) {
                    Loaded.SETTINGS = new HashMap<>();
                }

                if (Loaded.ANIMATED_TEXTURES == null) {
                    Loaded.ANIMATED_TEXTURES = new HashMap<>();
                }

                ModConfig.INSTANCE = Loaded;
            }
        } catch (Exception Exception) {
            Exception.printStackTrace();
        }
    }
}