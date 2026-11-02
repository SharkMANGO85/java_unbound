package com.java_unbound.loader.resourcepack;

import com.java_unbound.JavaUnbound;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

//the folder where the bedrock resourcepack should be placed
public final class Folder {
    private Folder() {}

    public static Path GetConfigFolder() {
        return GetConfigsFolder().resolve("java_unbound_addon");
    }

    public static Path GetConfigsFolder() {
        return FabricLoader.getInstance().getConfigDir();
    }

    public static void EnsureExists() throws IOException {
        Files.createDirectories(GetConfigFolder());
    }

    public static void SetIdentifier(String MinecraftPath, String ResourcePath) {
       ResourceMapper.SetIdentifier(MinecraftPath, ResourcePath);
    }

    public static void ClearResourcePack() {
        Path ResourcePack = GetConfigFolder().resolve("assets").resolve("minecraft");

        try {
            if (Files.exists(ResourcePack)) {
                try (var Paths = Files.walk(ResourcePack)) {
                    Paths.sorted(Comparator.reverseOrder())
                            .forEach(Path -> {
                                try {
                                    Files.delete(Path);
                                } catch (IOException Exception) {
                                    throw new UncheckedIOException(Exception);
                                }
                            });
                }
            }

            ResourceMapper.Clear();
        } catch (IOException | UncheckedIOException Exception) {
            JavaUnbound.LOGGER.error("Failed to clear generated resourcepack assets.", Exception);
        }
    }
}