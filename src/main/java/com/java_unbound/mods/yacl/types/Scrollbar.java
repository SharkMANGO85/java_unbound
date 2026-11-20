package com.java_unbound.mods.yacl.types;

import com.java_unbound.JavaUnbound;
import com.java_unbound.mods.yacl.ModScreen;
import com.java_unbound.shared.definitions.manifest.ManifestDefinition;
import com.java_unbound.shared.definitions.manifest.ManifestSettingsDefinition;
import com.java_unbound.shared.definitions.manifest.ManifestSubpackDefinition;
import com.java_unbound.shared.loaders.manifest.ManifestLoader;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.controller.DoubleSliderControllerBuilder;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;

public class Scrollbar {
    public static void AddonCreateScrollbarOption(ManifestSettingsDefinition Defintion) {

    }

    public static void SubpackCreateScrollbarOption(ManifestDefinition Defintion) {
        ArrayList<Object> SubpackNames = new ArrayList<>();

        for (ManifestSubpackDefinition SubpackDefinition : ManifestLoader.Manifest.Subpacks) {
            SubpackNames.add(SubpackDefinition.Name);
        }

        double Step = SubpackNames.size() > 1 ? 1.0 / (SubpackNames.size() - 1) : 1.0;

        Option<Double> ScrollbarOption = Option.<Double>createBuilder()
                .name(Component.literal("Subpack"))
                .description(OptionDescription.of(Component.empty()))
                .binding(0.0, () -> 0.5, Value -> {JavaUnbound.LOGGER.error("d");;})
                .controller(Option -> DoubleSliderControllerBuilder.create(Option).range(0.0, 1.0).step(Step).valueFormatter(Value -> Component.empty()))
                .build();

        ModScreen.GetCategory("Addon").option(ScrollbarOption);
    }
}
