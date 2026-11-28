
package com.java_unbound.mods.yacl.resource_pack;

import com.java_unbound.JavaUnbound;
import com.java_unbound.mods.yacl.ResourcePackSettingsManager;
import com.java_unbound.shared.definitions.resource_pack.ResourcePackDefinition;
import com.java_unbound.shared.loader.resourcepacks.ResourcePackLoader;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ResourcePackScreen {
    public static YetAnotherConfigLib.Builder Builder;
    public static ConfigCategory.Builder PackCategory;

    private static ResourcePackDefinition CurrentPack;
    private static String CurrentPackId;

    private static Screen ParentScreen;
    private static Screen CurrentScreen;

    public static void Create(String Title, ResourcePackDefinition PackDefinition) {
        CurrentPack = PackDefinition;
        Builder = YetAnotherConfigLib.createBuilder().title(Component.literal(Title)).save(ResourcePackScreen::Save);
        PackCategory = ConfigCategory.createBuilder().name(Component.literal(PackDefinition.Manifest.Header.Name));
    }

    public static ConfigCategory.Builder GetCategory(String Tab) {
        if ("ResourcePack".equalsIgnoreCase(Tab)) {
            return PackCategory;
        }

        return null;
    }

    public static YetAnotherConfigLib Build() {
        return Builder.category(PackCategory.build()).build();
    }

    private static void Save() {
        JavaUnbound.LOGGER.info("Saving resource pack settings...");
        ResourcePackSettingsManager.SaveSettings();
    }

    public static Screen ToggleScreen(String PackId, Screen Parent) {
        ParentScreen = Parent;

        ResourcePackDefinition PackDefinition = ResourcePackLoader.GetPack(PackId);

        if (PackDefinition == null) {
            JavaUnbound.LOGGER.error("Couldn't find resource pack with id {}", PackId);
            return Parent;
        }

        CurrentPackId = PackId;

        Create("Settings: " + PackId, PackDefinition);
        ResourcePackSettingsLoader.LoadResourcePackSettings(PackDefinition);

        CurrentScreen = Build().generateScreen(ParentScreen);

        return CurrentScreen;
    }

    public static String GetCurrentPackId() {
        return CurrentPackId;
    }
}