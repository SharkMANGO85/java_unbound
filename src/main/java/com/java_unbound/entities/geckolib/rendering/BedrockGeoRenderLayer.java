package com.java_unbound.entities.geckolib.rendering;

import com.geckolib.animatable.GeoAnimatable;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.GeoRenderer;
import com.geckolib.renderer.base.RenderPassInfo;
import com.geckolib.renderer.layer.GeoRenderLayer;

import com.java_unbound.loader.definitions.entity.types.render_controller.EntityRenderPass;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;

import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BedrockGeoRenderLayer<T extends GeoAnimatable, E extends Entity, R extends EntityRenderState & GeoRenderState> extends GeoRenderLayer<T, E, R> {
    @FunctionalInterface
    public interface PassProvider<E extends Entity> {
        List<EntityRenderPass> GetActiveRenderPasses(E Entity, float PartialTick);
    }

    private final EntityRendererProvider.Context Context;
    private final T Animatable;
    private final PassProvider<E> PassProvider;

    private final Map<EntityRenderPass, BedrockPassRenderer<T, E, R>> Renderers = new HashMap<>();

    public BedrockGeoRenderLayer(GeoRenderer<T, E, R> Renderer, EntityRendererProvider.Context Context, T Animatable, PassProvider<E> PassProvider) {
        super(Renderer);

        this.Context = Context;
        this.Animatable = Animatable;
        this.PassProvider = PassProvider;
    }

    @Override
    public void addRenderData(T Animatable, @Nullable E RelatedObject, R RenderState, float PartialTick) {
        if (RelatedObject == null) {
            RenderState.addGeckolibData(BedrockRenderData.ActiveRenderPasses, BedrockRenderPassData.EMPTY);
            return;
        }

        List<EntityRenderPass> Passes = this.PassProvider.GetActiveRenderPasses(RelatedObject, PartialTick);

        RenderState.addGeckolibData(BedrockRenderData.ActiveRenderPasses, new BedrockRenderPassData(Passes == null ? List.of() : Passes));
    }

    private BedrockPassRenderer<T, E, R> GetRenderer(EntityRenderPass Pass) {
        return this.Renderers.computeIfAbsent(Pass, Key -> {
            GeoModel<T> Model = new BedrockGeoModel<>(Key.Geometry(), Key.Texture(), this.renderer.getGeoModel());

            return new BedrockPassRenderer<>(this.Context, Model, this.Animatable, this.renderer);
        });
    }

    @Override
    public void submitRenderTask(RenderPassInfo<R> RenderPassInfo, SubmitNodeCollector RenderTasks) {
        BedrockRenderPassData PassData = RenderPassInfo.getOrDefaultGeckolibData(BedrockRenderData.ActiveRenderPasses, BedrockRenderPassData.EMPTY);
        if (PassData.Passes().isEmpty()) {return;}

        PoseStack PoseStack = RenderPassInfo.poseStack();

        for (EntityRenderPass Pass : PassData.Passes()) {
            PoseStack.pushPose();

            try {
                PoseStack.last().set(RenderPassInfo.getPreRenderMatrixPose());
                GetRenderer(Pass).submit(RenderPassInfo.renderState(), PoseStack, RenderTasks, RenderPassInfo.cameraState());
            } finally {
                PoseStack.popPose();
            }
        }
    }
}