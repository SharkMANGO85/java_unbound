package com.java_unbound.mixin.attachable;

import com.java_unbound.attachables.Renderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandRendererMixin {
    @Inject(method = "submitHandsWithItems", at = @At("HEAD"), cancellable = true)
    private void CancelVanillaHands(float FrameInterp, PoseStack PoseStack, SubmitNodeCollector SubmitNodeCollector, LocalPlayer Player, int LightCoords, CallbackInfo CallbackInfo) {
        ItemStack MainHand = Player.getMainHandItem();
        if (!Renderer.HasRenderer(MainHand.getItem())) return;

        CallbackInfo.cancel();

        PoseStack.pushPose();
        PoseStack.translate(0.0F, 0.0F, -1.0F);

        Renderer.Render(MainHand.getItem(), PoseStack, SubmitNodeCollector, LightCoords);

        PoseStack.popPose();
    }
}