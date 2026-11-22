package com.java_unbound;

import com.java_unbound.attachables.AttachableResolver;
import com.java_unbound.attachables.resolvers.AttachableGeometryResolver;
import com.java_unbound.attachables.resolvers.AttachableRenderControllerResolver;
import com.java_unbound.attachables.resolvers.AttachableTextureResolver;
import com.java_unbound.shared.converters.TgaConverter;
import com.java_unbound.shared.loaders.language.LanguageLoader;
import com.java_unbound.shared.loaders.main_screen.PanoramaLoader;
import com.java_unbound.shared.loaders.main_screen.SplashLoader;
import com.java_unbound.shared.loaders.main_screen.TitleLoader;
import com.java_unbound.shared.loaders.manifest.ManifestLoader;
import com.java_unbound.shared.resourcepack.Folder;
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

        AttachableTextureResolver.ClearCache();
        AttachableGeometryResolver.ClearCache();
        AttachableRenderControllerResolver.ClearCache();

        DebugPrint("TGA Converter", () -> TgaConverter.ConvertAll(Folder.GetAddonFolder()));
        DebugPrint("Languages", LanguageLoader::LoadLanguages);
        DebugPrint("Splashes", SplashLoader::LoadSplashes);
        DebugPrint("Title", TitleLoader::LoadTitle);
        DebugPrint("Panorama", PanoramaLoader::LoadPanorama);
        DebugPrint("Manifest", ManifestLoader::LoadManifest);
        DebugPrint("Debug Folder", Folder::CreateDebugFolder);
        DebugPrint("Attachables", AttachableResolver::ResolveAttachables);

        JavaUnbound.LOGGER.error("------------------{}------------------", JavaUnbound.MOD_NAME);
    }
}