package com.java_unbound.mixin.mod_menu;

import dev.isxander.yacl3.gui.controllers.dropdown.AbstractDropdownControllerElement;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(AbstractDropdownControllerElement.class)
public interface DropdownControllerElementAccessor {
    @Accessor("matchingValues")
    List<?> JavaUnbound$getMatchingValues();
}