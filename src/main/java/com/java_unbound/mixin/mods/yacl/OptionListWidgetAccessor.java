package com.java_unbound.mixin.mod_menu;

import dev.isxander.yacl3.gui.OptionListWidget;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(OptionListWidget.class)
public interface OptionListWidgetAccessor {
    @Accessor("scrollAmount")
    double JavaUnbound$getScrollAmount();
}