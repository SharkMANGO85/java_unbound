package com.java_unbound.entities.geckolib.entities.cow;

import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.GeoReplacedEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.util.GeckoLibUtil;

/**
 * Replacement {@link net.minecraft.world.entity.monster.Creeper Creeper} {@link GeoEntity} to showcase
 * replacing the model and animations of an existing entity
 */

public class ReplacedCowEntity implements GeoReplacedEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        //controllers.add(DefaultAnimations.genericWalkIdleController());
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}