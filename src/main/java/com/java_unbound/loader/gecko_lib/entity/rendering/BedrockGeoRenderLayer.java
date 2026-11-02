package com.java_unbound.loader.gecko_lib.entity.rendering;

import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.GeoRenderer;
import com.geckolib.renderer.base.RenderPassInfo;
import com.geckolib.renderer.layer.GeoRenderLayer;

import com.java_unbound.loader.gecko_lib.entity.EntityRenderPass;
import com.java_unbound.loader.gecko_lib.entity.GenericReplacedEntity;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.Entity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BedrockGeoRenderLayer<R extends LivingEntityRenderState & GeoRenderState>
        extends GeoRenderLayer<GenericReplacedEntity, Entity, R> {

    private final EntityRendererProvider.Context Context;
    private final GenericReplacedEntity Animatable;
    private final GeoModel<GenericReplacedEntity> AnimationModel;
    private final List<EntityRenderPass> Passes;
    private final float ScaleWidth;
    private final float ScaleHeight;

    private final Map<EntityRenderPass, GenericReplacedEntityPassRenderer<R>> PassRenderers = new HashMap<>();

    public BedrockGeoRenderLayer(
            GeoRenderer<GenericReplacedEntity, Entity, R> Renderer,
            EntityRendererProvider.Context Context,
            GenericReplacedEntity Animatable,
            List<EntityRenderPass> Passes,
            float ScaleWidth,
            float ScaleHeight
    ) {
        super(Renderer);

        this.Context = Context;
        this.Animatable = Animatable;
        this.AnimationModel = Renderer.getGeoModel();
        this.Passes = List.copyOf(Passes);
        this.ScaleWidth = ScaleWidth;
        this.ScaleHeight = ScaleHeight;
    }

    private GenericReplacedEntityPassRenderer<R> GetPassRenderer(EntityRenderPass Pass) {
        return this.PassRenderers.computeIfAbsent(
                Pass,
                Key -> new GenericReplacedEntityPassRenderer<>(
                        this.Context,
                        Key,
                        this.Animatable,
                        this.AnimationModel,
                        this.ScaleWidth,
                        this.ScaleHeight
                )
        );
    }

    @Override
    public void submitRenderTask(
            RenderPassInfo<R> RenderPassInfo,
            SubmitNodeCollector RenderTasks
    ) {
        PoseStack PoseStack = RenderPassInfo.poseStack();

        for (EntityRenderPass Pass : this.Passes) {
            PoseStack.pushPose();

            try {
                PoseStack.last().set(RenderPassInfo.getPreRenderMatrixPose());

                GetPassRenderer(Pass).submit(
                        RenderPassInfo.renderState(),
                        PoseStack,
                        RenderTasks,
                        RenderPassInfo.cameraState()
                );
            } finally {
                PoseStack.popPose();
            }
        }
    }
}
