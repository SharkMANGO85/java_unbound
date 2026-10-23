package com.java_unbound.mod_menu.types;

import com.java_unbound.JavaUnboundClient;
import com.java_unbound.loader.resourcepack.Folder;
import com.java_unbound.mod_menu.ModMenuIntegration;
import com.java_unbound.mod_menu.config.ConfigManager;
import com.java_unbound.mod_menu.config.ModConfig;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class Toggle {
    public static void CreateToggleButton(String Tab, String Title, String Name, boolean DefaultValue) {
        Option<Boolean> ToggleOption = Option.<Boolean>createBuilder()
                .name(Component.literal(Title).withStyle(Style -> Style.withColor(0xFFFFFF)))
                .description(OptionDescription.of(Component.empty()))
                .binding(DefaultValue, () -> ModConfig.INSTANCE.GetBoolean(Name, DefaultValue), Value -> {
                    if (Name.equals("Clear Resource Pack Cache")) {
                        if (Value == true) {
                            Folder.ClearResourcePack();

                            JavaUnboundClient.Loading();

                            Minecraft.getInstance().execute(() -> Minecraft.getInstance().reloadResourcePacks());
                        }
                    } else {
                        ModConfig.INSTANCE.SetBoolean(Name, Value);
                        ConfigManager.Save();
                    }
                })
                .controller(Option -> BooleanControllerBuilder.create(Option).onOffFormatter().coloured(true))
                .build();

        ModMenuIntegration.GetCategory(Tab).option(ToggleOption);
    }
}