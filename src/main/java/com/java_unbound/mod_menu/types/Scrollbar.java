
package com.java_unbound.mod_menu.types;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import com.java_unbound.loader.ui.textures.AnimatedTexture;
import com.java_unbound.mixin.ui.PumpkinBlurMixin;
import com.java_unbound.mod_menu.ModMenuIntegration;
import com.java_unbound.mod_menu.config.ConfigManager;
import com.java_unbound.mod_menu.config.ModConfig;

import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.controller.DoubleSliderControllerBuilder;

import net.minecraft.network.chat.Component;

public class Scrollbar {
    private static List<String> SubpackNames = new ArrayList<>();

    public static void CreateSubpackScrollbarButton(String Tab, List<String> Names, List<String> SubpackFolders) {
        if (Names.isEmpty() || Names.size() != SubpackFolders.size()) {return;}

        SubpackNames = new ArrayList<>(Names);

        double Step = Names.size() > 1 ? 1.0 / (Names.size() - 1) : 1.0;

        Option<Double> ScrollbarOption = Option.<Double>createBuilder()
                .name(Component.literal("Subpack"))
                .description(OptionDescription.of(Component.empty()))
                .binding(0.0, () -> ModConfig.INSTANCE.GetDouble("Subpack", 0.0), Value -> {ModConfig.INSTANCE.SetDouble("Subpack", Value); ConfigManager.Save();})
                .controller(Option -> DoubleSliderControllerBuilder.create(Option).range(0.0, 1.0).step(Step).valueFormatter(Value -> Component.empty()))
                .build();

        ModMenuIntegration.GetCategory(Tab).option(ScrollbarOption);
    }

    public static void CreateScrollbarButton(String Tab, String Name, String DisplayName, double DefaultValue, double Minimum, double Maximum, double Step) {
        if (ModMenuIntegration.GetCategory(Tab) == null || Minimum > Maximum) {return;}

        DefaultValue = Math.clamp(DefaultValue, Minimum, Maximum);

        double FinalDefaultValue = DefaultValue;
        int Decimals = Math.max(0, BigDecimal.valueOf(Step).stripTrailingZeros().scale());

        Option<Double> ScrollbarOption = Option.<Double>createBuilder()
                .name(Component.literal(DisplayName))
                .description(OptionDescription.of(Component.empty()))
                .binding(FinalDefaultValue, () -> ModConfig.INSTANCE.GetDouble(Name, FinalDefaultValue), Value -> {
                    double Rounded = BigDecimal.valueOf(Value).setScale(Decimals, RoundingMode.HALF_UP).doubleValue();

                    ModConfig.INSTANCE.SetDouble(Name, Rounded); ConfigManager.Save();
                })
                .controller(Option -> DoubleSliderControllerBuilder.create(Option).range(Minimum, Maximum).step(Step).valueFormatter(Value -> Component.literal(BigDecimal.valueOf(Value).setScale(Decimals, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString())))
                .build();

        ModMenuIntegration.GetCategory(Tab).option(ScrollbarOption);
    }

    public static String GetSubpackName(double Value) {
        if (SubpackNames.isEmpty()) {return "Subpack";}

        int Index = (int) Math.round(Value * (SubpackNames.size() - 1));
        Index = Math.clamp(Index, 0, SubpackNames.size() - 1);

        return SubpackNames.get(Index).replaceAll("§[0-9a-fk-or]", "");
    }
}
