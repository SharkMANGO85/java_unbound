package com.java_unbound.loader.gecko_lib.entity;

import net.minecraft.resources.Identifier;

import java.util.Objects;

public record EntityRenderPass(Identifier Geometry, Identifier Texture) {
    public EntityRenderPass {
        Objects.requireNonNull(Geometry, "Geometry cannot be null.");
        Objects.requireNonNull(Texture, "Texture cannot be null.");
    }
}
