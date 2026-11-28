package com.java_unbound.shared.loader.resourcepacks;

import com.java_unbound.JavaUnbound;
import com.java_unbound.shared.definitions.language.LanguageResourceDefinition;
import com.java_unbound.shared.definitions.manifest.ManifestDefinition;
import com.java_unbound.shared.definitions.resource_pack.ResourcePackDefinition;
import com.java_unbound.shared.resourcepack.PackFolders;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

public class ResourcePackLoader {
    public static final Map<String, ResourcePackDefinition> Packs = new LinkedHashMap<>();

    public static synchronized List<ResourcePackDefinition> Refresh() {
        Set<String> Found = new HashSet<>();

        try {
            PackFolders.EnsureExists();
        } catch (IOException Exception) {
            JavaUnbound.LOGGER.error("Failed to create resource pack folder", Exception);
            return List.copyOf(Packs.values());
        }

        try (Stream<Path> Folders = Files.list(PackFolders.GetResourcePacksFolder())) {
            List<Path> Directories = Folders.filter(Files::isDirectory).sorted(Comparator.comparing(Path -> Path.getFileName().toString())).toList();

            for (Path Directory : Directories) {
                String KnownId = FindIdByPath(Directory);

                if (KnownId != null) {
                    Found.add(KnownId);
                    continue;
                }

                ResourcePackDefinition Definition = Load(Directory);
                if (Definition == null) continue;

                String Id = PackLoader.GetPackId(Definition);

                if (Packs.containsKey(Id)) {
                    JavaUnbound.LOGGER.error("Duplicate pack UUID '{}' in folder '{}', skipping", Id, Definition.FolderName);
                    continue;
                }

                Packs.put(Id, Definition);
                Found.add(Id);
            }
        } catch (IOException Exception) {
            JavaUnbound.LOGGER.error("Failed to scan resource packs", Exception);
        }

        Packs.keySet().removeIf(Id -> !Found.contains(Id));

        return List.copyOf(Packs.values());
    }

    public static synchronized List<ResourcePackDefinition> GetPacks() {
        return List.copyOf(Packs.values());
    }

    public static synchronized ResourcePackDefinition GetPack(String PackId) {
        return PackId == null ? null : Packs.get(PackId);
    }

    private static String FindIdByPath(Path Directory) {
        for (Map.Entry<String, ResourcePackDefinition> Entry : Packs.entrySet()) {
            if (Entry.getValue().Path.equals(Directory)) {
                return Entry.getKey();
            }
        }

        return null;
    }

    private static ResourcePackDefinition Load(Path Directory) {
        ResourcePackDefinition Definition = new ResourcePackDefinition();

        Definition.Path = Directory;
        Definition.FolderName = Directory.getFileName().toString();
        Definition.LanguageResource = LoadLanguageResource(Directory, Definition);
        Definition.Manifest = LoadManifest(Directory, Definition);

        if (Definition.Manifest == null) {
            JavaUnbound.LOGGER.error("Failed to load resource pack folder '{}'", Directory);
            return null;
        }

        return Definition;
    }

    private static ManifestDefinition LoadManifest(Path Directory, ResourcePackDefinition PackDefinition) {
        Path ManifestPath = Directory.resolve("manifest.json");

        if (!Files.isRegularFile(ManifestPath)) {
            JavaUnbound.LOGGER.warn("No manifest.json in {}, skipping", Directory);
            return null;
        }

        ManifestDefinition Definition = ManifestLoader.GetManifestDefinition(ManifestPath, PackDefinition);

        if (Definition == null || Definition.Header == null) {
            JavaUnbound.LOGGER.error("Invalid manifest in {}, skipping", Directory);
            return null;
        }

        return Definition;
    }

    private static LanguageResourceDefinition LoadLanguageResource(Path Directory, ResourcePackDefinition PackDefinition) {
        Path TextsFolderPath = Directory.resolve("texts");

        if (!Files.isDirectory(TextsFolderPath)) {
            JavaUnbound.LOGGER.warn("No texts folder in {}, skipping", Directory);
            return null;
        }

        LanguageResourceDefinition Definition = LanguageLoader.GetLanguageResourceDefinition(TextsFolderPath, PackDefinition);

        if (Definition == null) {
            JavaUnbound.LOGGER.error("Invalid language resource in {}, skipping", Directory);
            return null;
        }

        return Definition;
    }
}