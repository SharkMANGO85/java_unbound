package com.java_unbound.shared.resources.textures;

import com.java_unbound.JavaUnbound;
import com.java_unbound.shared.resources.textures.main_screen.PanoramaLoader;
import com.java_unbound.shared.resources.textures.main_screen.TitleLoader;
import net.minecraft.client.Minecraft;

public class TextureUpdater {
    public static void LoadTextures() {
        JavaUnbound.LOGGER.info("[TextureUpdater] Loading main screen textures");

        try {
            PanoramaLoader.LoadPanoramaTextures();
            TitleLoader.LoadTitleTexture();
        } catch (Exception Exception) {
            JavaUnbound.LOGGER.error("[TextureUpdater] Failed to load textures", Exception);
        }
    }

    public static void LoadTexturesAndReload() {
        Minecraft Client = Minecraft.getInstance();

        JavaUnbound.LOGGER.info("[TextureUpdater] Queued textures load and reload");

        Client.execute(() -> {
            LoadTextures();

            JavaUnbound.LOGGER.info("[TextureUpdater] Reloading resource packs");

            Client.reloadResourcePacks();
        });
    }
}