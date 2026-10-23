package com.java_unbound.mixin.mod_menu;

import dev.isxander.yacl3.gui.controllers.dropdown.DropdownStringController;
import dev.isxander.yacl3.gui.controllers.dropdown.DropdownStringControllerElement;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(DropdownStringControllerElement.class)
public abstract class DropdownOptionsMixin {
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