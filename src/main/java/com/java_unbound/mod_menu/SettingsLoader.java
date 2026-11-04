package com.java_unbound.mod_menu;

import com.google.gson.*;
import com.java_unbound.JavaUnbound;
import com.java_unbound.loader.resourcepack.Folder;
import com.java_unbound.loader.resourcepack.LanguageLoader;
import com.java_unbound.mod_menu.config.ConfigManager;
import com.java_unbound.mod_menu.config.ModConfig;
import com.java_unbound.mod_menu.types.*;
import net.minecraft.resources.Identifier;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.java_unbound.utils.functions.GetStringFromJsonObject.GetString;

public class SettingsLoader {
    public static void LoadModSettings() {
        Identifier SettingsIdentifier = Identifier.fromNamespaceAndPath("java_unbound", "mod_settings.json");

        try (Reader Reader = new java.io.InputStreamReader(SettingsLoader.class.getResourceAsStream("/assets/java_unbound/mod_settings.json"), java.nio.charset.StandardCharsets.UTF_8)) {
            JsonElement RootElement = JsonParser.parseReader(Reader);

            if (!RootElement.isJsonObject()) {
                throw new JsonParseException("Invalid mod_settings.json root.");
            }

            JsonObject RootObject = RootElement.getAsJsonObject();

            if (!RootObject.has("settings") || !RootObject.get("settings").isJsonObject()) {
                throw new JsonParseException("Missing settings object in mod_settings.json.");
            }

            JsonObject SettingsObject = RootObject.getAsJsonObject("settings");

            for (Map.Entry<String, JsonElement> Entry : SettingsObject.entrySet()) {
                JsonElement SettingElement = Entry.getValue();
                if (!SettingElement.isJsonObject()) {continue;}

                JsonObject SettingObject = SettingElement.getAsJsonObject();

                String Tab = GetString(SettingObject, "Tab");
                String Name = GetString(SettingObject, "Name");
                String Type = GetString(SettingObject, "Type");
                JsonElement Default = SettingObject.get("DefaultValue");

                if (Tab == null || Name == null || Type == null || Default == null || !Default.isJsonPrimitive() || !Type.equalsIgnoreCase("Dropdown")) {continue;}
                if (ModMenuIntegration.GetCategory(Tab) == null || !Default.getAsJsonPrimitive().isString()) {continue;}

                JsonElement OptionsElement = SettingObject.get("options");
                if (OptionsElement == null || !OptionsElement.isJsonObject()) {continue;}

                JsonObject OptionsObject = OptionsElement.getAsJsonObject();
                JsonElement NamesElement = OptionsObject.get("Names");
                JsonElement ValuesElement = OptionsObject.get("Values");

                if (NamesElement == null || !NamesElement.isJsonArray() || ValuesElement == null || !ValuesElement.isJsonArray()) {continue;}

                JsonArray NamesArray = NamesElement.getAsJsonArray();
                JsonArray ValuesArray = ValuesElement.getAsJsonArray();

                if (NamesArray.size() != ValuesArray.size()) {continue;}

                List<String> OptionNames = new ArrayList<>();
                List<String> OptionValues = new ArrayList<>();

                for (int Index = 0; Index < NamesArray.size(); Index++) {
                    JsonElement OptionNameElement = NamesArray.get(Index);
                    JsonElement OptionValueElement = ValuesArray.get(Index);

                    if (!OptionNameElement.isJsonPrimitive() || !OptionValueElement.isJsonPrimitive()) {continue;}

                    OptionNames.add(OptionNameElement.getAsString());
                    OptionValues.add(OptionValueElement.getAsString());
                }

                String DefaultValue = Default.getAsString();
                if (OptionNames.isEmpty() || !OptionNames.contains(DefaultValue)) {continue;}

                ModConfig.INSTANCE.RegisterSetting(Tab, Name, Default);

                String CurrentValue = ModConfig.INSTANCE.GetString(Name, DefaultValue);

                if (!OptionNames.contains(CurrentValue)) {
                    CurrentValue = DefaultValue;
                    ModConfig.INSTANCE.SetString(Name, CurrentValue);
                }

                DropDown.CreateDropDownButton(Tab, Name, Name, CurrentValue, OptionNames, OptionValues);
            }

            TextLabelController.CreateTextLabel("Mod", "");

            for (Map.Entry<String, JsonElement> Entry : SettingsObject.entrySet()) {
                JsonElement SettingElement = Entry.getValue();
                if (!SettingElement.isJsonObject()) {continue;}

                JsonObject SettingObject = SettingElement.getAsJsonObject();

                String Tab = GetString(SettingObject, "Tab");
                String Name = GetString(SettingObject, "Name");
                String Type = GetString(SettingObject, "Type");
                JsonElement Default = SettingObject.get("DefaultValue");

                if (Tab == null || Name == null || Type == null || Default == null || !Default.isJsonPrimitive() || !Type.equalsIgnoreCase("Toggle")) {continue;}
                if (ModMenuIntegration.GetCategory(Tab) == null || !Default.getAsJsonPrimitive().isBoolean()) {continue;}

                ModConfig.INSTANCE.RegisterSetting(Tab, Name, Default);

                boolean CurrentValue = ModConfig.INSTANCE.GetBoolean(Name, Default.getAsBoolean());

                Toggle.CreateToggleButton(Tab, Name, Name, CurrentValue);
            }

            for (Map.Entry<String, JsonElement> Entry : SettingsObject.entrySet()) {
                JsonElement SettingElement = Entry.getValue();
                if (!SettingElement.isJsonObject()) {continue;}

                JsonObject SettingObject = SettingElement.getAsJsonObject();
                String Tab = GetString(SettingObject, "Tab");
                String Name = GetString(SettingObject, "Name");
                String Type = GetString(SettingObject, "Type");
                JsonElement Default = SettingObject.get("DefaultValue");
                JsonElement RangeElement = SettingObject.get("Range");

                double Step = 1.0;

                try {
                    JsonElement StepElement = SettingObject.get("Step");

                    if (StepElement != null && StepElement.isJsonPrimitive() && StepElement.getAsJsonPrimitive().isNumber()) {
                        Step = StepElement.getAsDouble();
                    } else if (StepElement != null && StepElement.isJsonPrimitive() && StepElement.getAsJsonPrimitive().isString()) {
                        Step = Double.parseDouble(StepElement.getAsString());
                    }
                } catch (NumberFormatException Exception) {
                    Step = 1.5;
                }

                if (!Double.isFinite(Step) || Step <= 0.0) {
                    Step = 1.5;
                }

                if (Tab == null || Name == null || Type == null || Default == null || !Type.equalsIgnoreCase("Scrollbar") || !Default.isJsonPrimitive() || !Default.getAsJsonPrimitive().isNumber() || RangeElement == null || !RangeElement.isJsonArray()) {continue;}
                if (ModMenuIntegration.GetCategory(Tab) == null) {continue;}

                JsonArray Range = RangeElement.getAsJsonArray();
                if (Range.size() != 2 || !Range.get(0).isJsonPrimitive() || !Range.get(1).isJsonPrimitive() || !Range.get(0).getAsJsonPrimitive().isNumber() || !Range.get(1).getAsJsonPrimitive().isNumber()) {continue;}

                double DefaultValue = Default.getAsDouble();
                double Minimum = Range.get(0).getAsDouble();
                double Maximum = Range.get(1).getAsDouble();

                if (Minimum > Maximum) {continue;}

                DefaultValue = Math.clamp(DefaultValue, Minimum, Maximum);

                ModConfig.INSTANCE.RegisterSetting(Tab, Name, new JsonPrimitive(DefaultValue));

                double CurrentValue = ModConfig.INSTANCE.GetDouble(Name, DefaultValue);

                if (CurrentValue < Minimum || CurrentValue > Maximum) {
                    CurrentValue = DefaultValue;
                    ModConfig.INSTANCE.SetDouble(Name, CurrentValue);
                }

                Scrollbar.CreateScrollbarButton(Tab, Name, Name, DefaultValue, Minimum, Maximum, Step);
            }

            TextLabelController.CreateTextLabel("Mod", "");

            for (Map.Entry<String, JsonElement> Entry : SettingsObject.entrySet()) {
                JsonElement SettingElement = Entry.getValue();
                if (!SettingElement.isJsonObject()) {continue;}

                JsonObject SettingObject = SettingElement.getAsJsonObject();

                String Tab = GetString(SettingObject, "Tab");
                String Name = GetString(SettingObject, "Name");
                String Type = GetString(SettingObject, "Type");
                String Action = GetString(SettingObject, "Action");

                if (Tab == null || Name == null || Type == null || Action == null || !Type.equalsIgnoreCase("Button")) {continue;}
                if (ModMenuIntegration.GetCategory(Tab) == null) {continue;}

                Button.CreateToggleButton("Mod", Name, Action);
            }

            ConfigManager.Save();

        } catch (IOException | JsonParseException Exception) {
            Exception.printStackTrace();
        }
    }

