package com.java_unbound.loader.gecko_lib.entity;

import com.geckolib.animatable.GeoReplacedEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.util.GeckoLibUtil;

public final class GenericReplacedEntity implements GeoReplacedEntity {
    private final AnimatableInstanceCache Cache = GeckoLibUtil.createInstanceCache(this);

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar Controllers) {}

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.Cache;
    }
}
