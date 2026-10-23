package com.java_unbound.mixin.entity;

import com.java_unbound.entities.interfaces.LivingEntityRenderStateInterface;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererStateMixin {
    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void JavaUnbound$StoreEntity(LivingEntity Entity, LivingEntityRenderState State, float PartialTick, CallbackInfo CallbackInfo) {
        ((LivingEntityRenderStateInterface) State).JavaUnbound$SetEntity(Entity);
    }
}