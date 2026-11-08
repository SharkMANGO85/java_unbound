package com.java_unbound.attachables.rendering;

import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.Objects;

public record AttachableRenderPass(Identifier Geometry, Identifier Texture, List<PartVisibility> PartVisibility, EntityScale Scale) {
    public record PartVisibility(String BonePattern, boolean Visible) {}

    public record EntityScale(float X, float Y, float Z) {
        public static final EntityScale NONE = new EntityScale(1f, 1f, 1f);
    }

    public AttachableRenderPass {
        Objects.requireNonNull(Geometry, "Geometry cannot be null.");
        Objects.requireNonNull(Texture, "Texture cannot be null.");

        PartVisibility = PartVisibility == null ? List.of() : List.copyOf(PartVisibility);
        Scale = Scale == null ? EntityScale.NONE : Scale;
    }

    public AttachableRenderPass(Identifier Geometry, Identifier Texture, List<PartVisibility> PartVisibility) {
        this(Geometry, Texture, PartVisibility, EntityScale.NONE);
    }

    public AttachableRenderPass(Identifier Geometry, Identifier Texture) {
        this(Geometry, Texture, List.of(), EntityScale.NONE);
    }
}