package com.java_unbound.mods.yacl.types;

import com.google.gson.JsonElement;
import com.java_unbound.mods.mod_menu.ModMenuIntegration;
import com.java_unbound.mods.yacl.ModScreen;
import com.java_unbound.shared.definitions.manifest.ManifestSettingsDefinition;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.controller.DropdownStringControllerBuilder;
import net.minecraft.network.chat.Component;

public class Dropdown {
    public static void AddonCreateDropdownButton(ManifestSettingsDefinition Defintion) {
        TextLabel.CreateTextLabel("Addon", Defintion.Text);

        Option<String> DropDownOption = Option.<String>createBuilder()
                .name(Component.literal(""))
                .description(OptionDescription.of(Component.empty()))
                .binding("HI", () -> {
                           return "HI";
                        },
                        Value -> {

                        }
                )
                .controller(Option -> DropdownStringControllerBuilder.create(Option).values())
                .build();

        ModScreen.GetCategory("Addon").option(DropDownOption);
    }
}
