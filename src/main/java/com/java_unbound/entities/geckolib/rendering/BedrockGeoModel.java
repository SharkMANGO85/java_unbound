package com.java_unbound.entities.geckolib.rendering;

import com.geckolib.animatable.GeoAnimatable;
import com.geckolib.model.DefaultedEntityGeoModel;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;

import net.minecraft.resources.Identifier;

public class BedrockGeoModel<T extends GeoAnimatable> extends DefaultedEntityGeoModel<T> {
    private final Identifier Texture;
    private final GeoModel<T> AnimationModel;

    public BedrockGeoModel(String Geometry, Identifier Texture, GeoModel<T> AnimationModel) {
        super(Identifier.fromNamespaceAndPath("minecraft", Geometry));

        this.Texture = Texture;
        this.AnimationModel = AnimationModel;
    }

    @Override
    public Identifier getTextureResource(GeoRenderState RenderState) {
        return this.Texture;
    }

    @Override
    public Identifier getAnimationResource(T Animatable) {
        return this.AnimationModel.getAnimationResource(Animatable);
    }

    @Override
    public Identifier[] getAnimationResourceFallbacks(T Animatable) {
        return this.AnimationModel.getAnimationResourceFallbacks(Animatable);
    }
}