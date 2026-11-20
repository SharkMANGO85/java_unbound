package com.java_unbound.mods.yacl;

import com.google.gson.JsonElement;
import com.java_unbound.JavaUnbound;
import com.java_unbound.mods.yacl.types.Dropdown;
import com.java_unbound.mods.yacl.types.Scrollbar;
import com.java_unbound.shared.definitions.manifest.ManifestSettingsDefinition;
import com.java_unbound.shared.loaders.manifest.ManifestLoader;
import com.java_unbound.shared.resourcepack.Folder;

import java.nio.file.Path;

public class SettingsLoader {
    public static void LoadAddonSettings() {
        if (ManifestLoader.Manifest == null) {
            JavaUnbound.LOGGER.error("Manifest not found");
            return;
        };

        if (ManifestLoader.Manifest.Subpacks != null) {
            Scrollbar.SubpackCreateScrollbarOption(ManifestLoader.Manifest);
        }

        for (ManifestSettingsDefinition Defintion : ManifestLoader.Manifest.Settings) {
            if (Defintion.Type.equals("dropdown")) {
                Dropdown.AddonCreateDropdownButton(Defintion);
            }
        }
    }
}
