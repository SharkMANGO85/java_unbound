package com.java_unbound.entities.geckolib.entities.creeper;

import com.geckolib.model.DefaultedEntityGeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.resources.Identifier;

public class ReplacedCreeperModel extends DefaultedEntityGeoModel<ReplacedCreeperEntity> {
    public ReplacedCreeperModel() {
        super(Identifier.fromNamespaceAndPath("minecraft", "geometry.oreville_ans.gvwagv"));
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath("minecraft", "textures/oreville/ans/cro.png");
    }
}