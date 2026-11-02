package com.java_unbound.entities.geckolib.entities.slime;

import com.geckolib.model.DefaultedEntityGeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.resources.Identifier;

public class ReplacedSlimeModel extends DefaultedEntityGeoModel<ReplacedSlimeEntity> {
    public ReplacedSlimeModel() {
        super(Identifier.fromNamespaceAndPath("minecraft", "geometry.oreville_ans.igrcna"));
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath("minecraft", "textures/oreville/ans/cmh.png");
    }
}