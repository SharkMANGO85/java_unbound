package com.java_unbound.entities.geckolib.entities.slime;

import com.geckolib.renderer.GeoReplacedEntityRenderer;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.SlimeRenderState;
import net.minecraft.world.entity.monster.cubemob.Slime;

public class ReplacedSlimeRenderer<R extends SlimeRenderState & GeoRenderState> extends GeoReplacedEntityRenderer<ReplacedSlimeEntity, Slime, R> {
    public ReplacedSlimeRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new ReplacedSlimeModel(), new ReplacedSlimeEntity());

        withScale(2.0f, 2.0f);
    }
}