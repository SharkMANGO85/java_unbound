package com.java_unbound.entities.geckolib.entities.creeper;

import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.GeoReplacedEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.util.GeckoLibUtil;

public class ReplacedCreeperEntity implements GeoReplacedEntity {
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