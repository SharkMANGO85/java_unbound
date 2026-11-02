package com.java_unbound.entities.geckolib.entities.copper_golem;

import com.geckolib.model.DefaultedEntityGeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.resources.Identifier;

public class ReplacedCopperGolemModel extends DefaultedEntityGeoModel<ReplacedCopperGolemEntity> {
    public ReplacedCopperGolemModel() {
        super(Identifier.fromNamespaceAndPath("minecraft", "geometry.oreville_ans.uzvlke"));
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath("minecraft", "textures/oreville/ans/bkc.png");
    }
}