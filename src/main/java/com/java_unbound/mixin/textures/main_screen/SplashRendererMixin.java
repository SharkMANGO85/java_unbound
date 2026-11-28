package com.java_unbound.mixin.textures.main_screen;

import com.java_unbound.JavaUnbound;
import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.TextAlignment;
import net.minecraft.client.gui.components.SplashRenderer;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import org.joml.Matrix3x2f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(SplashRenderer.class)
public class SplashRendererMixin {
    @Final
    @Shadow
    private net.minecraft.network.chat.Component splash;

    @Unique
    private List<String> JavaUnbound$WrapText(String Text) {
        List<String> Lines = new ArrayList<>();
        StringBuilder Line = new StringBuilder();

        final int Max_Characters = JavaUnbound.SPLASH_MAXIMUM_CHARACTERS;

        for (String Word : Text.split("\\s+")) {
            if (!Line.isEmpty() && Line.length() + Word.length() + 1 > Max_Characters) {
                Lines.add(Line.toString());
                Line.setLength(0);
            }

            if (!Line.isEmpty()) {
                Line.append(' ');
            }

            Line.append(Word);
        }

        if (!Line.isEmpty()) {
            Lines.add(Line.toString());
        }

        return Lines;
    }

    @Inject(method = "extractRenderState", at = @At("HEAD"), cancellable = true)
    private void JavaUnbound$changeSplashPosition(GuiGraphicsExtractor Graphics, int ScreenWidth, Font Font, float Alpha, CallbackInfo CallbackInfo) {
        if (!JavaUnbound.SPLASH_VISIBLE) {
            CallbackInfo.cancel();
            return;
        }

        float Scale = JavaUnbound.SPLASH_SCALE;
        float Positon_X = JavaUnbound.SPLASH_POSITION_X;
        float Positon_Y = JavaUnbound.SPLASH_POSITION_Y;

        ActiveTextCollector TextRenderer = Graphics.textRenderer();

        float TextPhase = 1.8F - Mth.abs(Mth.sin((float)(Util.getMillis() % 1000L) / 1000.0f * (float)(Math.PI * 2)) * 0.1f);
        float TextScale = TextPhase * (ScreenWidth / 1920.0f) * Scale;

        Matrix3x2f Transform = new Matrix3x2f(TextRenderer.defaultParameters().pose()).translate(ScreenWidth / 2.0f + Positon_X, Positon_Y).rotate((float)(-Math.PI / 9)).scale(TextScale);
        ActiveTextCollector.Parameters RenderParameters = TextRenderer.defaultParameters().withOpacity(Alpha).withPose(Transform);
        List<String> Lines = JavaUnbound$WrapText(this.splash.getString());

        for (int Index = 0; Index < Lines.size(); Index++) {
            String Line = Lines.get(Index);
            int TextWidth = Font.width(Line);

            TextRenderer.accept(TextAlignment.LEFT, -TextWidth / 2, -8 + Index * 10, RenderParameters, net.minecraft.network.chat.Component.literal(Line));
        }

        CallbackInfo.cancel();
    }
}