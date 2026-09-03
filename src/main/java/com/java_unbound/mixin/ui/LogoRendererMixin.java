package com.java_unbound.mixin.ui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.LogoRenderer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.util.ARGB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LogoRenderer.class)
public class LogoRendererMixin {
    @Shadow
    private boolean keepLogoThroughFade;

    @Inject(method = "extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IFI)V", at = @At("HEAD"), cancellable = true)
    private void ExtractRenderState(final GuiGraphicsExtractor Graphics, final int Width, final float Alpha, final int HeightOffset, final CallbackInfo CallbackInfo) {
        float EffectiveAlpha = this.keepLogoThroughFade ? 1.0F : Alpha;
        int Color = ARGB.white(EffectiveAlpha);

        final int TextureWidth = 2374;
        final int TextureHeight = 403;

        final int LogoHeight = 50;
        final int LogoWidth = Math.round((float) LogoHeight * TextureWidth / TextureHeight);

        final int LogoX = Width / 2 - LogoWidth / 2;

        Graphics.blit(RenderPipelines.GUI_TEXTURED, LogoRenderer.MINECRAFT_LOGO, LogoX, HeightOffset, 0.0F, 0.0F, LogoWidth, LogoHeight, TextureWidth, TextureHeight, TextureWidth, TextureHeight, Color);

        CallbackInfo.cancel();
    }
}