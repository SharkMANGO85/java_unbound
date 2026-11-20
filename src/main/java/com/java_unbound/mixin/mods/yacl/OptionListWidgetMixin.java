package com.java_unbound.mixin.mod_menu;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.isxander.yacl3.gui.OptionListWidget;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(OptionListWidget.class)
public abstract class OptionListWidgetMixin {
    @ModifyReturnValue(method = "getRowWidth", at = @At("RETURN"))
    private int JavaUnbound$IncreaseRowWidth(int Width) {
        return Width + 100;
    }
}