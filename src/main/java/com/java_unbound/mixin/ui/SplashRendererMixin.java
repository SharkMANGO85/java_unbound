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

//adjust the splashes
@Mixin(SplashRenderer.class)
public class SplashRendererMixin {
    @Shadow
    private net.minecraft.network.chat.Component splash;

    @Unique
    private static final float JAVA_UNBOUND_SCALE = 0.5F;

    @Unique
    private static final float JAVA_UNBOUND_X = 205.0F;

    @Unique
    private static final float JAVA_UNBOUND_Y = 90.0F;

    @Inject(method = "extractRenderState", at = @At("HEAD"), cancellable = true)
    private void javaUnbound$changeSplashPosition(GuiGraphicsExtractor graphics, int screenWidth, Font font, float alpha, CallbackInfo ci) {
        ci.cancel();

        int TextWidth = font.width(this.splash);
        ActiveTextCollector textRenderer = graphics.textRenderer();

        float TextPhase = 1.8F - Mth.abs(Mth.sin((float)(Util.getMillis() % 1000L) / 1000.0F * (float)(Math.PI * 2)) * 0.1F);
        float TextScale = TextPhase * 100.0F / (TextWidth + 32) * JAVA_UNBOUND_SCALE;

        Matrix3x2f Transform = new Matrix3x2f(textRenderer.defaultParameters().pose()).translate(screenWidth / 2.0F + JAVA_UNBOUND_X, JAVA_UNBOUND_Y).rotate((float)(-Math.PI / 9)).scale(TextScale);
        ActiveTextCollector.Parameters renderParameters = textRenderer.defaultParameters().withOpacity(alpha).withPose(Transform);

        textRenderer.accept(TextAlignment.LEFT, -TextWidth / 2, -8, renderParameters, this.splash);
    }
}