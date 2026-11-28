package com.java_unbound.entrypoints;

import com.java_unbound.JavaUnbound;
import com.java_unbound.mods.yacl.ResourcePackSettingsManager;
import com.java_unbound.shared.converters.TgaConverter;
import com.java_unbound.shared.loader.resourcepacks.ResourcePackLoader;
import com.java_unbound.shared.resourcepack.PackFolders;
import com.java_unbound.shared.resources.ResourceUpdater;
import com.java_unbound.shared.resources.ResourceUtilities;
import com.java_unbound.shared.resources.textures.TextureUpdater;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;

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

        ResourceUtilities.ClearTextureCache();
        ResourceUpdater.UpdateResources();
        TextureUpdater.LoadTexturesAndReload();

        DebugPrint("TGA Converter", () -> TgaConverter.ConvertAll(PackFolders.GetResourcePacksFolder()));
        DebugPrint("Resource Pack Preloading", ResourcePackLoader::Refresh);
        DebugPrint("Settings", ResourcePackSettingsManager::LoadSettings);

        JavaUnbound.LOGGER.error("------------------{}------------------", JavaUnbound.MOD_NAME);

        ResourceLoader.get(PackType.CLIENT_RESOURCES).registerReloadListener(Identifier.fromNamespaceAndPath("java_unbound", "pack_order"), (ResourceManagerReloadListener) Manager -> ResourceUtilities.OnResourcesReloaded());
    }
}