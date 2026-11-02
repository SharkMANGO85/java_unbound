package com.java_unbound.loader.gecko_lib.entity.rendering;

import com.geckolib.model.DefaultedEntityGeoModel;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;

import com.java_unbound.loader.gecko_lib.entity.EntityRenderPass;
import com.java_unbound.loader.gecko_lib.entity.GenericReplacedEntity;

import net.minecraft.resources.Identifier;

import org.jspecify.annotations.Nullable;

public class BedrockGeoModel extends DefaultedEntityGeoModel<GenericReplacedEntity> {
    private final Identifier Texture;
    private final GeoModel<GenericReplacedEntity> AnimationModel;

    public BedrockGeoModel(EntityRenderPass Pass, GeoModel<GenericReplacedEntity> AnimationModel) {
        super(Pass.Geometry());

        this.Texture = Pass.Texture();
        this.AnimationModel = AnimationModel;
    }

    @Override
    public Identifier getTextureResource(GeoRenderState RenderState) {
        return this.Texture;
    }

    @Override
    public Identifier getAnimationResource(GenericReplacedEntity Animatable) {
        return this.AnimationModel.getAnimationResource(Animatable);
    }

    @Override
    public Identifier[] getAnimationResourceFallbacks(GenericReplacedEntity Animatable) {
        return this.AnimationModel.getAnimationResourceFallbacks(Animatable);
    }

    @Override
    public void addAdditionalStateData(GenericReplacedEntity Animatable, @Nullable Object RelatedObject, GeoRenderState RenderState) {
        this.AnimationModel.addAdditionalStateData(Animatable, RelatedObject, RenderState);
    }
}
