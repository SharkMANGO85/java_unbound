package com.java_unbound;

import com.java_unbound.loader.textures.block.BlockTextureLoader;
import com.java_unbound.loader.textures.gui.Panorama;
import com.java_unbound.loader.textures.gui.Splashes;
import com.java_unbound.loader.textures.gui.Title;
import net.fabricmc.api.ClientModInitializer;

import java.io.IOException;

public class JavaUnboundClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        try {
            Panorama.CreatePanorama();
            Title.CreateTitle();
            Splashes.CreateSplashes();
            BlockTextureLoader.LoadTextures();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}