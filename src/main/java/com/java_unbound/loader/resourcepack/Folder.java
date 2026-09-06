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

    public static Path GetSubpackFolder() {
        return GetResourceFolder().resolve("subpacks").resolve(com.java_unbound.JavaUnbound.SUBPACK);
    }

    public static void SetIdentifier(String MinecraftPath, String ResourcePath) {
        ResourceMapper.SetIdentifier(MinecraftPath, ResourcePath);
    }
}