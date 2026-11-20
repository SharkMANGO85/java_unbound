
package com.java_unbound.mod_menu.types;

import com.java_unbound.mod_menu.ModMenuIntegration;

import dev.isxander.yacl3.api.Controller;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.controller.ControllerBuilder;
import dev.isxander.yacl3.api.utils.Dimension;
import dev.isxander.yacl3.gui.AbstractWidget;
import dev.isxander.yacl3.gui.YACLScreen;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

public class TextLabelController {
    public static void CreateTextLabel(String Tab, String Text) {
        Option<String> LabelOption = Option.<String>createBuilder().name(Component.empty()).description(OptionDescription.of(Component.empty())).binding(Text, () -> Text, Value -> {})
                .controller(Option -> (ControllerBuilder<String>) () -> new Controller<String>() {
                    @Override
                    public Option<String> option() {
                        return Option;
                    }

                    @Override
                    public Component formatValue() {
                        return Component.literal(Text);
                    }

                    @Override
                    public AbstractWidget provideWidget(YACLScreen Screen, Dimension<Integer> Dimension) {
                        return new TextLabelWidget(Dimension, Text);
                    }
                })
                .build();

        ModMenuIntegration.GetCategory(Tab).option(LabelOption);
    }

    private static class TextLabelWidget extends AbstractWidget {
        private final String Text;

        public TextLabelWidget(Dimension<Integer> Dimension, String Text) {
            super(Dimension);

            this.Text = Text;
        }

        @Override
        public void setFocused(boolean focused) {}

        @Override
        public boolean isFocused() {
            return false;
        }

        @Override
        public void extractRenderState(GuiGraphicsExtractor Graphics, int MouseX, int MouseY, float Delta) {
            Graphics.text(this.textRenderer, this.Text, this.getDimension().x(), this.getDimension().y() + 5, 0xFFFFFFFF);
        }
    }
}
