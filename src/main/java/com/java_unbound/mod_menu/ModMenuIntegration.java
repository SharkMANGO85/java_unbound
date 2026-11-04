package com.java_unbound.mod_menu;

import com.java_unbound.JavaUnbound;
import com.java_unbound.loader.resourcepack.Folder;
import com.java_unbound.loader.resourcepack.PackLoader;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.gui.YACLScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.nio.file.Path;

import static com.java_unbound.mod_menu.SettingsLoader.*;

public class ModMenuIntegration implements ModMenuApi {
    public static YetAnotherConfigLib.Builder Builder;
    public static ConfigCategory.Builder AddonCategory;
    public static ConfigCategory.Builder ModCategory;

    private static Screen ParentScreen;
    private static Screen CurrentScreen;

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return ParentScreen -> ToggleScreen(ParentScreen);
    }

    public static void Create(String Title) {
        Path ResourcePack = Folder.GetConfigFolder();

        Builder = YetAnotherConfigLib.createBuilder().title(Component.literal(Title));
        AddonCategory = ConfigCategory.createBuilder().name(Component.literal(PackLoader.GetPackName(ResourcePack) + " Settings"));
        ModCategory = ConfigCategory.createBuilder().name(Component.literal(JavaUnbound.MOD_NAME + " Settings"));
    }

    public static YetAnotherConfigLib Build() {
        return Builder.category(AddonCategory.build()).category(ModCategory.build()).build();
    }

    public static ConfigCategory.Builder GetCategory(String Tab) {
        if ("Addon".equalsIgnoreCase(Tab)) {return AddonCategory;}
        if ("Mod".equalsIgnoreCase(Tab)) {return ModCategory;}

        return null;
    }

    public static void ReloadTab(String Tab) {
        if (!"Addon".equalsIgnoreCase(Tab) && !"Mod".equalsIgnoreCase(Tab)) {return;}

        Create(JavaUnbound.MOD_NAME);

        if ("Addon".equalsIgnoreCase(Tab)) {
            LoadSubpackSettings();
            LoadSettings();
        } else {
            LoadModSettings();
        }

        CurrentScreen = Build().generateScreen(ParentScreen);

        Minecraft.getInstance().gui.setScreen(CurrentScreen);

        if (CurrentScreen instanceof YACLScreen YaclScreen) {
            YaclScreen.tabNavigationBar.selectTab(1, false);
        }
    }

    public static Screen ToggleScreen(Screen Parent) {
        ParentScreen = Parent;

        Create(JavaUnbound.MOD_NAME);

        LoadSubpackSettings();
        LoadSettings();
        LoadModSettings();

        CurrentScreen = Build().generateScreen(ParentScreen);

        return CurrentScreen;
    }
}