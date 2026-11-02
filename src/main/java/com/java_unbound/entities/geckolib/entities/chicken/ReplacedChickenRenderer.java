package com.java_unbound.entities.geckolib.entities.chicken;

import com.geckolib.renderer.GeoReplacedEntityRenderer;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.ChickenRenderState;
import net.minecraft.world.entity.animal.chicken.Chicken;

public class ReplacedChickenRenderer<R extends ChickenRenderState & GeoRenderState> extends GeoReplacedEntityRenderer<ReplacedChickenEntity, Chicken, R> {
    public ReplacedChickenRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new ReplacedChickenModel(), new ReplacedChickenEntity());
    }
}