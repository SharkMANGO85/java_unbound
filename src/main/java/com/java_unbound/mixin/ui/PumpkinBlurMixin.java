package com.java_unbound.mixin.ui;

import com.google.gson.JsonPrimitive;
//import com.java_unbound.mod_menu.config.ConfigManager;
import com.java_unbound.mod_menu.config.ConfigManager;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Hud.class)
public abstract class PumpkinBlurMixin {
    private static final Identifier PUMPKIN_BLUR_TEXTURE = Identifier.fromNamespaceAndPath("minecraft", "textures/misc/pumpkinblur.png");

    private static final int TextureSize = 128;
    private static final int Last = TextureSize - 1;

    @Inject(method = "extractTextureOverlay", at = @At("HEAD"), cancellable = true)
    private void JavaUnbound$RenderPumpkinBlur(GuiGraphicsExtractor Graphics, Identifier Texture, float Alpha, CallbackInfo Callback) {
        if (!Texture.equals(PUMPKIN_BLUR_TEXTURE)) {return;}

        Object ScaleValue = ConfigManager.GetValue("SETTING", "Pumpkin Blur Texture Scale");

        float Scale = ScaleValue instanceof JsonPrimitive Primitive ? Primitive.getAsFloat() : ScaleValue instanceof Number Number ? Number.floatValue() : 1.5F;

        int ScreenWidth = Graphics.guiWidth();
        int ScreenHeight = Graphics.guiHeight();

        int Size = Math.toIntExact(Math.round(ScreenHeight * Scale));
        int X = (ScreenWidth - Size) / 2;
        int Y = (ScreenHeight - Size) / 2;
        int Right = X + Size;
        int Bottom = Y + Size;
        int Tint = ARGB.white(Alpha);

        if (X > 0) {
            Graphics.blit(RenderPipelines.GUI_TEXTURED, Texture, 0, Y, 0.0F, 0.0F, X, Size, 1, TextureSize, TextureSize, TextureSize, Tint);
            Graphics.blit(RenderPipelines.GUI_TEXTURED, Texture, Right, Y, (float) Last, 0.0F, ScreenWidth - Right, Size, 1, TextureSize, TextureSize, TextureSize, Tint);
        }

        if (Y > 0) {
            Graphics.blit(RenderPipelines.GUI_TEXTURED, Texture, X, 0, 0.0F, 0.0F, Size, Y, TextureSize, 1, TextureSize, TextureSize, Tint);
            Graphics.blit(RenderPipelines.GUI_TEXTURED, Texture, X, Bottom, 0.0F, (float) Last, Size, ScreenHeight - Bottom, TextureSize, 1, TextureSize, TextureSize, Tint);
        }

        if (X > 0 && Y > 0) {
            Graphics.blit(RenderPipelines.GUI_TEXTURED, Texture, 0, 0, 0.0F, 0.0F, X, Y, 1, 1, TextureSize, TextureSize, Tint);
            Graphics.blit(RenderPipelines.GUI_TEXTURED, Texture, Right, 0, (float) Last, 0.0F, ScreenWidth - Right, Y, 1, 1, TextureSize, TextureSize, Tint);
            Graphics.blit(RenderPipelines.GUI_TEXTURED, Texture, 0, Bottom, 0.0F, (float) Last, X, ScreenHeight - Bottom, 1, 1, TextureSize, TextureSize, Tint);
            Graphics.blit(RenderPipelines.GUI_TEXTURED, Texture, Right, Bottom, (float) Last, (float) Last, ScreenWidth - Right, ScreenHeight - Bottom, 1, 1, TextureSize, TextureSize, Tint);
        }

        Graphics.blit(RenderPipelines.GUI_TEXTURED, Texture, X, Y, 0.0F, 0.0F, Size, Size, TextureSize, TextureSize, TextureSize, TextureSize, Tint);

        Callback.cancel();
    }
}