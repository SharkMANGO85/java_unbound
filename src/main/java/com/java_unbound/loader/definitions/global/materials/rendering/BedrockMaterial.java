package com.java_unbound.loader.definitions.global.materials.rendering;

import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

import java.util.Locale;

public enum BedrockMaterial implements BedrockRenderMaterial {
    SOLID, CUTOUT, CUTOUT_CULL, TRANSLUCENT, TRANSLUCENT_CULL, EYES, EMISSIVE_OPAQUE_CULL;;

    public static BedrockMaterial Parse(String Name) {
        if (Name == null) {return CUTOUT;}

        return switch (Name.toLowerCase(Locale.ROOT)) {
            case "solid" -> SOLID;
            case "cutout_cull" -> CUTOUT_CULL;
            case "translucent" -> TRANSLUCENT;
            case "translucent_cull" -> TRANSLUCENT_CULL;
            case "eyes", "emissive" -> EYES;
            case "emissive_opaque", "emissive_opaque_cull" -> EMISSIVE_OPAQUE_CULL;

            default -> CUTOUT;
        };
    }

    public boolean IsFullBright() {
        return this == EMISSIVE_OPAQUE_CULL || this == EYES;
    }

    public RenderType GetRenderType(Identifier Texture) {
        return switch (this) {
            case SOLID -> RenderTypes.entitySolid(Texture);
            case CUTOUT -> RenderTypes.entityCutout(Texture);
            case CUTOUT_CULL -> RenderTypes.entityCutoutCull(Texture);
            case TRANSLUCENT -> RenderTypes.entityTranslucent(Texture);
            case TRANSLUCENT_CULL -> RenderTypes.entityTranslucentCullItemTarget(Texture);
            case EYES -> RenderTypes.eyes(Texture);
            case EMISSIVE_OPAQUE_CULL -> RenderTypes.entitySolid(Texture);
        };
    }
}