package com.java_unbound.shared.resourcepack;

import com.java_unbound.JavaUnbound;
import com.java_unbound.shared.definitions.resource_pack.ResourcePackDefinition;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.stream.Stream;

public final class PackFolders {
    private PackFolders() {}

    public static Path GetConfigsFolder() {
        return FabricLoader.getInstance().getConfigDir().resolve("java_unbound");
    }

    public static Path GetGameResourcePacksFolder() {
        return FabricLoader.getInstance().getGameDir().resolve("resourcepacks");
    }

    public static Path GetResourcePacksFolder() {
        return GetConfigsFolder().resolve("resource_packs");
    }

    public static Path GetSavesFolder() {
        return GetConfigsFolder().resolve("saves");
    }

    public static Path GetDebugFolder(ResourcePackDefinition Definition) {
        return GetConfigsFolder().resolve("java_unbound_debug").resolve(Definition.FolderName);
    }

    public static Path GetDevelopmentDebugFolder(ResourcePackDefinition Definition) {
        if (!JavaUnbound.DEBUG) {
            return null;
        }

        return GetConfigsFolder().resolve("java_unbound_debug_development").resolve(Definition.FolderName);
    }

    public static Path GetManifest(ResourcePackDefinition Definition) {
        return Definition.Path.resolve("manifest.json");
    }

    public static void EnsureExists() throws IOException {
        Files.createDirectories(GetResourcePacksFolder());
    }

    public static void SetIdentifier(ResourcePackDefinition Definition, String MinecraftPath, String ResourcePath) {
        Path AbsolutePath = Definition.Path.resolve(ResourcePath);
        Path DesiredPath = GetDebugFolder(Definition).resolve("assets").resolve(MinecraftPath);

        try {
            Files.createDirectories(DesiredPath.getParent());
            Files.copy(AbsolutePath, DesiredPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException Exception) {
            JavaUnbound.LOGGER.error("Failed to copy debug resource: " + DesiredPath, Exception);
        }

        Definition.Mapper.SetIdentifier(MinecraftPath, ResourcePath);
    }

    public static void CreateDebugFolder(ResourcePackDefinition Definition) {
        if (!Files.exists(Definition.Path)) {
            JavaUnbound.LOGGER.error("Pack folder does not exist: {}", Definition.Path);
            return;
        }

        Path Source = Definition.Path.resolve("assets");
        Path Destination = GetDebugFolder(Definition).resolve("assets");

        if (!Files.exists(Source)) {
            JavaUnbound.LOGGER.debug("Source folder does not exist: {}", Source);
            return;
        }

        try (Stream<Path> Paths = Files.walk(Source)) {
            Paths.forEach(Path -> {
                try {
                    Path Target = Destination.resolve(Source.relativize(Path));

                    if (Files.isDirectory(Path)) {
                        Files.createDirectories(Target);
                    } else {
                        Files.createDirectories(Target.getParent());
                        Files.copy(Path, Target, StandardCopyOption.REPLACE_EXISTING);
                    }
                } catch (IOException Exception) {
                    throw new UncheckedIOException(Exception);
                }
            });
        } catch (IOException | UncheckedIOException Exception) {
            JavaUnbound.LOGGER.error("Failed to create debug resourcepack.", Exception);
        }
    }

    public static void ClearResourcePack(ResourcePackDefinition Definition) {
        Path ResourcePack = Definition.Path.resolve("assets").resolve("minecraft");

        try {
            if (Files.exists(ResourcePack)) {
                try (Stream<Path> Paths = Files.walk(ResourcePack)) {
                    Paths.sorted(Comparator.reverseOrder()).forEach(Path -> {
                        try {
                            Files.delete(Path);
                        } catch (IOException Exception) {
                            throw new UncheckedIOException(Exception);
                        }
                    });
                }
            }

            Definition.Clear();
        } catch (IOException | UncheckedIOException Exception) {
            JavaUnbound.LOGGER.error("Failed to clear generated resourcepack assets for " + Definition.FolderName, Exception);
        }
    }
}