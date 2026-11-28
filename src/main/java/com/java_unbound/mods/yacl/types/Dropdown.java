
package com.java_unbound.mods.yacl.types;

import com.java_unbound.mods.yacl.ResourcePackSettingsManager;
import com.java_unbound.mods.yacl.resource_pack.ResourcePackScreen;
import com.java_unbound.shared.definitions.manifest.ManifestSettingsDefinition;
import com.java_unbound.shared.definitions.manifest.ManifestSettingsOptionDefinition;
import com.java_unbound.shared.definitions.resource_pack.ResourcePackDefinition;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.controller.DropdownStringControllerBuilder;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class Dropdown {
    public static void AddonCreateDropdownButton(ResourcePackDefinition PackDefinition, ManifestSettingsDefinition SettingsDefinition) {
        TextLabel.CreateTextLabel("ResourcePack", PackDefinition.GetTranslatedText(PackDefinition, SettingsDefinition.Text));

        List<String> Values = new ArrayList<>();

        for (ManifestSettingsOptionDefinition Option : SettingsDefinition.Options) {
            Values.add(Option.Text);
        }

        if (Values.isEmpty()) {
            return;
        }

        String PackId = ResourcePackScreen.GetCurrentPackId();
        String SettingName = SettingsDefinition.Name;
        var PackSettings = ResourcePackSettingsManager.GetPackSettings(PackId);
        String DefaultName = SettingsDefinition.Defaults != null && !SettingsDefinition.Defaults.isEmpty() ? SettingsDefinition.Defaults.get(0) : null;
        String DefaultText = Values.get(0);

        for (ManifestSettingsOptionDefinition Option : SettingsDefinition.Options) {
            if (Option.Name.equals(DefaultName)) {
                DefaultText = Option.Text;
                break;
            }
        }

        String InitialText = DefaultText;

        if (PackSettings.has(SettingName)) {
            String SavedName = PackSettings.get(SettingName).getAsString();

            for (ManifestSettingsOptionDefinition Option : SettingsDefinition.Options) {
                if (Option.Name.equals(SavedName)) {
                    InitialText = Option.Text;
                    break;
                }
            }
        }

        String[] Current = {InitialText};

        Option<String> DropDownOption = Option.<String>createBuilder()
                .name(Component.literal(""))
                .description(OptionDescription.of(Component.empty()))
                .binding(InitialText, () -> Current[0], Value -> {
                    Current[0] = Value;

                    for (ManifestSettingsOptionDefinition Option : SettingsDefinition.Options) {
                        if (Option.Text.equals(Value)) {
                            ResourcePackSettingsManager.SetPackSetting(PackId, SettingName, Option.Name);
                            break;
                        }
                    }
                })
                .controller(Option -> DropdownStringControllerBuilder.create(Option).values(Values))
                .build();

        ResourcePackScreen.GetCategory("ResourcePack").option(DropDownOption);
    }
}