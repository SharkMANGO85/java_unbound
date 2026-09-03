package com.java_unbound.loader.resourcepack;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class Folder {
    private Folder() {

    }

    public static Path GetResourceFolder() {
        return FabricLoader.getInstance().getConfigDir().resolve("java_unbound");
    }

    public static void EnsureExists() throws IOException {
        Files.createDirectories(GetResourceFolder());
    }
}