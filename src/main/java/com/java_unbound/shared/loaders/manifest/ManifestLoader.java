
package com.java_unbound.shared.loaders.manifest;

import com.google.gson.*;
import com.java_unbound.JavaUnbound;
import com.java_unbound.shared.definitions.manifest.*;
import com.java_unbound.shared.loaders.language.LanguageLoader;
import com.java_unbound.shared.resourcepack.Folder;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

public class ManifestLoader {
    public static ManifestDefinition Manifest = new ManifestDefinition();

    private static Path GetManifestFile() {
        Path ConfigFolder = Folder.GetAddonFolder();

        if (!Files.isDirectory(ConfigFolder)) {
            JavaUnbound.LOGGER.error("Config folder does not exist: {}", ConfigFolder);
            return null;
        }

        Path ManifestFile = ConfigFolder.resolve("manifest.json");

        if (!Files.isRegularFile(ManifestFile) || !Files.isReadable(ManifestFile)) {
            JavaUnbound.LOGGER.error("Manifest file does not exist or is not readable: {}", ManifestFile);
            return null;
        }

        return ManifestFile;
    }

    private static JsonObject GetManifestJson() {
        Path ManifestFile = GetManifestFile();

        if (ManifestFile == null) return null;

        try {
            JsonElement Element = JsonParser.parseString(Files.readString(ManifestFile));

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

    private static String GetString(JsonObject Json, String Key) {
        JsonElement Element = GetElement(Json, Key);

        if (Element == null || !Element.isJsonPrimitive()) return null;

        try {
            return Element.getAsString();
        } catch (Exception Exception) {
            JavaUnbound.LOGGER.error("Invalid string property: {}", Key);
            return null;
        }
    }

    private static String GetTranslatedText(String Text) {
        if (Text == null) return null;

        try {
            String TranslatedText = LanguageLoader.GetText(Text);

            return TranslatedText == null || TranslatedText.equals(JavaUnbound.UNKNOWN)
                    ? Text
                    : TranslatedText;
        } catch (Exception Exception) {
            return Text;
        }
    }

    private static String GetTranslatedValue(JsonObject Json, String Key) {
        String Value = GetString(Json, Key);

        return Value == null ? null : GetTranslatedText(Value);
    }

    private static void LoadHeader() {
        JsonObject Json = GetManifestJson();

        if (Json == null) return;

        JsonObject Header = GetObject(GetElement(Json, "header"));

        if (Header == null) {
            JavaUnbound.LOGGER.error("Manifest header is missing or invalid");
            return;
        }

        Manifest.Header = new ManifestHeaderDefinition();

        Manifest.Header.Description = GetTranslatedValue(Header, "description");
        Manifest.Header.Name = GetTranslatedValue(Header, "name");
        Manifest.Header.Version = GetTranslatedValue(Header, "version");
        Manifest.Header.UUID = GetString(Header, "uuid");
        Manifest.Header.MinimumEngineVersion = GetString(Header, "min_engine_version");
    }

    private static void LoadSubpacks() {
        JsonObject Json = GetManifestJson();

        if (Json == null) return;

        JsonArray Subpacks = GetArray(Json, "subpacks");

        if (Subpacks == null) {
            JavaUnbound.LOGGER.error("Manifest subpacks is missing or invalid");
            return;
        }

        Manifest.Subpacks = new ArrayList<>();

        for (JsonElement Subpack : Subpacks) {
            JsonObject SubpackJson = GetObject(Subpack);

            if (SubpackJson == null) continue;

            String FolderName = GetString(SubpackJson, "folder_name");
            String MemoryPerformanceTier = GetString(SubpackJson, "memory_performance_tier");
            String Name = GetString(SubpackJson, "name");

            if (FolderName == null || MemoryPerformanceTier == null || Name == null) {
                JavaUnbound.LOGGER.warn("Skipping invalid manifest subpack");
                continue;
            }

            ManifestSubpackDefinition Definition = new ManifestSubpackDefinition();

            Definition.FolderName = GetTranslatedText(FolderName);
            Definition.MemoryPerformanceTier = MemoryPerformanceTier;
            Definition.Name = GetTranslatedText(Name);

            Manifest.Subpacks.add(Definition);
        }
    }

    public static void LoadSettings() {
        JsonObject Json = GetManifestJson();

        if (Json == null) return;

        JsonArray Settings = GetArray(Json, "settings");

        if (Settings == null) {
            JavaUnbound.LOGGER.error("Manifest settings is missing or invalid");
            return;
        }

        Manifest.Settings = new ArrayList<>();

        for (JsonElement Setting : Settings) {
            JsonObject SettingJson = GetObject(Setting);

            if (SettingJson == null) continue;

            String Type = GetString(SettingJson, "type");
            String Text = GetTranslatedValue(SettingJson, "text");
            String Name = GetTranslatedValue(SettingJson, "name");

            if (Type == null || Text == null) {
                JavaUnbound.LOGGER.warn("Skipping manifest setting with missing type or text");
                continue;
            }

            ManifestSettingsDefinition Definition = new ManifestSettingsDefinition();

            Definition.Type = Type;
            Definition.Text = Text;
            Definition.Name = Name;
            Definition.Defaults = new ArrayList<>();
            Definition.Options = new ArrayList<>();

            JsonElement Default = GetElement(SettingJson, "default");

            if (Default != null) {
                if (Default.isJsonPrimitive()) {
                    Definition.Defaults.add(GetTranslatedText(Default.getAsString()));
                } else {
                    JavaUnbound.LOGGER.warn("Invalid default value for setting: {}", Name);
                }
            }

            JsonArray Defaults = GetArray(SettingJson, "defaults");

            if (Defaults != null) {
                for (JsonElement DefaultValue : Defaults) {
                    if (DefaultValue == null || DefaultValue.isJsonNull()) continue;

                    if (DefaultValue.isJsonPrimitive()) {
                        Definition.Defaults.add(GetTranslatedText(DefaultValue.getAsString()));
                    } else if (DefaultValue.isJsonObject()) {
                        String Value = GetString(DefaultValue.getAsJsonObject(), "value");

                        if (Value != null) {
                            Definition.Defaults.add(GetTranslatedText(Value));
                        }
                    }
                }
            }

            JsonArray Options = GetArray(SettingJson, "options");

            if (Options != null) {
                for (JsonElement Option : Options) {
                    JsonObject OptionJson = GetObject(Option);

                    if (OptionJson == null) continue;

                    String OptionName = GetTranslatedValue(OptionJson, "name");
                    String OptionText = GetTranslatedValue(OptionJson, "text");

                    if (OptionName == null || OptionText == null) {
                        JavaUnbound.LOGGER.warn("Skipping invalid option for setting: {}", Name);
                        continue;
                    }

                    ManifestSettingsOptionDefinition OptionDefinition = new ManifestSettingsOptionDefinition();

                    OptionDefinition.Name = OptionName;
                    OptionDefinition.Text = OptionText;

                    Definition.Options.add(OptionDefinition);
                }
            }

            Manifest.Settings.add(Definition);
        }
    }

    public static void DebugManifest() {
        JavaUnbound.LOGGER.info("========== MANIFEST DEBUG ==========");

        if (Manifest == null) {
            JavaUnbound.LOGGER.error("Manifest is null");
            return;
        }

        // Header
        JavaUnbound.LOGGER.info("---------- HEADER ----------");

        if (Manifest.Header != null) {
            JavaUnbound.LOGGER.info("Name: {}", Manifest.Header.Name);
            JavaUnbound.LOGGER.info("Description: {}", Manifest.Header.Description);
            JavaUnbound.LOGGER.info("Version: {}", Manifest.Header.Version);
            JavaUnbound.LOGGER.info("UUID: {}", Manifest.Header.UUID);
            JavaUnbound.LOGGER.info("Minimum Engine Version: {}", Manifest.Header.MinimumEngineVersion);
        } else {
            JavaUnbound.LOGGER.warn("Header is null");
        }

        // Subpacks
        JavaUnbound.LOGGER.info("---------- SUBPACKS ----------");

        if (Manifest.Subpacks != null) {
            for (int i = 0; i < Manifest.Subpacks.size(); i++) {
                ManifestSubpackDefinition Subpack = Manifest.Subpacks.get(i);

                if (Subpack == null) {
                    JavaUnbound.LOGGER.warn("Subpack {} is null", i);
                    continue;
                }

                JavaUnbound.LOGGER.info("Subpack #{}", i);
                JavaUnbound.LOGGER.info("  Name: {}", Subpack.Name);
                JavaUnbound.LOGGER.info("  Folder: {}", Subpack.FolderName);
                JavaUnbound.LOGGER.info("  Memory Performance Tier: {}", Subpack.MemoryPerformanceTier);
            }
        } else {
            JavaUnbound.LOGGER.warn("Subpacks are null");
        }

        // Settings
        JavaUnbound.LOGGER.info("---------- SETTINGS ----------");

        if (Manifest.Settings != null) {
            for (int i = 0; i < Manifest.Settings.size(); i++) {
                ManifestSettingsDefinition Setting = Manifest.Settings.get(i);

                if (Setting == null) {
                    JavaUnbound.LOGGER.warn("Setting {} is null", i);
                    continue;
                }

                JavaUnbound.LOGGER.info("Setting #{}", i);
                JavaUnbound.LOGGER.info("  Type: {}", Setting.Type);
                JavaUnbound.LOGGER.info("  Name: {}", Setting.Name);
                JavaUnbound.LOGGER.info("  Text: {}", Setting.Text);

                JavaUnbound.LOGGER.info("  Defaults:");

                if (Setting.Defaults != null && !Setting.Defaults.isEmpty()) {
                    for (String Default : Setting.Defaults) {
                        JavaUnbound.LOGGER.info("    - {}", Default);
                    }
                } else {
                    JavaUnbound.LOGGER.info("    (none)");
                }

                JavaUnbound.LOGGER.info("  Options:");

                if (Setting.Options != null && !Setting.Options.isEmpty()) {
                    for (ManifestSettingsOptionDefinition Option : Setting.Options) {
                        if (Option == null) {
                            JavaUnbound.LOGGER.warn("    Option is null");
                            continue;
                        }

                        JavaUnbound.LOGGER.info("    - Name: {}", Option.Name);
                        JavaUnbound.LOGGER.info("      Text: {}", Option.Text);
                    }
                } else {
                    JavaUnbound.LOGGER.info("    (none)");
                }
            }
        } else {
            JavaUnbound.LOGGER.warn("Settings are null");
        }

        JavaUnbound.LOGGER.info("========== END MANIFEST DEBUG ==========");
    }

    public static void LoadManifest() {
        Manifest = new ManifestDefinition();

        LoadHeader();
        LoadSubpacks();
        LoadSettings();

        //DebugManifest();
    }
}
