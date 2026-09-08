package com.java_unbound.mixin.ui;

import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.TextAlignment;
import net.minecraft.client.gui.components.SplashRenderer;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import org.joml.Matrix3x2f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SplashRenderer.class)
public class SplashRendererMixin {
    @Shadow
    private net.minecraft.network.chat.Component splash;

    @Unique
    private static final float JAVA_UNBOUND_SCALE = 0.7F;

    @Unique
    private static final float JAVA_UNBOUND_X = 175.0F;

    @Unique
    private static final float JAVA_UNBOUND_Y = 85.0F;

    @Inject(method = "extractRenderState", at = @At("HEAD"), cancellable = true)
    private void javaUnbound$changeSplashPosition(GuiGraphicsExtractor graphics, int screenWidth, Font font, float alpha, CallbackInfo ci) {
        ci.cancel();

        int textWidth = font.width(this.splash);
        ActiveTextCollector textRenderer = graphics.textRenderer();

        float textPhase = 1.8F - Mth.abs(Mth.sin((float)(Util.getMillis() % 1000L) / 1000.0F * (float)(Math.PI * 2)) * 0.1F);
        float textScale = textPhase * 100.0F / (textWidth + 32) * JAVA_UNBOUND_SCALE;

        Matrix3x2f transform = new Matrix3x2f(textRenderer.defaultParameters().pose()).translate(screenWidth / 2.0F + JAVA_UNBOUND_X, JAVA_UNBOUND_Y).rotate((float)(-Math.PI / 9)).scale(textScale);
        ActiveTextCollector.Parameters renderParameters = textRenderer.defaultParameters().withOpacity(alpha).withPose(transform);

        textRenderer.accept(TextAlignment.LEFT, -textWidth / 2, -8, renderParameters, this.splash);
    }
}