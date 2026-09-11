package com.java_unbound.loader.definitions;

import com.java_unbound.loader.resourcepack.Folder;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class TexturesDefinition {
    private static final Map<String, String> TextureFiles = new HashMap<>();

    private static final Path BaseTextureFolder = Folder.GetConfigFolder().resolve("textures");
    private static final Path SubpackTextureFolder0 = Folder.GetConfigFolder().resolve("subpacks").resolve("SP0").resolve("textures");
    private static final Path SubpackTextureFolder1 = Folder.GetConfigFolder().resolve("subpacks").resolve("SP1").resolve("textures");
    private static final Path SubpackTextureFolder2 = Folder.GetConfigFolder().resolve("subpacks").resolve("SP2").resolve("textures");

    private static final Path[] TexturePriorityOrder = {SubpackTextureFolder2, BaseTextureFolder, SubpackTextureFolder1, SubpackTextureFolder0};

    public static void LoadTextures() throws IOException {
        TextureFiles.clear();

        for (Path TextureFolder : TexturePriorityOrder) {
            if (!Files.isDirectory(TextureFolder)) {continue;}

            try (Stream<Path> Paths = Files.walk(TextureFolder)) {
                Paths.filter(Files::isRegularFile).filter(Path -> {
                    try {
                        return ImageIO.read(Path.toFile()) != null;
                    } catch (IOException e) {
                        return false;
                    }
                }).forEach(Path -> {
                            String FileName = Path.getFileName().toString();
                            String Name = FileName.substring(0, FileName.lastIndexOf('.'));

                            TextureFiles.putIfAbsent(Name, Path.toString());
                        });
            }
        }
    }

    public static String GetJavaUnboundPath(Path TexturePath) {
        Path JavaUnboundFolder = Folder.GetConfigFolder();

        return JavaUnboundFolder.relativize(TexturePath).toString().replace('\\', '/');
    }

    public static Path GetTextureByName(String TextureName) {
        String TexturePath = TextureFiles.get(TextureName);

        if (TexturePath == null) {
            return null;
        }

        return Path.of(TexturePath);
    }

    public static Path GetTextureByOrevillePath(String TextureName) {
        int LastSlash = TextureName.lastIndexOf('/');

        if (LastSlash != -1) {
            TextureName = TextureName.substring(LastSlash + 1);
        }

        String TexturePath = TextureFiles.get(TextureName);

        if (TexturePath == null) {
            return null;
        }

        return Path.of(TexturePath);
    }

    public static Path GetTextureByPath(String TexturePath) {
        return Path.of(TexturePath);
    }
}