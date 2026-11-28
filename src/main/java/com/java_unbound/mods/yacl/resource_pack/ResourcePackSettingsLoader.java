package com.java_unbound.mods.yacl.resource_pack;

import com.java_unbound.JavaUnbound;
import com.java_unbound.mods.yacl.types.Dropdown;
import com.java_unbound.mods.yacl.types.Scrollbar;
import com.java_unbound.mods.yacl.types.TextLabel;
import com.java_unbound.mods.yacl.types.Toggle;
import com.java_unbound.shared.definitions.manifest.ManifestSettingsDefinition;
import com.java_unbound.shared.definitions.resource_pack.ResourcePackDefinition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ResourcePackSettingsLoader {
    public static void LoadResourcePackSettings(ResourcePackDefinition PackDefinition) {
        HashMap<String, List<ManifestSettingsDefinition>> Settings = new HashMap<>();

        Scrollbar.SubpackCreateScrollbarOption(PackDefinition);

        for (ManifestSettingsDefinition SettingsDefinition : PackDefinition.Manifest.Settings) {
            if (SettingsDefinition.Type == null) {
                JavaUnbound.LOGGER.warn("Setting {} has no type", SettingsDefinition.Name);
                continue;
            }

            Settings.computeIfAbsent(SettingsDefinition.Type, Key -> new ArrayList<>()).add(SettingsDefinition);
        }

        for (ManifestSettingsDefinition Setting : Settings.getOrDefault("dropdown", List.of())) {
            Dropdown.AddonCreateDropdownButton(PackDefinition, Setting);
        }

        TextLabel.CreateTextLabel("ResourcePack", "");

        for (ManifestSettingsDefinition Setting : Settings.getOrDefault("toggle", List.of())) {
            Toggle.AddonCreateToggleButton(PackDefinition, Setting);
        }
    }
}