    public static void LoadSettings() {
        Path ConfigPath = Folder.GetConfigFolder();
        if (!Files.isDirectory(ConfigPath)) {return;}

        Path ManifestFilePath = ConfigPath.resolve("manifest.json");
        if (!Files.isRegularFile(ManifestFilePath)) {return;}

        boolean ToggleCreated = false;

        ModConfig.INSTANCE.RegisterSetting("Mod", "Description Color", new JsonPrimitive("§8"));

        try (Reader Reader = Files.newBufferedReader(ManifestFilePath)) {
            JsonElement RootElement = JsonParser.parseReader(Reader);
            if (!RootElement.isJsonObject()) {return;}

            JsonObject RootObject = RootElement.getAsJsonObject();
            if (!RootObject.has("settings") || !RootObject.get("settings").isJsonArray()) {return;}

            JsonArray SettingsArray = RootObject.getAsJsonArray("settings");

            for (JsonElement SettingElement : SettingsArray) {
                if (!SettingElement.isJsonObject()) {continue;}

                JsonObject SettingObject = SettingElement.getAsJsonObject();

                String Type = GetString(SettingObject, "type");
                String Name = GetString(SettingObject, "name");
                String TextIdentifier = GetString(SettingObject, "text");

                if (Type == null || Name == null || TextIdentifier == null) {continue;}

                String Text = LanguageLoader.GetText(TextIdentifier);
                JsonElement Default = SettingObject.get("default");

                if (Text == null || Default == null || !Default.isJsonPrimitive()) {continue;}

                String DescriptionColor = ModConfig.INSTANCE.GetString("Description Color", "§8");
                Text = Text.replace("§8", DescriptionColor);

                if (Type.equals("toggle")) {
                    if (!Default.getAsJsonPrimitive().isBoolean()) {continue;}

                    ModConfig.INSTANCE.RegisterSetting("Addon", Name, Default);

                    boolean CurrentValue = ModConfig.INSTANCE.GetBoolean(Name, Default.getAsBoolean());

                    if (!ToggleCreated) {
                        ToggleCreated = true;

                        TextLabelController.CreateTextLabel("Addon", "");
                    }

                    Toggle.CreateToggleButton("Addon", Text, Name, CurrentValue);
                } else if (Type.equals("dropdown")) {
                    if (!Default.getAsJsonPrimitive().isString()) {continue;}

                    JsonElement OptionsElement = SettingObject.get("options");
                    if (OptionsElement == null || !OptionsElement.isJsonArray()) {continue;}

                    JsonArray Options = OptionsElement.getAsJsonArray();

                    List<String> OptionNames = new ArrayList<>();
                    List<String> OptionValues = new ArrayList<>();

                    for (JsonElement OptionElement : Options) {
                        if (!OptionElement.isJsonObject()) {continue;}

                        JsonObject OptionObject = OptionElement.getAsJsonObject();

                        String OptionName = GetString(OptionObject, "name");
                        String OptionText = GetString(OptionObject, "text");

                        if (OptionName == null || OptionText == null) {continue;}

                        OptionNames.add(OptionName);
                        OptionValues.add(LanguageLoader.GetText(OptionText));
                    }


                    String DefaultValue = Default.getAsString();
                    if (!OptionNames.contains(DefaultValue)) {continue;}

                    ModConfig.INSTANCE.RegisterSetting("Addon", Name, Default);

                    String CurrentValue = ModConfig.INSTANCE.GetString(Name, DefaultValue);

                    if (!OptionNames.contains(CurrentValue)) {
                        CurrentValue = DefaultValue;

                        ModConfig.INSTANCE.SetString(Name, CurrentValue);
                    }

                    int CurrentIndex = OptionNames.indexOf(CurrentValue);
                    String CurrentText = OptionValues.get(CurrentIndex);

                    DropDown.CreateDropDownButton("Addon", Text, Name, CurrentValue, OptionNames, OptionValues);
                }
            }

            ConfigManager.Save();
        } catch (IOException | JsonParseException Exception) {
            Exception.printStackTrace();
        }
    }

