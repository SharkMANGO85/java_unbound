package com.java_unbound.entities.geckolib.rendering;

import com.geckolib.animatable.GeoAnimatable;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.GeoReplacedEntityRenderer;
import com.geckolib.renderer.base.BoneSnapshots;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.GeoRenderer;
import com.geckolib.renderer.base.RenderPassInfo;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;

public class BedrockPassRenderer<T extends GeoAnimatable, E extends Entity, R extends EntityRenderState & GeoRenderState> extends GeoReplacedEntityRenderer<T, E, R> {
    private final GeoRenderer<T, E, R> ParentRenderer;

    public BedrockPassRenderer(EntityRendererProvider.Context Context, GeoModel<T> Model, T Animatable, GeoRenderer<T, E, R> ParentRenderer) {
        super(Context, Model, Animatable);

        this.ParentRenderer = ParentRenderer;
    }

    @Override
    public void preRenderPass(RenderPassInfo<R> RenderPassInfo, SubmitNodeCollector RenderTasks) {
        this.ParentRenderer.preRenderPass(RenderPassInfo, RenderTasks);
    }

    @Override
    public void scaleModelForRender(RenderPassInfo<R> RenderPassInfo, float WidthScale, float HeightScale) {
        this.ParentRenderer.scaleModelForRender(RenderPassInfo, WidthScale, HeightScale);
    }

    @Override
    public void adjustRenderPose(RenderPassInfo<R> RenderPassInfo) {
        this.ParentRenderer.adjustRenderPose(RenderPassInfo);
    }

    @Override
    public void adjustModelBonesForRender(RenderPassInfo<R> RenderPassInfo, BoneSnapshots Snapshots) {
        this.ParentRenderer.adjustModelBonesForRender(RenderPassInfo,Snapshots);
    }

    @Override
    public void applyAnimationControllers(RenderPassInfo<R> RenderPassInfo, BoneSnapshots Snapshots) {
        this.ParentRenderer.applyAnimationControllers(RenderPassInfo, Snapshots);
    }

    @Override
    public boolean firePreRenderEvent(RenderPassInfo<R> RenderPassInfo, SubmitNodeCollector RenderTasks) {
        return true;
    }

    @Override
    public void postRenderPass(RenderPassInfo<R> RenderPassInfo, SubmitNodeCollector RenderTasks) {

    }
}