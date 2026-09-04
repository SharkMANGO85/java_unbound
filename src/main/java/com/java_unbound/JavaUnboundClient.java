package com.java_unbound;

import com.java_unbound.config.ConfigManager;
import com.java_unbound.loader.resourcepack.Folder;
import com.java_unbound.loader.resourcepack.PackLoader;
import com.java_unbound.loader.gui.Panorama;
import com.java_unbound.loader.gui.Splashes;
import com.java_unbound.loader.gui.Title;
import com.java_unbound.loader.entity.EntityFileReader;

import net.fabricmc.api.ClientModInitializer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.temporal.ValueRange;
import java.util.concurrent.CompletableFuture;

public class JavaUnboundClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ConfigManager.load();

        Path ResourcePack = Folder.GetResourceFolder();
        String ResourcePackVersion = PackLoader.GetPackName(ResourcePack);
        Path AssetsFolder = ResourcePack.resolve("assets");

        String Version = ConfigManager.GetValue("LoadedVersion").toString();
        Boolean Loaded = (Boolean) ConfigManager.GetValue("Loaded");

        //if (!Version.equals(ResourcePackVersion) || Version.isEmpty() || Loaded == false || !Files.exists(AssetsFolder)) {
        //ConfigManager.ChangeValue("Loaded", true);
        //ConfigManager.ChangeValue("LoadedVersion", ResourcePackVersion);

            CompletableFuture.runAsync(() -> {
                try {
                    JavaUnbound.LOGGER.info("--------------------------Creating Panorama--------------------------");
                    Panorama.CreatePanorama();
                    JavaUnbound.LOGGER.info("--------------------------Finished Creating Panorama--------------------------");

                    JavaUnbound.LOGGER.info("--------------------------Creating Title--------------------------");
                    Title.CreateTitle();
                    JavaUnbound.LOGGER.info("--------------------------Finished Creating Title--------------------------");

                    JavaUnbound.LOGGER.info("--------------------------Creating Splashes--------------------------");
                    Splashes.CreateSplashes();
                    JavaUnbound.LOGGER.info("--------------------------Finished Creating Splashes--------------------------");

                    EntityFileReader.Read();

                    //WriteTextureToJsonGeometry.WriteEntityTexturesToJsonGeometry();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        //}
    }
}