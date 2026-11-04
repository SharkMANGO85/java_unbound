package com.java_unbound.loader.converters;

import com.java_unbound.loader.resourcepack.Folder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.stream.Stream;

public class MaterialToJsonConverter {
    public static Runnable Convert() {
        return () -> {
            Path Config = Folder.GetConfigFolder();
            if (Config == null) {return;}

            Path MaterialsFolder = Config.resolve("materials");
            if (!Files.isDirectory(MaterialsFolder)) {return;}

            try (Stream<Path> Materials = Files.list(MaterialsFolder)) {
                Materials.filter(Material -> Material.getFileName().toString().endsWith(".material"))
                        .forEach(Material -> {
                            try {
                                String FileName = Material.getFileName().toString();
                                Path JsonFile = Material.resolveSibling(FileName.substring(0, FileName.length() - ".material".length()) + ".json");

                                Files.copy(Material, JsonFile, StandardCopyOption.REPLACE_EXISTING);
                                Files.delete(Material);
                            } catch (IOException Exception) {
                                throw new RuntimeException("Failed to convert material: " + Material, Exception);
                            }
                        });
            } catch (IOException Exception) {
                throw new RuntimeException("Failed to load materials folder", Exception);
            }
        };
    }
}