package com.java_unbound.loader.gecko_lib.entity;

import com.geckolib.model.DefaultedEntityGeoModel;
import com.geckolib.renderer.base.GeoRenderState;

import net.minecraft.resources.Identifier;

public class GenericReplacedEntityModel extends DefaultedEntityGeoModel<GenericReplacedEntity> {
    private final Identifier Texture;

    public GenericReplacedEntityModel(EntityRenderPass Pass) {
        super(Pass.Geometry());

        this.Texture = Pass.Texture();
    }

    @Override
    public Identifier getTextureResource(GeoRenderState RenderState) {
        return this.Texture;
    }
}
