package com.java_unbound.mixin.attachable;

import com.java_unbound.JavaUnbound;
import com.java_unbound.attachables.Renderer;
import com.java_unbound.molang.types.MolangAttachableParser;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandRendererMixin {
    @Shadow private ItemStack mainHandItem;
    @Shadow private ItemStack offHandItem;
    @Shadow private float mainHandHeight;
    @Shadow private float oMainHandHeight;
    @Shadow private float offHandHeight;
    @Shadow private float oOffHandHeight;

    @Inject(method = "submitHandsWithItems", at = @At("HEAD"), cancellable = true)
    private void CancelVanillaHands(float FrameInterp, PoseStack PoseStack, SubmitNodeCollector SubmitNodeCollector, LocalPlayer Player, int LightCoords, CallbackInfo CallbackInfo) {
        ItemStack MainHand = Player.getMainHandItem();
        ItemStack OffHand = Player.getOffhandItem();

        this.mainHandItem = MainHand;
        this.offHandItem = OffHand;
        this.mainHandHeight = 1.0F;
        this.oMainHandHeight = 1.0F;
        this.offHandHeight = 1.0F;
        this.oOffHandHeight = 1.0F;

        boolean MainCustom = !MainHand.isEmpty() && Renderer.HasRenderer(MainHand.getItem());
        boolean OffCustom = !OffHand.isEmpty() && Renderer.HasRenderer(OffHand.getItem());
        if (!MainCustom && !OffCustom) return;

        CallbackInfo.cancel();

        if (MainCustom) {
            MolangAttachableParser.Context MainContext = MolangAttachableParser.ForHeld(Player, MainHand, MolangAttachableParser.Slot.MAIN_HAND, true);
            MolangAttachableParser.Swap(MainContext);

            try {
                Renderer.Render(MainContext, PoseStack, SubmitNodeCollector, LightCoords);
            } finally {
                MolangAttachableParser.Swap(null);
            }
        }

        if (OffCustom) {
            MolangAttachableParser.Context OffContext = MolangAttachableParser.ForHeld(Player, OffHand, MolangAttachableParser.Slot.OFF_HAND, true);
            MolangAttachableParser.Swap(OffContext);

            try {
                Renderer.Render(OffContext, PoseStack, SubmitNodeCollector, LightCoords);
            } finally {
                MolangAttachableParser.Swap(null);
            }
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void NoEquipAnimation(CallbackInfo CallbackInfo) {
        LocalPlayer Player = net.minecraft.client.Minecraft.getInstance().player;
        if (Player == null) return;

        this.mainHandItem = Player.getMainHandItem();
        this.offHandItem = Player.getOffhandItem();
        this.mainHandHeight = 1.0F;
        this.oMainHandHeight = 1.0F;
        this.offHandHeight = 1.0F;
        this.oOffHandHeight = 1.0F;
    }
}