package com.java_unbound.mixin.entity;

import com.java_unbound.loader.entities.JavaUnboundEntity;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Entity.class)
public class EntityMixin implements JavaUnboundEntity {
    @Unique
    private boolean JavaUnbound$JavaUnbound;

    @Override
    public boolean JavaUnbound$IsJavaUnbound() {
        return this.JavaUnbound$JavaUnbound;
    }

    @Override
    public void JavaUnbound$SetJavaUnbound(boolean Value) {
        this.JavaUnbound$JavaUnbound = Value;
    }
}