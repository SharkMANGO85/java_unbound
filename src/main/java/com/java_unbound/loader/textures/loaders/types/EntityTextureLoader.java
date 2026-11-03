package com.java_unbound.loader.textures.loaders.types;

import java.util.List;

import static com.java_unbound.loader.textures.loaders.TextureLoader.ExtraTargets;
import static com.java_unbound.loader.textures.loaders.TextureLoader.ManualMappings;

public class EntityTextureLoader {
    public static void LoadEntityTextures() {
        ManualMappings.put("entity/shield", "entity/shield/shield_base_nopattern");
        ManualMappings.put("blocks/decorated_pot_base", "entity/decorated_pot/decorated_pot_base");
        ManualMappings.put("blocks/decorated_pot_side", "entity/decorated_pot/decorated_pot_side");
        ManualMappings.put("entity/banner/banner_base", "entity/banner/banner_base");

        ExtraTargets.put("entity/shield", List.of("entity/shield/shield_base"));
    }
}