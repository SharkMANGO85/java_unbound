package com.java_unbound.loader.definitions.global.materials.rendering;

import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;

public interface BedrockRenderMaterial {
    RenderType GetRenderType(Identifier Texture);

    boolean IsFullBright();
}