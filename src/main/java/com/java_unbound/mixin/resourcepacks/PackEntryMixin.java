package com.java_unbound.mixin.resourcepacks;

import com.java_unbound.mods.yacl.resource_pack.ResourcePackScreen;
import com.java_unbound.shared.definitions.resource_pack.ResourcePackDefinition;
import com.java_unbound.shared.loader.resourcepacks.ResourcePackLoader;
import com.java_unbound.shared.resources.ResourceUtilities;
import com.java_unbound.shared.resources.types.UIResourceUpdater;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.packs.PackSelectionModel;
import net.minecraft.client.gui.screens.packs.TransferableSelectionList;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TransferableSelectionList.PackEntry.class)
public abstract class PackEntryMixin {
    @Shadow
    @Final
    private PackSelectionModel.Entry pack;

    @Unique private Button javaUnbound$Button;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void javaUnbound$Initialize(CallbackInfo ci) {
        String PackId = this.pack.getId();
        if (!ResourcePackLoader.Packs.containsKey(PackId)) {return;}

        ResourcePackDefinition Definition = ResourcePackLoader.GetPack(PackId);
        if (Definition == null) {return;}

        if (Definition.Manifest.Settings.isEmpty()) {
            return;
        }

        this.javaUnbound$Button = Button.builder(Component.empty(), Button -> {
            Minecraft MinecraftInstance = Minecraft.getInstance();
            MinecraftInstance.gui.setScreen(ResourcePackScreen.ToggleScreen(PackId, MinecraftInstance.gui.screen()));
        }).bounds(0, 0, 20, 20).build();
    }

    @Inject(method = "extractContent", at = @At("TAIL"))
    private void javaUnbound$Extract(GuiGraphicsExtractor Graphics, int MouseX, int MouseY, boolean Hovered, float Alpha, CallbackInfo CallbackInfo) {
        if (this.javaUnbound$Button == null) {return;}

        TransferableSelectionList.PackEntry Self = (TransferableSelectionList.PackEntry) (Object) this;

        this.javaUnbound$Button.setPosition(Self.getContentRight() - 22, Self.getContentY() + 6);

        Identifier SettingsIcon = UIResourceUpdater.SETTINGS_ICON;
        if (SettingsIcon == null) {return;}

        Graphics.blit(RenderPipelines.GUI_TEXTURED, SettingsIcon, this.javaUnbound$Button.getX() + 2, this.javaUnbound$Button.getY() + 2, 0.0F, 0.0F, 16, 16, 16, 16);
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void javaUnbound$Clicked(MouseButtonEvent Event, boolean DoubleClick, CallbackInfoReturnable<Boolean> CallbackInfoReturnable) {
        if (this.javaUnbound$Button == null) {return;}

        if (this.javaUnbound$Button.mouseClicked(Event, DoubleClick)) {
            CallbackInfoReturnable.setReturnValue(true);
        }
    }
}