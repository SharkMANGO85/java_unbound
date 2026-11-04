package com.java_unbound.mixin.mod_menu;

import dev.isxander.yacl3.gui.OptionListWidget;
import dev.isxander.yacl3.gui.YACLScreen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(OptionListWidget.class)
public abstract class YACLScreenMixin {
    @Shadow
    @Final
    private YACLScreen yaclScreen;

    @Inject(method = "getRowLeft", at = @At("HEAD"), cancellable = true)
    private void JavaUnbound$CenterRowLeft(CallbackInfoReturnable<Integer> Callback) {
        OptionListWidget Widget = (OptionListWidget) (Object) this;

        int Width = Widget.getRowWidth();

        Callback.setReturnValue((this.yaclScreen.width - Width) / 2);
    }
}