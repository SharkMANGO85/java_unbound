package com.java_unbound.mixin.mod_menu;

import com.java_unbound.mod_menu.types.Scrollbar;

import dev.isxander.yacl3.api.Controller;
import dev.isxander.yacl3.gui.controllers.ControllerWidget;

import net.minecraft.network.chat.Component;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ControllerWidget.class)
public abstract class ControllerWidgetMixin {
    @Shadow
    @Final
    protected Controller<?> control;

    @ModifyVariable(method = "extractRenderState", at = @At("STORE"), ordinal = 0)
    private Component JavaUnbound$UpdateSubpackTitle(Component Name) {
        if (!this.control.option().name().getString().equalsIgnoreCase("Subpack")) {return Name;}

        Object Value = this.control.option().pendingValue();
        if (!(Value instanceof Double SubpackValue)) {return Name;}

        String SubpackName = Scrollbar.GetSubpackName(SubpackValue);

        return Component.literal(SubpackName).setStyle(Name.getStyle());
    }
}
