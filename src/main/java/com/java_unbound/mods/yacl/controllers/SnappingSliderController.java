package com.java_unbound.mods.yacl.controllers;

import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.utils.Dimension;
import dev.isxander.yacl3.gui.AbstractWidget;
import dev.isxander.yacl3.gui.YACLScreen;
import dev.isxander.yacl3.gui.controllers.slider.DoubleSliderController;
import dev.isxander.yacl3.gui.controllers.slider.SliderControllerElement;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class SnappingSliderController extends DoubleSliderController {
    private static final double FineInterval = 0.0001;

    private final double SnapStep;

    public SnappingSliderController(Option<Double> Option, double Minimum, double Maximum, double SnapStep) {
        super(Option, Minimum, Maximum, FineInterval, Value -> Component.empty());

        this.SnapStep = SnapStep;
    }

    private void Snap() {
        double Current = option().pendingValue();
        double Snapped = Math.round(Current / SnapStep) * SnapStep;

        Snapped = Math.clamp(Snapped, min(), max());

        if (Snapped != Current) {
            option().requestSet(Snapped);
        }
    }

    @Override
    public AbstractWidget provideWidget(YACLScreen Screen, Dimension<Integer> WidgetDimension) {
        return new SliderControllerElement(this, Screen, WidgetDimension, min(), max(), FineInterval) {

            @Override
            public void extractRenderState(GuiGraphicsExtractor Graphics, int MouseX, int MouseY, float A) {
                super.extractRenderState(Graphics, MouseX, MouseY, A);

                if (!isHovered()) {
                    extractHoveredControl(Graphics, MouseX, MouseY, A);
                }
            }

            @Override
            protected int getControlWidth() {
                return getHoveredControlWidth();
            }

            @Override
            protected int getUnhoveredControlWidth() {
                return 0;
            }

            @Override
            public void setFocused(boolean Focused) {
                super.setFocused(Focused);
                if (!Focused) Snap();
            }

            @Override
            public boolean mouseReleased(MouseButtonEvent Event) {
                boolean Result = super.mouseReleased(Event);

                Snap();

                return Result;
            }
        };
    }
}