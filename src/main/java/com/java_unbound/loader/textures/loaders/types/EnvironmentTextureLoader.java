package com.java_unbound.loader.textures.loaders.types;

import static com.java_unbound.loader.textures.loaders.TextureLoader.ManualMappings;

public class EnvironmentTextureLoader {
    public static void LoadEnvironmentTextures() {
        ManualMappings.put("environment/sun", "environment/celestial/sun");
        ManualMappings.put("environment/end_flash", "environment/celestial/end_flash");
    }
}
