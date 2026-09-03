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

public class EntityTextureLoader {
    private static final Path EntityFolder = Folder.GetResourceFolder().resolve("entity");
    private static final Path SubpackEntityFolder = Folder.GetResourceFolder().resolve("subpacks").resolve(JavaUnbound.SUBPACK);

    public static void LoadEntityTextures() throws IOException {
        CreateEntitiesFolder();

        try (DirectoryStream<Path> files = Files.newDirectoryStream(EntityFolder, "*.json")) {
            for (Path file : files) {
                if (Files.isRegularFile(file)) {
                    ReadJsonFile(file);
                }
            }
        }
    }

    private static String GetValue(Path file, String path) throws IOException {
        String json = Files.readString(file);

        JsonElement element = JsonParser.parseString(json);

        for (String key : path.split("\\.")) {
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

    public static void CreateEntitiesFolder() throws IOException {
        Path ResourcePack = Folder.GetResourceFolder();

        FolderResources.CreateFolder(ResourcePack, "assets/minecraft/textures/entity");
    }

    public static void CreateEntityFolder(String Entity) throws IOException {
        Path ResourcePack = Folder.GetResourceFolder();
        String EntityName = GetEntityName(Entity);

        FolderResources.CreateFolder(ResourcePack, "assets/minecraft/textures/entity/" + EntityName);
    }

    private static void ReadJsonFile(Path file) {
        try {
            String identifier = GetValue(file, "minecraft:client_entity.description.identifier");

            //System.out.println("File: " + file);
            //System.out.println("Identifier: " + identifier);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}