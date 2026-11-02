package com.java_unbound.loader.gecko_lib.entity.rendering;

import com.geckolib.model.GeoModel;
import com.geckolib.renderer.GeoReplacedEntityRenderer;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.RenderPassInfo;

import com.java_unbound.loader.gecko_lib.entity.EntityRenderPass;
import com.java_unbound.loader.gecko_lib.entity.GenericReplacedEntity;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.Entity;

public class GenericReplacedEntityPassRenderer<R extends LivingEntityRenderState & GeoRenderState>
        extends GeoReplacedEntityRenderer<GenericReplacedEntity, Entity, R> {

    public GenericReplacedEntityPassRenderer(
            EntityRendererProvider.Context Context,
            EntityRenderPass Pass,
            GenericReplacedEntity Animatable,
            GeoModel<GenericReplacedEntity> AnimationModel,
            float ScaleWidth,
            float ScaleHeight
    ) {
        super(
                Context,
                new BedrockGeoModel(Pass, AnimationModel),
                Animatable
        );

        withScale(ScaleWidth, ScaleHeight);
    }

    @Override
    public void postRenderPass(
            RenderPassInfo<R> RenderPassInfo,
            SubmitNodeCollector RenderTasks
    ) {
    }
}
