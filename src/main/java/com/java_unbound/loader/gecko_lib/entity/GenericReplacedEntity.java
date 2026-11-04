package com.java_unbound.loader.gecko_lib.entity;

import com.geckolib.animatable.GeoReplacedEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.object.PlayState;
import com.geckolib.animation.state.AnimationTest;
import com.geckolib.util.GeckoLibUtil;

public final class GenericReplacedEntity implements GeoReplacedEntity {
    private final AnimatableInstanceCache Cache = GeckoLibUtil.createInstanceCache(this);

    private static final RawAnimation RUN = RawAnimation.begin().thenLoop("animation.oreville_ans.aeakyu");

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<GenericReplacedEntity>("Locomotion", 4, this::locomotion));
    }

    private PlayState locomotion(AnimationTest<GenericReplacedEntity> state) {
        if (state.isMoving()) {
            return state.setAndContinue(RUN);
        }

        return PlayState.STOP;
    }

    private PlayState rotation(AnimationTest<GenericReplacedEntity> state) {
        return PlayState.STOP;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.Cache;
    }
}