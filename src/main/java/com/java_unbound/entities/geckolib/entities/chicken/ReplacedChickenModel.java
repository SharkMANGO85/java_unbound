package com.java_unbound.entities.geckolib.entities.chicken;

import com.geckolib.model.DefaultedEntityGeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.resources.Identifier;

public class ReplacedChickenModel extends DefaultedEntityGeoModel<ReplacedChickenEntity> {
    public ReplacedChickenModel() {
        super(Identifier.fromNamespaceAndPath("minecraft", "geometry.oreville_ans.zchplq"));
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath("minecraft", "textures/oreville/ans/oah.png");
    }
}