package com.java_unbound.mixin.entity;

import com.java_unbound.entities.interfaces.LivingEntityRenderStateInterface;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(LivingEntityRenderState.class)
public class LivingEntityRenderStateMixin implements LivingEntityRenderStateInterface {
    @Unique
    private LivingEntity JavaUnbound$Entity;

    @Override
    public void JavaUnbound$SetEntity(LivingEntity Entity) {
        this.JavaUnbound$Entity = Entity;
    }

    @Override
    public LivingEntity JavaUnbound$GetEntity() {
        return this.JavaUnbound$Entity;
    }
}