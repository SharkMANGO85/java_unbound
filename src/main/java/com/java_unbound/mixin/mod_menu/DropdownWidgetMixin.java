package com.java_unbound.mixin.mod_menu;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import dev.isxander.yacl3.api.utils.Dimension;
import dev.isxander.yacl3.gui.controllers.dropdown.AbstractDropdownControllerElement;
import dev.isxander.yacl3.gui.controllers.dropdown.DropdownWidget;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;

import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(DropdownWidget.class)
public abstract class DropdownWidgetMixin<T> {
    @Shadow
    @Final
    private AbstractDropdownControllerElement<T, ?> dropdownElement;

    @Shadow
    protected Dimension<Integer> dropdownDim;

    @Shadow
    protected int firstVisibleIndex;

    @Shadow
    public abstract int entryHeight();

    @Shadow
    public abstract int dropdownLength();

    @Shadow
    public abstract void selectVisibleItem(int VisibleIndex);

    @Shadow
    public abstract int selectedIndex();

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void JavaUnbound$SelectClickedOption(@NonNull MouseButtonEvent Event, boolean DoubleClick, CallbackInfoReturnable<Boolean> Callback) {
        if (!this.dropdownDim.isPointInside((int) Event.x(), (int) Event.y())) {return;}

        if (this.dropdownLength() == 0 || this.entryHeight() <= 0) {
            this.dropdownElement.unfocus();
            Callback.setReturnValue(true);
            return;
        }

        int VisibleIndex = (int) ((Event.y() - this.dropdownDim.y()) / this.entryHeight());

        this.selectVisibleItem(VisibleIndex);

        int SelectedIndex = this.selectedIndex();

        List<?> MatchingValues = ((DropdownControllerElementAccessor) (Object) this.dropdownElement).JavaUnbound$getMatchingValues();

        if (SelectedIndex >= 0 && SelectedIndex < MatchingValues.size()) {
            Object SelectedOption = MatchingValues.get(SelectedIndex);

            @SuppressWarnings("unchecked")
            AbstractDropdownControllerElement<T, Object> Element = (AbstractDropdownControllerElement<T, Object>) (Object) this.dropdownElement;
            String SelectedValue = Element.getString(SelectedOption);
            Element.modifyInput(Builder -> Builder.replace(0, Builder.length(), SelectedValue));
        }

        this.dropdownElement.unfocus();

        Callback.setReturnValue(true);
    }

    @Redirect(method = "extractRenderState", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blit"), require = 1)
    private void JavaUnbound$RenderOpaqueDropdownBackground(GuiGraphicsExtractor Graphics, RenderPipeline Pipeline, Identifier Texture, int X, int Y, float U, float V, int Width, int Height, int TextureWidth, int TextureHeight, int Color) {
        Graphics.fill(X, Y, X + Width, Y + Height, 0xFF202020);
    }
}