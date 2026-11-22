package com.java_unbound.shared.resourcepack;

import com.java_unbound.JavaUnbound;
import com.java_unbound.JavaUnboundUtilities;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

public final class Folder {
    private Folder() {}

    public static Path GetAddonFolder() {
        return GetConfigsFolder().resolve("java_unbound_addon");
    }

    public static Path GetDebugFolder() {
        return GetConfigsFolder().resolve("java_unbound_debug");
    }

    public static Path GetDevelopmentDebugFolder() {
        if (!JavaUnbound.DEBUG) {
            return null;
        }

        return GetConfigsFolder().resolve("java_unbound_debug_development");
    }

    public static Path GetManifest() {
        return GetConfigsFolder().resolve("manifest.json");
    }

    public static Path GetConfigsFolder() {
        return FabricLoader.getInstance().getConfigDir();
    }

    public static void EnsureExists() throws IOException {
        Files.createDirectories(GetAddonFolder());
    }

    public static void SetIdentifier(String MinecraftPath, String ResourcePath) {
        Path AbsolutePath = GetAddonFolder().resolve(ResourcePath);
        Path DesiredPath = GetDebugFolder().resolve("assets").resolve(MinecraftPath);

        try {
            Files.createDirectories(DesiredPath.getParent());
            Files.copy(AbsolutePath, DesiredPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException Exception) {
            JavaUnbound.LOGGER.error("Failed to copy debug resource: " + DesiredPath, Exception);
        }

        ResourceMapper.SetIdentifier(MinecraftPath, ResourcePath);
    }

    public static void CreateDebugFolder() {
        if (!GetAddonFolder().toFile().exists()) {
            JavaUnbound.LOGGER.error("Addon folder does not exist");
            return;
        }

        Path Source = GetAddonFolder().resolve("assets");
        Path Destination = GetDebugFolder().resolve("assets");

        if (!Source.toFile().exists()) {
            JavaUnbound.LOGGER.debug("Source folder does not exist");
            return;
        }

        try {
            Files.walk(Source).forEach(Path -> {
                try {
                    Path Target = Destination.resolve(Source.relativize(Path));

                    if (Files.isDirectory(Path)) {
                        Files.createDirectories(Target);
                    } else {
                        Files.createDirectories(Target.getParent());
                        Files.copy(Path, Target, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    }
                } catch (IOException Exception) {
                    throw new UncheckedIOException(Exception);
                }
            });
        } catch (IOException | UncheckedIOException Exception) {
            JavaUnbound.LOGGER.error("Failed to create debug resourcepack.", Exception);
        }
    }

    public static void ClearResourcePack() {
        Path ResourcePack = GetAddonFolder().resolve("assets").resolve("minecraft");

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