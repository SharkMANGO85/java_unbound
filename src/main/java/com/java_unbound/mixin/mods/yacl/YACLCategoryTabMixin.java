package com.java_unbound.mixin.mod_menu;

import dev.isxander.yacl3.gui.OptionListWidget;
import dev.isxander.yacl3.gui.WidgetAndType;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(targets = "dev.isxander.yacl3.gui.YACLScreen$CategoryTab")
public abstract class YACLCategoryTabMixin {
    @Shadow
    @Final
    private WidgetAndType<OptionListWidget> optionList;

    @Shadow
    @Final
    public Button saveFinishedButton;

    @Shadow
    @Final
    public Button cancelResetButton;

    @Shadow
    @Final
    public Button undoButton;

    @Inject(method = "visitChildren", at = @At("HEAD"), cancellable = true)
    private void javaUnbound$hideRightPanel(Consumer<AbstractWidget> consumer, CallbackInfo ci) {
        consumer.accept(optionList.getWidget());
        consumer.accept(saveFinishedButton);

        ci.cancel();
    }

    @Inject(method = "doLayout", at = @At("HEAD"), cancellable = true)
    private void javaUnbound$expandSettingsList(ScreenRectangle tabArea, CallbackInfo ci) {
        int BottomPadding = 26;

        optionList.getWidget().setX(tabArea.left());
        optionList.getWidget().setY(tabArea.top());
        optionList.getWidget().setWidth(tabArea.width());
        optionList.getWidget().setHeight(tabArea.height() - BottomPadding);

        saveFinishedButton.setWidth(80);
        saveFinishedButton.setHeight(20);
        saveFinishedButton.setX(tabArea.left() + (tabArea.width() - saveFinishedButton.getWidth()) / 2);
        saveFinishedButton.setY(tabArea.top() + tabArea.height() - 22);

        ci.cancel();
    }


    @Inject(method = "doLayout", at = @At("HEAD"), cancellable = true)
    private void javaUnbound$centerSettingsContainer(ScreenRectangle tabArea, CallbackInfo ci) {
        int ContainerWidth = Math.min(600, tabArea.width());
        int ContainerHeight = tabArea.height() - 26;
        int ContainerX = tabArea.left() + (tabArea.width() - ContainerWidth) / 2;

        optionList.getWidget().setX(ContainerX);
        optionList.getWidget().setY(tabArea.top());
        optionList.getWidget().setWidth(ContainerWidth);
        optionList.getWidget().setHeight(ContainerHeight);

        int ButtonWidth = 80;
        int ButtonGap = 4;
        int TotalWidth = ButtonWidth * 3 + ButtonGap * 2;

        int startX = ContainerX + (ContainerWidth - TotalWidth) / 2;
        int buttonY = tabArea.top() + tabArea.height() - 22;

        saveFinishedButton.setWidth(ButtonWidth);
        saveFinishedButton.setHeight(20);
        saveFinishedButton.setX(startX);
        saveFinishedButton.setY(buttonY);

        cancelResetButton.setWidth(ButtonWidth);
        cancelResetButton.setHeight(20);
        cancelResetButton.setX(startX + ButtonWidth + ButtonGap);
        cancelResetButton.setY(buttonY);

        undoButton.setWidth(ButtonWidth);
        undoButton.setHeight(20);
        undoButton.setX(startX + (ButtonWidth + ButtonGap) * 2);
        undoButton.setY(buttonY);

        ci.cancel();
    }

    @Inject(method = "renderBackground", at = @At("HEAD"), cancellable = true)
    private void javaUnbound$hideRightPanelBackground(net.minecraft.client.gui.GuiGraphicsExtractor graphics, CallbackInfo ci) {
        ci.cancel();
    }
}