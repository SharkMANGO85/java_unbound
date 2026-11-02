package com.java_unbound.entities.geckolib.entities.cow;

import com.geckolib.renderer.GeoReplacedEntityRenderer;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.CowRenderState;
import net.minecraft.world.entity.animal.cow.Cow;

public class ReplacedCowRenderer<R extends CowRenderState & GeoRenderState> extends GeoReplacedEntityRenderer<ReplacedCowEntity, Cow, R> {
    public ReplacedCowRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new ReplacedCowModel(), new ReplacedCowEntity());

        withScale(0.5f, 0.5f);
    }
}