package com.java_unbound.mixin.mod_menu;

import dev.isxander.yacl3.gui.controllers.dropdown.AbstractDropdownControllerElement;

import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;

import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractDropdownControllerElement.class)
public abstract class DropdownControllerMixin {
    @Inject(method = "charTyped", at = @At("HEAD"), cancellable = true)
    private void JavaUnbound$PreventTextInput(@NonNull CharacterEvent Event, CallbackInfoReturnable<Boolean> Callback) {
        Callback.setReturnValue(false);
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void JavaUnbound$PreventTextEditing(@NonNull KeyEvent Event, CallbackInfoReturnable<Boolean> Callback) {
        int Key = Event.key();

        if (Key == 259 || Key == 261 || Key == 262 || Key == 263) {
            Callback.setReturnValue(true);
        }
    }
}