package com.java_unbound.mixin.textures.main_screen;

import com.java_unbound.JavaUnbound;
import com.java_unbound.shared.resources.ResourceUtilities;
import com.java_unbound.shared.resources.textures.main_screen.TitleLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.LogoRenderer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;

@Mixin(LogoRenderer.class)
public class TitleRendererMixin {
    @Final
    @Shadow
    private boolean keepLogoThroughFade;

    @Unique
    private static final Identifier LOGO = Identifier.fromNamespaceAndPath("minecraft", "textures/gui/title/minecraft.png");

    @Unique
    private static int CachedWidth = -1;

    @Unique
    private static int CachedHeight = -1;

    @Unique
    private static boolean SizeFailed = false;

    @Unique
    private static boolean ResolveSize() {
        if (CachedWidth > 0) return true;
        if (SizeFailed) return false;

        try {
            var Resource = Minecraft.getInstance().getResourceManager().getResource(LOGO);
            if (Resource.isEmpty()) {
                JavaUnbound.LOGGER.error("Logo resource not found: {}", LOGO);

                SizeFailed = true;

                return false;
            }

            try (InputStream Stream = Resource.get().open()) {
                BufferedImage Image = ImageIO.read(Stream);

                if (Image == null) {
                    JavaUnbound.LOGGER.error("Could not decode logo image!");
                    SizeFailed = true;
                    return false;
                }

                CachedWidth = Image.getWidth();
                CachedHeight = Image.getHeight();

                return true;
            }
        } catch (Exception Exception) {
            JavaUnbound.LOGGER.error("Failed to read logo size", Exception);

            SizeFailed = true;

            return false;
        }
    }

    @Inject(method = "extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IFI)V", at = @At("HEAD"), cancellable = true)
    private void ExtractRenderState(final GuiGraphicsExtractor Graphics, final int Width, final float Alpha, final int HeightOffset, final CallbackInfo CallbackInfo) {
        if (!JavaUnbound.TITLE_VISIBLE) {
            CallbackInfo.cancel();
            return;
        }

        if (!ResolveSize()) {return;}

        int TextureWidth = CachedWidth;
        int TextureHeight = CachedHeight;

        float LogoScale = JavaUnbound.TITLE_SCALE;
        float LogoHeight = 44.0f * LogoScale;
        float LogoWidth = LogoHeight * TextureWidth / TextureHeight;

        float MaxWidth = Width * 0.9f;

        if (LogoWidth > MaxWidth) {
            LogoWidth = MaxWidth;
            LogoHeight = LogoWidth * TextureHeight / TextureWidth;
        }

        int LogoX = Math.round(Width / 2.0f - LogoWidth / 2.0f);
        int Color = ARGB.white(this.keepLogoThroughFade ? 1.0f : Alpha);

        Graphics.blit(RenderPipelines.GUI_TEXTURED, LOGO, LogoX, HeightOffset, 0.0F, 0.0f, Math.round(LogoWidth), Math.round(LogoHeight), TextureWidth, TextureHeight, TextureWidth, TextureHeight, Color);
        CallbackInfo.cancel();
    }
}