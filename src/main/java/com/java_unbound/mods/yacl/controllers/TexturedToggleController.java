package com.java_unbound.mods.yacl.controllers;

import com.java_unbound.shared.resources.ResourceUtilities;
import dev.isxander.yacl3.api.Controller;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.utils.Dimension;
import dev.isxander.yacl3.gui.AbstractWidget;
import dev.isxander.yacl3.gui.YACLScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class TexturedToggleController implements Controller<Boolean> {
    private final Option<Boolean> Option;

    public TexturedToggleController(Option<Boolean> Option) {
        this.Option = Option;
    }

    @Override
    public Option<Boolean> option() {
        return Option;
    }

    @Override
    public Component formatValue() {
        return Component.literal(Option.pendingValue() ? "Enabled" : "Disabled");
    }

    @Override
    public AbstractWidget provideWidget(YACLScreen Screen, Dimension<Integer> WidgetDimension) {
        return new Element(this, Screen, WidgetDimension);
    }
}