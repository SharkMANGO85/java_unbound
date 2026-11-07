package com.java_unbound.geckolib.entity;

import com.geckolib.animatable.GeoReplacedEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.util.GeckoLibUtil;

public final class GenericReplacedEntity implements GeoReplacedEntity {
    private final AnimatableInstanceCache Cache = GeckoLibUtil.createInstanceCache(this);

    /*private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.oreville_ans.xghxmf");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("animation.oreville_ans.qdpwhx");
    private static final RawAnimation RUN = RawAnimation.begin().thenLoop("animation.oreville_ans.aeakyu");
    private static final RawAnimation JUMP = RawAnimation.begin().thenPlay("animation.oreville_ans.pdefav");
    private static final RawAnimation FALL = RawAnimation.begin().thenLoop("animation.oreville_ans.vraein");
    private static final RawAnimation SIT = RawAnimation.begin().thenLoop("animation.oreville_ans.qtwewe");

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar Controllers) {
        Controllers.add(new AnimationController<GenericReplacedEntity>("Locomotion", 0, this::Locomotion));
    }

    private PlayState Locomotion(AnimationTest<GenericReplacedEntity> State) {
        System.out.println("sit=" + State.getData(GenericReplacedEntityData.SITTING) + " jump=" + State.getData(GenericReplacedEntityData.JUMPING) + " fall=" + State.getData(GenericReplacedEntityData.FALLING) + " run=" + State.getData(GenericReplacedEntityData.RUNNING) + " walk=" + State.getData(GenericReplacedEntityData.WALK_SPEED));

        if (State.getData(GenericReplacedEntityData.SITTING)) {
            return State.setAndContinue(SIT);
        }

        if (State.getData(GenericReplacedEntityData.JUMPING)) {
            return State.setAndContinue(JUMP);
        }

        if (State.getData(GenericReplacedEntityData.FALLING)) {
            return State.setAndContinue(FALL);
        }

        if (State.getData(GenericReplacedEntityData.WALK_SPEED) > 0.5f) {
            return State.setAndContinue(RUN);
        }

        if (State.getData(GenericReplacedEntityData.WALK_SPEED) > 0.01f) {
            return State.setAndContinue(WALK);
        }

        return State.setAndContinue(IDLE);
    }*/

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {

    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.Cache;
    }
}