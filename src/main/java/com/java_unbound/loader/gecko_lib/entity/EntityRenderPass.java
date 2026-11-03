package com.java_unbound.loader.gecko_lib.entity;

import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.Objects;

public record EntityRenderPass(Identifier Geometry, Identifier Texture, List<PartVisibility> PartVisibility) {
    public record PartVisibility(String BonePattern, boolean Visible) { }

    public EntityRenderPass {
        Objects.requireNonNull(Geometry, "Geometry cannot be null.");
        Objects.requireNonNull(Texture, "Texture cannot be null.");

        PartVisibility = PartVisibility == null ? List.of() : List.copyOf(PartVisibility);
    }

    public EntityRenderPass(Identifier Geometry, Identifier Texture) {
        this(Geometry, Texture, List.of());
    }
}