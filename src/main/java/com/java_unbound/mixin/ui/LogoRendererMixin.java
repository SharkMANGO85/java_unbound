package com.java_unbound.mixin.ui;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.LogoRenderer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.util.ARGB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;
import java.io.InputStream;

@Mixin(LogoRenderer.class)
public class LogoRendererMixin {
    @Shadow
    private boolean keepLogoThroughFade;

    @Inject(method = "extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IFI)V", at = @At("HEAD"), cancellable = true)
    private void ExtractRenderState(final GuiGraphicsExtractor Graphics, final int Width, final float Alpha, final int HeightOffset, final CallbackInfo CallbackInfo) {
        float EffectiveAlpha = this.keepLogoThroughFade ? 1.0F : Alpha;
        int Color = ARGB.white(EffectiveAlpha);

        Identifier Logo = LogoRenderer.MINECRAFT_LOGO;

        int ImageWidth;
        int ImageHeight;

        try {
            Resource Resource = Minecraft.getInstance().getResourceManager().getResource(Logo).orElseThrow();

            try (InputStream InputStream = Resource.open()) {
                NativeImage Image = NativeImage.read(InputStream);

                ImageWidth = Image.getWidth();
                ImageHeight = Image.getHeight();

                Image.close();
            }
        } catch (IOException Exception) {
            CallbackInfo.cancel();
            return;
        }

        int LogoWidth = Width / 2;
        int LogoHeight = Math.round((float) LogoWidth * ImageHeight / ImageWidth);
        int LogoX = (Width - LogoWidth) / 2;

        Graphics.blit(RenderPipelines.GUI_TEXTURED, Logo, LogoX, HeightOffset, 0.0F, 0.0F, LogoWidth, LogoHeight, ImageWidth, ImageHeight, ImageWidth, ImageHeight, Color);

        CallbackInfo.cancel();
    }
}