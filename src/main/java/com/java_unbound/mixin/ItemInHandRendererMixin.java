package com.java_unbound.mixin;

import com.java_unbound.loader.attachables.FirstPersonAttachableRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandRendererMixin {
    @Inject(method = "submitHandsWithItems", at = @At("HEAD"), cancellable = true)
    private void JavaUnbound$RenderHands(float TickDelta, PoseStack PoseStack, SubmitNodeCollector Collector, LocalPlayer Player, int Light, CallbackInfo CallbackInfo) {
        if (!FirstPersonAttachableRenderer.HasAttachable(Player.getMainHandItem())) return;
        CallbackInfo.cancel();
        FirstPersonAttachableRenderer.Render(TickDelta, PoseStack, Collector, Player, Light);
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void JavaUnbound$PreventEquipAnimation(CallbackInfo CallbackInfo) {
        CallbackInfo.cancel();
    }
}