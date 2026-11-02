package com.java_unbound.loader.gecko_lib.entity;

import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.renderer.GeoReplacedEntityRenderer;
import com.geckolib.renderer.base.GeoRenderState;

import com.google.common.reflect.TypeToken;

import com.java_unbound.entities.geckolib.EntityRendererRegistration;
import com.java_unbound.loader.gecko_lib.entity.rendering.BedrockGeoRenderLayer;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;

import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.function.Function;

public class GenericReplacedEntityRenderer<R extends LivingEntityRenderState & GeoRenderState> extends GeoReplacedEntityRenderer<GenericReplacedEntity, Entity, R> {
    public static final DataTicket<List<EntityRenderPass>> RENDER_PASSES = DataTicket.create("java_unbound_render_passes", new TypeToken<>() {});

    private final Function<Entity, List<EntityRenderPass>> PassProvider;
    private final float ScaleWidth;
    private final float ScaleHeight;

    public GenericReplacedEntityRenderer(EntityRendererProvider.Context Context, Function<Entity, List<EntityRenderPass>> PassProvider, float ScaleWidth, float ScaleHeight) {
        super(Context, new GenericReplacedEntityModel(new EntityRenderPass(Identifier.fromNamespaceAndPath("minecraft", "geometry.empty"), Identifier.fromNamespaceAndPath("minecraft", "textures/misc/unknown_server.png"))), new GenericReplacedEntity());

        this.PassProvider = PassProvider;
        this.ScaleWidth = ScaleWidth;
        this.ScaleHeight = ScaleHeight;

        withScale(this.ScaleWidth, this.ScaleHeight);
        withRenderLayer(Renderer -> new BedrockGeoRenderLayer<>(Renderer, Context, this.animatable, this.ScaleWidth, this.ScaleHeight));
    }

    @Override
    public void addRenderData(GenericReplacedEntity Animatable, Entity RelatedEntity, R RenderState, float PartialTick) {
        List<EntityRenderPass> Passes = this.PassProvider.apply(RelatedEntity);

        RenderState.addGeckolibData(RENDER_PASSES, Passes);
    }

    @Override
    public @Nullable RenderType getRenderType(R RenderState, Identifier Texture) {
        return null;
    }
}