package com.java_unbound.loader.gecko_lib.entity;

import com.geckolib.renderer.GeoReplacedEntityRenderer;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;

public class GenericReplacedEntityRenderer<R extends EntityRenderState & GeoRenderState> extends GeoReplacedEntityRenderer<GenericReplacedEntity, Entity, R> {

    public GenericReplacedEntityRenderer(EntityRendererProvider.Context context, Identifier model, Identifier texture, Identifier animation) {
        super(context, new GenericReplacedEntityModel(model, texture, animation), new GenericReplacedEntity());
    }

    @SuppressWarnings("unchecked")
    @Override
    public R createRenderState(GenericReplacedEntity animatable, Entity relatedObject) {
        return (R) (relatedObject instanceof net.minecraft.world.entity.LivingEntity ? new LivingEntityRenderState() : new EntityRenderState());
    }
}