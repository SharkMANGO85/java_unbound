
package com.java_unbound.mods.yacl.types;

import com.java_unbound.mods.yacl.ResourcePackSettingsManager;
import com.java_unbound.mods.yacl.controllers.TexturedToggleController;
import com.java_unbound.mods.yacl.resource_pack.ResourcePackScreen;
import com.java_unbound.shared.definitions.manifest.ManifestSettingsDefinition;
import com.java_unbound.shared.definitions.resource_pack.ResourcePackDefinition;
import com.java_unbound.shared.resources.ResourceUtilities;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class Toggle {
    public static void AddonCreateToggleButton(ResourcePackDefinition PackDefinition, ManifestSettingsDefinition SettingsDefinition) {
        String PackId = ResourcePackScreen.GetCurrentPackId();
        String SettingName = SettingsDefinition.Name;
        boolean DefaultValue = GetDefaultValue(SettingsDefinition);
        var PackSettings = ResourcePackSettingsManager.GetPackSettings(PackId);
        boolean InitialValue = PackSettings.has(SettingName) ? PackSettings.get(SettingName).getAsBoolean() : DefaultValue;
        boolean[] Current = {InitialValue};

        Option<Boolean> ToggleOption = Option.<Boolean>createBuilder()
                .name(Component.literal(PackDefinition.GetTranslatedText(PackDefinition, SettingsDefinition.Text)))
                .description(OptionDescription.of(Component.empty()))
                .binding(InitialValue, () -> Current[0], Value -> {
                    Current[0] = Value;
                    ResourcePackSettingsManager.SetPackSetting(PackId, SettingName, Value);
                })
                .customController(TexturedToggleController::new)
                .build();

        ResourcePackScreen.GetCategory("ResourcePack").option(ToggleOption);
    }

    private static boolean GetDefaultValue(ManifestSettingsDefinition SettingsDefinition) {
        if (SettingsDefinition.Defaults == null || SettingsDefinition.Defaults.isEmpty()) {
            return false;
        }

        return Boolean.parseBoolean(SettingsDefinition.Defaults.get(0));
    }
}