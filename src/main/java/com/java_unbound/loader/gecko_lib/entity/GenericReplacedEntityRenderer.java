package com.java_unbound.loader.gecko_lib.entity;

import com.geckolib.renderer.GeoReplacedEntityRenderer;
import com.geckolib.renderer.base.GeoRenderState;

import com.java_unbound.loader.gecko_lib.entity.rendering.BedrockGeoRenderLayer;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;

import org.jspecify.annotations.Nullable;

import java.util.List;

public class GenericReplacedEntityRenderer<R extends LivingEntityRenderState & GeoRenderState> extends GeoReplacedEntityRenderer<GenericReplacedEntity, Entity, R> {
    private final float ScaleWidth;
    private final float ScaleHeight;

    public GenericReplacedEntityRenderer(EntityRendererProvider.Context Context, List<EntityRenderPass> Passes, float ScaleWidth, float ScaleHeight) {
        super(Context, new GenericReplacedEntityModel(GetFirstPass(Passes)), new GenericReplacedEntity());

        this.ScaleWidth = ScaleWidth;
        this.ScaleHeight = ScaleHeight;

        withScale(this.ScaleWidth, this.ScaleHeight);
        withRenderLayer(Renderer -> new BedrockGeoRenderLayer<>(Renderer, Context, this.animatable, Passes, this.ScaleWidth, this.ScaleHeight));
    }

    private static EntityRenderPass GetFirstPass(List<EntityRenderPass> Passes) {
        if (Passes == null || Passes.isEmpty()) {
            throw new IllegalArgumentException("At least one geometry/texture render pass is required.");
        }

        return Passes.get(0);
    }

    @Override
    public @Nullable RenderType getRenderType(R RenderState, Identifier Texture) {
        return null;
    }
}
