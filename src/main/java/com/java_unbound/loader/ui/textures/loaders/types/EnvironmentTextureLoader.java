package com.java_unbound.loader.ui.textures.loaders.types;

import java.io.IOException;

import static com.java_unbound.loader.ui.textures.loaders.TextureLoader.ManualMappings;

public class EnvironmentTextureLoader {
    public static void LoadEnvironmentTextures() {
        ManualMappings.put("environment/sun", "environment/celestial/sun");
        ManualMappings.put("environment/end_flash", "environment/celestial/end_flash");
    }
}
