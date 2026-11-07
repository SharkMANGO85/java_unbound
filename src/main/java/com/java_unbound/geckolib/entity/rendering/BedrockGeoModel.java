
package com.java_unbound.geckolib.entity.rendering;

import com.geckolib.model.DefaultedEntityGeoModel;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;

import com.java_unbound.geckolib.entity.EntityRenderPass;
import com.java_unbound.geckolib.entity.GenericReplacedEntity;

import com.java_unbound.loader.resourcepack.PackLoader;
import net.minecraft.resources.Identifier;

import org.jspecify.annotations.Nullable;

public class BedrockGeoModel extends DefaultedEntityGeoModel<GenericReplacedEntity> {
    private final Identifier Texture;
    private final Identifier Animation;
    private final GeoModel<GenericReplacedEntity> AnimationModel;

    public BedrockGeoModel(EntityRenderPass Pass, GeoModel<GenericReplacedEntity> AnimationModel) {
        super(Pass.Geometry());

        this.Texture = Pass.Texture();
        this.AnimationModel = AnimationModel;
        this.Animation = Identifier.fromNamespaceAndPath(PackLoader.PackID, "entity/oreville_ans");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState RenderState) {
        return this.Texture;
    }

    @Override
    public Identifier getAnimationResource(GenericReplacedEntity Animatable) {
        return this.Animation;
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
