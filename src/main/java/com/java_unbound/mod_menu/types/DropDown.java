package com.java_unbound.mod_menu.types;

import com.java_unbound.mod_menu.ModMenuIntegration;
import com.java_unbound.mod_menu.config.ConfigManager;
import com.java_unbound.mod_menu.config.ModConfig;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.controller.DropdownStringControllerBuilder;
import net.minecraft.network.chat.Component;

import java.util.List;

public class DropDown {
    public static void CreateDropDownButton(String Tab, String Title, String Name, String DefaultValue, List<String> OptionNames, List<String> OptionValues) {
        TextLabelController.CreateTextLabel(Tab, Title);

        int DefaultIndex = OptionNames.indexOf(DefaultValue);
        if (DefaultIndex == -1) {return;}

        String DefaultText = OptionValues.get(DefaultIndex);

        Option<String> DropDownOption = Option.<String>createBuilder()
                .name(Component.literal(""))
                .description(OptionDescription.of(Component.empty()))
                .binding(DefaultText, () -> {
                            String CurrentValue = ModConfig.INSTANCE.GetString(Name, DefaultValue);

                            int CurrentIndex = OptionNames.indexOf(CurrentValue);
                            if (CurrentIndex == -1) {return DefaultText;}

                            return OptionValues.get(CurrentIndex);
                        },
                        Value -> {
                            int SelectedIndex = OptionValues.indexOf(Value);
                            if (SelectedIndex == -1) {return;}

                            ModConfig.INSTANCE.SetString(Name, OptionNames.get(SelectedIndex));

                            ConfigManager.Save();

                            if ("Description Color".equals(Name)) {
                                ModMenuIntegration.ReloadTab("Addon");
                            }
                        }
                )
                .controller(Option -> DropdownStringControllerBuilder.create(Option).values(OptionValues))
                .build();

        ModMenuIntegration.GetCategory(Tab).option(DropDownOption);
    }
}