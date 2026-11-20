package com.java_unbound.mods.yacl;

import com.google.gson.JsonElement;
import com.java_unbound.JavaUnbound;
import com.java_unbound.mods.yacl.types.Dropdown;
import com.java_unbound.shared.loaders.manifest.ManifestLoader;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import net.minecraft.network.chat.Component;

public class ModScreen {
    public static YetAnotherConfigLib.Builder Builder;

    public static ConfigCategory.Builder AddonCategory;

    private static net.minecraft.client.gui.screens.Screen ParentScreen;
    private static net.minecraft.client.gui.screens.Screen CurrentScreen;

    public static void Create(String Title) {
        Builder = YetAnotherConfigLib.createBuilder().title(Component.literal(Title));

        AddonCategory = ConfigCategory.createBuilder().name(Component.literal(ManifestLoader.Manifest.Header.Name));
        //AddonCategory.option(Option.<Boolean>createBuilder().name(Component.literal("Test")).binding(true, () -> true, v -> {}).controller(TickBoxControllerBuilder::create).build());
    }

    public static ConfigCategory.Builder GetCategory(String Tab) {
        if ("Addon".equalsIgnoreCase(Tab)) {return AddonCategory;}

        return null;
    }

    public static YetAnotherConfigLib Build() {
        return Builder.category(AddonCategory.build()).build();
    }

    public static net.minecraft.client.gui.screens.Screen ToggleScreen(net.minecraft.client.gui.screens.Screen Parent) {
        ParentScreen = Parent;

        Create(JavaUnbound.MOD_NAME);

        SettingsLoader.LoadAddonSettings();

        CurrentScreen = Build().generateScreen(ParentScreen);

        return CurrentScreen;
    }
}