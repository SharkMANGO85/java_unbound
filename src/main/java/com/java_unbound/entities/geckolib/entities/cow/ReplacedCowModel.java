package com.java_unbound.entities.geckolib.entities.cow;

import com.geckolib.model.DefaultedEntityGeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.resources.Identifier;

public class ReplacedCowModel extends DefaultedEntityGeoModel<ReplacedCowEntity> {
    public ReplacedCowModel() {
        super(Identifier.fromNamespaceAndPath("minecraft", "geometry.oreville_ans.tqwpec"));
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath("minecraft", "textures/oreville/ans/dak.png");
    }
}