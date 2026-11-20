
package com.java_unbound.mixin.mod_menu;

import dev.isxander.yacl3.gui.OptionListWidget;
import dev.isxander.yacl3.gui.WidgetAndType;
import dev.isxander.yacl3.gui.YACLScreen;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(YACLScreen.CategoryTab.class)
public interface YACLCategoryTabAccessor {
    @Accessor("optionList")
    WidgetAndType<OptionListWidget> JavaUnbound$getOptionList();
}
