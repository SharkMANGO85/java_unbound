package com.java_unbound;

import com.java_unbound.shared.loaders.language.LanguageLoader;
import com.java_unbound.shared.loaders.main_screen.PanoramaLoader;
import com.java_unbound.shared.loaders.main_screen.SplashLoader;
import com.java_unbound.shared.loaders.main_screen.TitleLoader;
import com.java_unbound.shared.loaders.textures.TextureLoader;
import net.fabricmc.api.ClientModInitializer;

import java.io.IOException;

public class JavaUnboundClient implements ClientModInitializer {
    @FunctionalInterface
    private interface Loader {
        void Load() throws IOException;
    }

    private static void DebugPrint(String Text, Loader Function) {
        JavaUnbound.LOGGER.error("------------------------------------------------");
        JavaUnbound.LOGGER.error("Loading " + Text);
        JavaUnbound.LOGGER.error("");

        try {
            Function.Load();

        } catch (IOException Exception) {
            throw new RuntimeException(Exception);
        }

        JavaUnbound.LOGGER.error("");
        JavaUnbound.LOGGER.error("Finished Loading " + Text);
        JavaUnbound.LOGGER.error("------------------------------------------------");
    }

    @Override
    public void onInitializeClient() {
        JavaUnbound.LOGGER.error("------------------{}------------------", JavaUnbound.MOD_NAME);

        DebugPrint("Languages", LanguageLoader::LoadLanguages);
        DebugPrint("Splashes", SplashLoader::LoadSplashes);
        DebugPrint("Title", TitleLoader::LoadTitle);
        DebugPrint("Panorama", PanoramaLoader::LoadPanorama);
        DebugPrint("Textures", TextureLoader::LoadTextures);

        JavaUnbound.LOGGER.error("------------------{}------------------", JavaUnbound.MOD_NAME);
    }
}