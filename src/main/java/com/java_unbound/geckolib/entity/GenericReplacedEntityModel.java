package com.java_unbound.geckolib.entity;

import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.resources.Identifier;

public class GenericReplacedEntityModel extends GeoModel<GenericReplacedEntity> {
    private static final Identifier EMPTY_MODEL = Identifier.fromNamespaceAndPath("minecraft", "empty");
    private static final Identifier EMPTY_TEXTURE = Identifier.fromNamespaceAndPath("minecraft", "textures/misc/unknown_server.png");
    private static final Identifier ANIMATION = Identifier.fromNamespaceAndPath("minecraft", "animation.oreville_ans.test");

    @Override
    public Identifier getModelResource(GeoRenderState RenderState) {
        return EMPTY_MODEL;
    }

    @Override
    public Identifier getTextureResource(GeoRenderState RenderState) {
        return EMPTY_TEXTURE;
    }

    @Override
    public Identifier getAnimationResource(GenericReplacedEntity Animatable) {
        return ANIMATION;
    }
}