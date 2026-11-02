package com.java_unbound.entities.geckolib.rendering;

import com.java_unbound.loader.definitions.entity.types.render_controller.EntityRenderPass;

import java.util.List;

public record BedrockRenderPassData(List<EntityRenderPass> Passes) {
    public static final BedrockRenderPassData EMPTY = new BedrockRenderPassData(List.of());

    public BedrockRenderPassData {
        Passes = List.copyOf(Passes);
    }
}