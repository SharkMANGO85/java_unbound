package com.java_unbound;

import com.java_unbound.config.ConfigManager;
import com.java_unbound.loader.converter.TgaConverter;
import com.java_unbound.loader.item.ItemTextureLoader;
import com.java_unbound.loader.resourcepack.Folder;
import com.java_unbound.loader.resourcepack.PackLoader;
import com.java_unbound.loader.entity.EntityFileReader;

import com.java_unbound.loader.resourcepack.ResourceMappings;
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

        TgaConverter.ConvertAll(Folder.GetResourceFolder());

        ResourceMappings.Register();
        CompletableFuture.runAsync(EntityFileReader::Read);
    }
}