
package com.java_unbound.mods.yacl;

import com.google.gson.JsonObject;
import com.java_unbound.JavaUnbound;
import com.java_unbound.shared.definitions.resource_pack.ResourcePackDefinition;
import com.java_unbound.shared.loader.resourcepacks.ResourcePackLoader;

public class ResourcePackSettingsUtilities {
    public static boolean GetPackSetting(String PackId, String SettingName, boolean DefaultValue) {
        JsonObject PackSettings = ResourcePackSettingsManager.GetPackSettings(PackId);
        return PackSettings.has(SettingName) ? PackSettings.get(SettingName).getAsBoolean() : DefaultValue;
    }

    public static String GetPackSetting(String PackId, String SettingName, String DefaultValue) {
        JsonObject PackSettings = ResourcePackSettingsManager.GetPackSettings(PackId);
        return PackSettings.has(SettingName) ? PackSettings.get(SettingName).getAsString() : DefaultValue;
    }

    public static double GetPackSetting(String PackId, String SettingName, double DefaultValue) {
        JsonObject PackSettings = ResourcePackSettingsManager.GetPackSettings(PackId);
        return PackSettings.has(SettingName) ? PackSettings.get(SettingName).getAsDouble() : DefaultValue;
    }

    public static String GetSubpackSetting(String PackId) {
        ResourcePackDefinition PackDefinition = ResourcePackLoader.GetPack(PackId);

        if (PackDefinition == null || PackDefinition.Manifest.Subpacks == null || PackDefinition.Manifest.Subpacks.isEmpty()) {
            JavaUnbound.LOGGER.debug("No Subpacks in Pack ID: " + PackId);
            return "";
        }

        double Value = ResourcePackSettingsManager.GetPackSetting(PackId, "subpack", 0.0);
        int Index = (int) Math.round(Value * (PackDefinition.Manifest.Subpacks.size() - 1));

        Index = Math.clamp(Index, 0, PackDefinition.Manifest.Subpacks.size() - 1);

        return PackDefinition.Manifest.Subpacks.get(Index).FolderName;
    }
}
