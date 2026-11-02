package com.java_unbound.entities.geckolib.entities.copper_golem;

import com.geckolib.renderer.GeoReplacedEntityRenderer;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.CopperGolemRenderState;
import net.minecraft.world.entity.animal.golem.CopperGolem;

public class ReplacedCopperGolemRenderer<R extends CopperGolemRenderState & GeoRenderState> extends GeoReplacedEntityRenderer<ReplacedCopperGolemEntity, CopperGolem, R> {
    public ReplacedCopperGolemRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new ReplacedCopperGolemModel(), new ReplacedCopperGolemEntity());
    }
}