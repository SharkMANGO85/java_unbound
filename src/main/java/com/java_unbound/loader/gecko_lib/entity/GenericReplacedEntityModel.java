package com.java_unbound.loader.gecko_lib.entity;

import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.resources.Identifier;

public class GenericReplacedEntityModel extends GeoModel<GenericReplacedEntity> {
    private final Identifier model;
    private final Identifier texture;
    private final Identifier animation;

    public GenericReplacedEntityModel(Identifier model, Identifier texture, Identifier animation) {
        this.model = model;
        this.texture = texture;
        this.animation = animation;
    }

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return this.model;
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return this.texture;
    }

    @Override
    public Identifier getAnimationResource(GenericReplacedEntity animatable) {
        return this.animation;
    }
}