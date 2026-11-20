package com.java_unbound.mixin.mods.yacl;

import dev.isxander.yacl3.gui.controllers.dropdown.DropdownStringController;
import dev.isxander.yacl3.gui.controllers.dropdown.DropdownStringControllerElement;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(DropdownStringControllerElement.class)
public abstract class DropdownOptionsMixin {
    @Final
    @Shadow
    private DropdownStringController controller;

    /**
     * @author JavaUnbound
     * @reason Always display all available dropdown options.
     */

    @Overwrite
    public List<String> computeMatchingValues() {
        return this.controller.getAllowedValues("");
    }
}