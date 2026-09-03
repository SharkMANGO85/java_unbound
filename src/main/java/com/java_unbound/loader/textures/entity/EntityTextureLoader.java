package com.java_unbound.loader.textures.entity;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.java_unbound.JavaUnbound;
import com.java_unbound.loader.resourcepack.Folder;
import com.java_unbound.loader.resourcepack.FolderResources;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public class EntityTextureLoader {
    private static final Path EntityFolder = Folder.GetResourceFolder().resolve("entity");
    private static final Path SubpackEntityFolder = Folder.GetResourceFolder().resolve("subpacks").resolve(JavaUnbound.SUBPACK).resolve("textures").resolve("entity");

    public static void LoadEntityTextures() throws IOException {
        CreateEntitiesFolder();

        try (DirectoryStream<Path> files = Files.newDirectoryStream(EntityFolder, "*.json")) {
            for (Path File : files) {
                if (Files.isRegularFile(File)) {
                    ReadJsonFile(File);
                }
            }
        }
    }

    private static String GetValue(Path File, String Path) throws IOException {
        String json = Files.readString(File);

        JsonElement element = JsonParser.parseString(json);

        for (String key : Path.split("\\.")) {
            if (!element.isJsonObject()) {
                return null;
            }

            JsonObject object = element.getAsJsonObject();

            if (!object.has(key)) {
                return null;
            }

            element = object.get(key);
        }

        if (element.isJsonPrimitive()) {
            return element.getAsString();
        }

        return element.toString();
    }

    private static String GetEntityName(String Entity) {
        return Entity.replace("minecraft:", "");
    }

    private static void CreateEntitiesFolder() throws IOException {
        Path ResourcePack = Folder.GetResourceFolder();

        FolderResources.CreateFolder(ResourcePack, "assets/minecraft/textures/entity");
    }

    private static void CreateEntityFolder(String Entity) throws IOException {
        Path ResourcePack = Folder.GetResourceFolder();

        FolderResources.CreateFolder(ResourcePack, "assets/minecraft/textures/entity/" + Entity);
    }

    private static Path SearchInSubpack(Path namespace) {
        String name = namespace.toString().replace("textures/oreville/ans/", "");

        try (var paths = java.nio.file.Files.walk(SubpackEntityFolder)) {
            return paths.filter(java.nio.file.Files::isRegularFile).filter(path -> path.getFileName().toString().equals(name)).findFirst().orElse(null);
        } catch (IOException e) {
            return null;
        }
    }

    private static void CopyEntityImages(Path File, String entity) throws IOException {
        String TexturesJson = GetValue(File, "minecraft:client_entity.description.textures");

        if (TexturesJson == null || TexturesJson.isBlank()) {
            return;
        }

        JsonObject Textures = JsonParser.parseString(TexturesJson).getAsJsonObject();
        Path ResourcePack = Folder.GetResourceFolder();
        Path DestinationFolder = ResourcePack.resolve("assets").resolve("minecraft").resolve("textures").resolve("entity").resolve(entity);

        Files.createDirectories(DestinationFolder);

        Set<Path> CopiedTextures = new HashSet<>();

        for (String TextureKey : Textures.keySet()) {
            JsonElement TextureElement = Textures.get(TextureKey);

            if (!TextureElement.isJsonPrimitive()) {
                continue;
            }

            String TexturePath = TextureElement.getAsString();

            if (TexturePath == null || TexturePath.isBlank()) {
                continue;
            }

            if (!TexturePath.endsWith(".png")) {
                TexturePath += ".png";
            }

            Path Source = ResourcePack.resolve(TexturePath).normalize();

            if (!Files.isRegularFile(Source)) {
                Source = SearchInSubpack(Source);

                if (Source == null || !Files.isRegularFile(Source)) {
                    continue;
                }
            }

            if (!CopiedTextures.add(Source)) {
                continue;
            }

            Path Destination = DestinationFolder.resolve(Source.getFileName());

            Files.copy(Source, Destination, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static void ReadJsonFile(Path File) {
        try {
            String EntityIdentifier = GetValue(File, "minecraft:client_entity.description.identifier");
            String EntityName = GetEntityName(EntityIdentifier);

            CreateEntityFolder(EntityName);
            CopyEntityImages(File, EntityName);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}