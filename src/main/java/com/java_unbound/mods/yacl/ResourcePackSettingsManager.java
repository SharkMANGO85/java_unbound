
package com.java_unbound.mods.yacl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.java_unbound.JavaUnbound;
import com.java_unbound.shared.resourcepack.PackFolders;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ResourcePackSettingsManager {
    private static final Gson Gson = new GsonBuilder().setPrettyPrinting().create();
    public static final JsonObject Settings = new JsonObject();

    private static Path GetSettingsFile() {
        return PackFolders.GetSavesFolder().resolve("settings.json");
    }

    private static JsonObject GetResourcePacks() {
        if (!Settings.has("resourcepacks") || !Settings.get("resourcepacks").isJsonObject()) {
            Settings.add("resourcepacks", new JsonObject());
        }

        return Settings.getAsJsonObject("resourcepacks");
    }

    public static JsonObject GetPackSettings(String PackId) {
        JsonObject ResourcePacks = GetResourcePacks();

        if (!ResourcePacks.has(PackId) || !ResourcePacks.get(PackId).isJsonObject()) {
            ResourcePacks.add(PackId, new JsonObject());
        }

        return ResourcePacks.getAsJsonObject(PackId);
    }

    public static void SetPackSetting(String PackId, String SettingName, boolean Value) {
        GetPackSettings(PackId).addProperty(SettingName, Value);
        SaveSettings();
    }

    public static void SetPackSetting(String PackId, String SettingName, String Value) {
        GetPackSettings(PackId).addProperty(SettingName, Value);
        SaveSettings();
    }

    public static void SetPackSetting(String PackId, String SettingName, double Value) {
        GetPackSettings(PackId).addProperty(SettingName, Value);
        SaveSettings();
    }  public static double GetPackSetting(String PackId, String SettingName, double DefaultValue) {
        JsonObject PackSettings = GetPackSettings(PackId);
        return PackSettings.has(SettingName) ? PackSettings.get(SettingName).getAsDouble() : DefaultValue;
    }

    public static void LoadSettings() {
        Path SettingsFile = GetSettingsFile();

        try {
            Files.createDirectories(SettingsFile.getParent());

            if (!Files.exists(SettingsFile)) {
                SaveSettings();
                return;
            }

            JsonObject Loaded = Gson.fromJson(Files.readString(SettingsFile), JsonObject.class);

            if (Loaded != null) {
                Settings.add("resourcepacks", Loaded.has("resourcepacks") && Loaded.get("resourcepacks").isJsonObject() ? Loaded.getAsJsonObject("resourcepacks") : new JsonObject());
            }
        } catch (Exception Exception) {
            JavaUnbound.LOGGER.error("Failed to load settings", Exception);
        }
    }

    public static void SaveSettings() {
        Path SettingsFile = GetSettingsFile();

        try {
            Files.createDirectories(SettingsFile.getParent());
            Files.writeString(SettingsFile, Gson.toJson(Settings));

            JavaUnbound.LOGGER.info("Settings saved to {}", SettingsFile);
        } catch (IOException Exception) {
            JavaUnbound.LOGGER.error("Failed to save settings", Exception);
        }
    }
}
