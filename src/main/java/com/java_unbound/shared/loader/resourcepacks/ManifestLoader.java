package com.java_unbound.shared.loader.resourcepacks;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.java_unbound.JavaUnbound;
import com.java_unbound.shared.definitions.manifest.*;
import com.java_unbound.shared.definitions.resource_pack.ResourcePackDefinition;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ManifestLoader {
    public static ManifestDefinition GetManifestDefinition(Path ManifestPath, ResourcePackDefinition PackDefinition) {
        ManifestDefinition Definition = new ManifestDefinition();

        LoadHeader(Definition, ManifestPath, PackDefinition);
        LoadSubpacks(Definition, ManifestPath, PackDefinition);
        LoadSettings(Definition, ManifestPath, PackDefinition);

        return Definition;
    }

    private static void LoadSettings(ManifestDefinition Definition, Path ManifestPath, ResourcePackDefinition PackDefinition) {
        List<ManifestSettingsDefinition> Result = new ArrayList<>();
        Definition.Settings = Result;

        JsonObject Json = GetManifestJsonObject(ManifestPath);

        if (Json == null) {
            JavaUnbound.LOGGER.error("Manifest File Not Found");
            return;
        }

        JsonElement SettingsElement = GetElement(Json, "settings");

        if (SettingsElement == null) {
            return;
        }

        if (!SettingsElement.isJsonArray()) {
            JavaUnbound.LOGGER.error("Manifest settings must be an array");
            return;
        }

        for (JsonElement Element : SettingsElement.getAsJsonArray()) {
            JsonObject SettingJson = GetObject(Element);

            if (SettingJson == null) {
                JavaUnbound.LOGGER.warn("Skipping invalid setting entry in manifest");
                continue;
            }

            String Name = GetString(SettingJson, "name");

            if (Name == null || Name.isBlank()) {
                JavaUnbound.LOGGER.warn("Skipping setting without name");
                continue;
            }

            ManifestSettingsDefinition Setting = new ManifestSettingsDefinition();

            Setting.Name = Name;
            Setting.Type = GetString(SettingJson, "type");
            Setting.Text = PackDefinition.GetTranslatedValue(PackDefinition, SettingJson, "text");
            Setting.Defaults = GetSettingDefaults(SettingJson);
            Setting.Options = GetSettingOptions(SettingJson, PackDefinition);

            Result.add(Setting);
        }
    }

    private static void LoadHeader(ManifestDefinition Definition, Path ManifestPath, ResourcePackDefinition PackDefinition) {
        JsonObject Json = GetManifestJsonObject(ManifestPath);

        if (Json == null) {
            JavaUnbound.LOGGER.error("Manifest File Not Found");
            return;
        }

        JsonObject Header = GetObject(GetElement(Json, "header"));

        if (Header == null) {
            JavaUnbound.LOGGER.error("Manifest header is missing or invalid");
            return;
        }

        Definition.Header = new ManifestHeaderDefinition();

        Definition.Header.Description = PackDefinition.GetTranslatedValue(PackDefinition, Header, "description");
        Definition.Header.Name = PackDefinition.GetTranslatedValue(PackDefinition, Header, "name");
        Definition.Header.Version = PackDefinition.GetTranslatedValue(PackDefinition, Header, "version");
        Definition.Header.UUID = GetString(Header, "uuid");
        Definition.Header.MinimumEngineVersion = GetString(Header, "min_engine_version");
    }

    private static void LoadSubpacks(ManifestDefinition Definition, Path ManifestPath, ResourcePackDefinition PackDefinition) {
        JsonObject Json = GetManifestJsonObject(ManifestPath);

        if (Json == null) {
            JavaUnbound.LOGGER.error("Manifest File Not Found");
            return;
        }

        List<ManifestSubpackDefinition> Result = new ArrayList<>();
        Definition.Subpacks = Result;

        JsonElement SubpacksElement = GetElement(Json, "subpacks");

        if (SubpacksElement == null || SubpacksElement.isJsonNull()) {
            return;
        }

        if (!SubpacksElement.isJsonArray()) {
            JavaUnbound.LOGGER.error("Manifest subpacks must be an array");
            return;
        }

        for (JsonElement Element : SubpacksElement.getAsJsonArray()) {
            JsonObject SubpackJson = GetObject(Element);

            if (SubpackJson == null) {
                JavaUnbound.LOGGER.warn("Skipping invalid subpack entry in manifest");
                continue;
            }

            String FolderName = GetString(SubpackJson, "folder_name");

            if (FolderName == null || FolderName.isBlank()) {
                JavaUnbound.LOGGER.warn("Skipping subpack without folder_name");
                continue;
            }

            ManifestSubpackDefinition Subpack = new ManifestSubpackDefinition();
            Subpack.FolderName = FolderName;
            Subpack.Name = PackDefinition.GetTranslatedValue(PackDefinition, SubpackJson, "name");

            JsonElement Tier = GetElement(SubpackJson, "memory_tier");

            if (Tier != null && Tier.isJsonPrimitive()) {
                Subpack.MemoryPerformanceTier = Tier.getAsString();
            }

            Result.add(Subpack);
        }
    }

    private static List<String> GetSettingDefaults(JsonObject SettingJson) {
        List<String> Defaults = new ArrayList<>();

        JsonArray DefaultsArray = GetArray(SettingJson, "defaults");

        if (DefaultsArray != null) {
            for (JsonElement Element : DefaultsArray) {
                if (Element != null && Element.isJsonPrimitive()) {
                    Defaults.add(Element.getAsString());
                }
            }

            return Defaults;
        }

        JsonElement Default = GetElement(SettingJson, "default");

        if (Default != null && Default.isJsonPrimitive()) {
            Defaults.add(Default.getAsString());
        }

        return Defaults;
    }

    private static List<ManifestSettingsOptionDefinition> GetSettingOptions(JsonObject SettingJson, ResourcePackDefinition PackDefinition) {
        List<ManifestSettingsOptionDefinition> Options = new ArrayList<>();

        JsonArray OptionsArray = GetArray(SettingJson, "options");

        if (OptionsArray == null) {
            return Options;
        }

        for (JsonElement Element : OptionsArray) {
            JsonObject OptionJson = GetObject(Element);

            if (OptionJson == null) {
                JavaUnbound.LOGGER.warn("Skipping invalid setting option entry");
                continue;
            }

            String OptionName = GetString(OptionJson, "name");

            if (OptionName == null || OptionName.isBlank()) {
                JavaUnbound.LOGGER.warn("Skipping setting option without name");
                continue;
            }

            ManifestSettingsOptionDefinition Option = new ManifestSettingsOptionDefinition();
            Option.Name = OptionName;
            Option.Text = PackDefinition.GetTranslatedValue(PackDefinition, OptionJson, "text");

            Options.add(Option);
        }

        return Options;
    }

    private static JsonObject GetManifestJsonObject(Path ManifestPath) {
        try {
            JsonElement Element = JsonParser.parseString(Files.readString(ManifestPath));

            if (Element == null || !Element.isJsonObject()) {
                JavaUnbound.LOGGER.error("Manifest root is not a JSON object");
                return null;
            }

            return Element.getAsJsonObject();
        } catch (Exception Exception) {
            JavaUnbound.LOGGER.error("Failed to read manifest", Exception);
            return null;
        }
    }

    private static JsonObject GetObject(JsonElement Element) {
        return Element != null && Element.isJsonObject() ? Element.getAsJsonObject() : null;
    }

    private static JsonArray GetArray(JsonObject Json, String Key) {
        if (Json == null || !Json.has(Key)) return null;

        JsonElement Element = Json.get(Key);

        return Element != null && Element.isJsonArray() ? Element.getAsJsonArray() : null;
    }

    private static JsonElement GetElement(JsonObject Json, String Key) {
        if (Json == null || !Json.has(Key)) return null;

        JsonElement Element = Json.get(Key);

        return Element == null || Element.isJsonNull() ? null : Element;
    }

    public static String GetString(JsonObject Json, String Key) {
        JsonElement Element = GetElement(Json, Key);

        if (Element == null || !Element.isJsonPrimitive()) return null;

        try {
            return Element.getAsString();
        } catch (Exception Exception) {
            JavaUnbound.LOGGER.error("Invalid string property: {}", Key);
            return null;
        }
    }
}
