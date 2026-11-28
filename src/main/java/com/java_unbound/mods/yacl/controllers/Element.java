package com.java_unbound.mods.yacl.controllers;

import com.java_unbound.shared.resources.ResourceUpdater;
import com.java_unbound.shared.resources.types.UIResourceUpdater;
import com.mojang.blaze3d.platform.InputConstants;
import dev.isxander.yacl3.api.utils.Dimension;
import dev.isxander.yacl3.gui.YACLScreen;
import dev.isxander.yacl3.gui.controllers.ControllerWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

public class Element extends ControllerWidget<TexturedToggleController> {
    public Element(TexturedToggleController Control, YACLScreen Screen, Dimension<Integer> Dimension) {
        super(Control, Screen, Dimension);
    }

    private int GetIconHeight() {
        return Math.max(1, getDimension().height() - 4);
    }

    private int GetIconWidth() {
        Identifier Texture = UIResourceUpdater.TOGGLE_ENABLED;

        var TextureInfo = Minecraft.getInstance().getTextureManager().getTexture(Texture);

        int OriginalWidth = TextureInfo.getTexture().getWidth(1);
        int OriginalHeight = TextureInfo.getTexture().getHeight(1);

        return Math.max(1, GetIconHeight() * OriginalWidth / OriginalHeight);
    }

    private Identifier GetCurrentTexture(int MouseX, int MouseY) {
        boolean Enabled = control.option().pendingValue();
        boolean Hovered = isAvailable() && (isMouseOver(MouseX, MouseY) || focused);

        if (Enabled) {
            return Hovered ? UIResourceUpdater.TOGGLE_ENABLED_HOVERED : UIResourceUpdater.TOGGLE_ENABLED;
        }

        return Hovered ? UIResourceUpdater.TOGGLE_DISABLED_HOVERED : UIResourceUpdater.TOGGLE_DISABLED;
    }

    @Override
    protected void extractValueText(GuiGraphicsExtractor Graphics, int MouseX, int MouseY, float Delta) {
        int IconWidth = GetIconWidth();
        int IconHeight = GetIconHeight();

        int X = getDimension().xLimit() - getXPadding() - IconWidth;
        int Y = getDimension().centerY() - IconHeight / 2;

        Graphics.blit(RenderPipelines.GUI_TEXTURED, GetCurrentTexture(MouseX, MouseY), X, Y, 0.0F, 0.0F, IconWidth, IconHeight, IconWidth, IconHeight);
    }

    @Override
    protected int getHoveredControlWidth() {
        return GetIconWidth();
    }

    private void Toggle() {
        control.option().requestSet(!control.option().pendingValue());
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent Event, boolean DoubleClick) {
        if (!isMouseOver(Event.x(), Event.y()) || !isAvailable()) return false;

        Toggle();
        playDownSound();

        return true;
    }

    @Override
    public boolean keyPressed(KeyEvent Event) {
        if (!focused || !isAvailable()) return false;

        int Key = Event.key();

        if (Key == InputConstants.KEY_RETURN || Key == InputConstants.KEY_SPACE || Key == InputConstants.KEY_NUMPADENTER) {
            Toggle();
            playDownSound();
            return true;
        }

        return false;
    }
}