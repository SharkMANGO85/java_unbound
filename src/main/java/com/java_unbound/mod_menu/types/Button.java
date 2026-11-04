package com.java_unbound.mod_menu.types;

import com.java_unbound.mod_menu.ModMenuIntegration;
import dev.isxander.yacl3.api.ButtonOption;
import dev.isxander.yacl3.api.OptionDescription;
import net.minecraft.network.chat.Component;

public class Button {
    public static void CreateToggleButton(String Tab, String Name, String Action) {
        ButtonOption ButtonOptionValue = ButtonOption.createBuilder()
                .name(Component.literal(Name))
                .description(OptionDescription.of(Component.literal(Name)))
                .text(Component.literal(Action))
                .action((Screen, Button) -> {})
                .build();

        ModMenuIntegration.GetCategory(Tab).option(ButtonOptionValue);
    }
}