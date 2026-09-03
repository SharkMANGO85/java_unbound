package com.java_unbound;

import com.java_unbound.loader.geometry.entity.EntityGeometryLoader;
import com.java_unbound.loader.textures.block.BlockTextureLoader;
import com.java_unbound.loader.textures.entity.EntityTextureLoader;
import com.java_unbound.loader.textures.gui.Panorama;
import com.java_unbound.loader.textures.gui.Splashes;
import com.java_unbound.loader.textures.gui.Title;
import net.fabricmc.api.ClientModInitializer;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class JavaUnboundClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        CompletableFuture.runAsync(() -> {
            try {
                Panorama.CreatePanorama();
                Title.CreateTitle();
                Splashes.CreateSplashes();
                BlockTextureLoader.LoadTextures();
                EntityTextureLoader.LoadEntityTextures();
                EntityGeometryLoader.LoadEntityGeometries();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}