    public static void LoadSubpackSettings() {
        Path ConfigPath = Folder.GetConfigFolder();
        if (!Files.isDirectory(ConfigPath)) {return;}

        Path ManifestFilePath = ConfigPath.resolve("manifest.json");
        if (!Files.isRegularFile(ManifestFilePath)) {return;}

        try (Reader Reader = Files.newBufferedReader(ManifestFilePath)) {
            JsonElement RootElement = JsonParser.parseReader(Reader);
            if (!RootElement.isJsonObject()) {return;}

            JsonObject RootObject = RootElement.getAsJsonObject();
            if (!RootObject.has("subpacks") || !RootObject.get("subpacks").isJsonArray()) {return;}

            JsonArray SubpacksArray = RootObject.getAsJsonArray("subpacks");

            List<String> SubpackNames = new ArrayList<>();
            List<String> SubpackFolders = new ArrayList<>();

            for (JsonElement SubpackElement : SubpacksArray) {
                if (!SubpackElement.isJsonObject()) {continue;}

                JsonObject SubpackObject = SubpackElement.getAsJsonObject();

                String FolderName = GetString(SubpackObject, "folder_name");
                String Name = GetString(SubpackObject, "name");

                if (FolderName == null || Name == null) {continue;}

                SubpackFolders.add(FolderName);
                SubpackNames.add(Name);
            }

            if (SubpackFolders.isEmpty()) {return;}

            Scrollbar.CreateSubpackScrollbarButton("Addon", SubpackNames, SubpackFolders);

        } catch (IOException | JsonParseException Exception) {
            Exception.printStackTrace();
        }
    }
}
