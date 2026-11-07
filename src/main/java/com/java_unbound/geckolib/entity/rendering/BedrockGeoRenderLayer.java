package com.java_unbound.geckolib.entity.rendering;

import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.GeoRenderer;
import com.geckolib.renderer.base.RenderPassInfo;
import com.geckolib.renderer.layer.GeoRenderLayer;

import com.java_unbound.geckolib.entity.EntityRenderPass;
import com.java_unbound.geckolib.entity.GenericReplacedEntity;
import com.java_unbound.geckolib.entity.GenericReplacedEntityRenderer;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BedrockGeoRenderLayer<R extends LivingEntityRenderState & GeoRenderState> extends GeoRenderLayer<GenericReplacedEntity, Entity, R> {
    private record PassKey(Identifier Geometry, Identifier Texture, List<EntityRenderPass.PartVisibility> PartVisibility) {}

    private final EntityRendererProvider.Context Context;
    private final GenericReplacedEntity Animatable;
    private final GeoModel<GenericReplacedEntity> AnimationModel;
    private final float ScaleWidth;
    private final float ScaleHeight;

    private final Map<PassKey, GenericReplacedEntityPassRenderer<R>> PassRenderers = new HashMap<>();

    public BedrockGeoRenderLayer(GeoRenderer<GenericReplacedEntity, Entity, R> Renderer, EntityRendererProvider.Context Context, GenericReplacedEntity Animatable, float ScaleWidth, float ScaleHeight) {
        super(Renderer);

        this.Context = Context;
        this.Animatable = Animatable;
        this.AnimationModel = Renderer.getGeoModel();
        this.ScaleWidth = ScaleWidth;
        this.ScaleHeight = ScaleHeight;
    }

    private GenericReplacedEntityPassRenderer<R> GetPassRenderer(EntityRenderPass Pass) {
        PassKey Key = new PassKey(Pass.Geometry(), Pass.Texture(), Pass.PartVisibility());

        return this.PassRenderers.computeIfAbsent(Key, K -> new GenericReplacedEntityPassRenderer<>(this.Context, Pass, this.Animatable, this.AnimationModel, this.ScaleWidth, this.ScaleHeight));
    }

    @Override
    public void submitRenderTask(RenderPassInfo<R> RenderPassInfo, SubmitNodeCollector RenderTasks) {
        List<EntityRenderPass> Passes = RenderPassInfo.renderState().getGeckolibData(GenericReplacedEntityRenderer.RENDER_PASSES);
        if (Passes == null || Passes.isEmpty()) {return;}

        PoseStack PoseStack = RenderPassInfo.poseStack();

        for (EntityRenderPass Pass : Passes) {
            PoseStack.pushPose();

            try {
                PoseStack.last().set(RenderPassInfo.getPreRenderMatrixPose());

                EntityRenderPass.EntityScale Scale = Pass.Scale();
                PoseStack.scale(Scale.X(), Scale.Y(), Scale.Z());

                GetPassRenderer(Pass).submit(RenderPassInfo.renderState(), PoseStack, RenderTasks, RenderPassInfo.cameraState());
            } finally {
                PoseStack.popPose();
            }
        }
    }
}