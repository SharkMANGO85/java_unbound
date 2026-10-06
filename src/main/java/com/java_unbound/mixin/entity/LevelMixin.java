package com.java_unbound.mixin.entity;

import com.java_unbound.entities.actions.EntitySpawnHandler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerLevel.class)
public class LevelMixin {
    @Inject(method = "addFreshEntity", at = @At("RETURN"))
    private void OnAddFreshEntity(Entity Entity, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue()) {
            EntitySpawnHandler.OnEntitySpawned(Entity);
        }
    }
}