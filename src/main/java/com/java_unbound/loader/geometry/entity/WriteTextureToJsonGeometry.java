package com.java_unbound.loader.geometry.entity;

import com.google.gson.*;
import com.java_unbound.JavaUnbound;
import com.java_unbound.loader.resourcepack.Folder;
import com.java_unbound.loader.resourcepack.FolderResources;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class WriteTextureToJsonGeometry {
    private static final String GeometryNamespace = "geometry.oreville_ans.";
    private static final Path EntityFolder = Folder.GetResourceFolder().resolve("entity");

    public static void WriteEntityTexturesToJsonGeometry() throws IOException {
        try (var files = Files.walk(EntityFolder)) {
            files.filter(Files::isRegularFile).filter(path -> path.toString().endsWith(".json")).forEach(path -> {ReadJsonFile(path);});
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

    private static String NormalizeGeometryIdentifier(String Identifier) {
        if (Identifier == null) {
            return "";
        }

        Identifier = Identifier.trim();

        if (Identifier.startsWith(GeometryNamespace)) {
            Identifier = Identifier.substring(GeometryNamespace.length());
        }

        if (Identifier.startsWith("oreville_ans.")) {
            Identifier = Identifier.substring("oreville_ans.".length());
        }

        if (Identifier.endsWith(".json")) {
            Identifier = Identifier.substring(0, Identifier.length() - 5);
        }

        return Identifier;
    }

    private static void CopyTextureToGeometry(Path File, String Entity) throws IOException {
        String TexturesJson = GetValue(File, "minecraft:client_entity.description.textures");
        String GeometriesJson = GetValue(File, "minecraft:client_entity.description.geometry");
        Path ResourcePack = Folder.GetResourceFolder();

        if (TexturesJson == null || TexturesJson.isBlank()) {
            return;
        }

        if (GeometriesJson == null || GeometriesJson.isBlank()) {
            return;
        }

        JsonObject Textures = JsonParser.parseString(TexturesJson).getAsJsonObject();
        JsonObject Geometries = JsonParser.parseString(GeometriesJson).getAsJsonObject();

        for (String TextureKey : Textures.keySet()) {
            for (String GeometryKey : Geometries.keySet()) {
                if (!Objects.equals(TextureKey, GeometryKey)) {
                    continue;
                }

                String GeometryPath = Geometries.get(GeometryKey).getAsString();
                GeometryPath = NormalizeGeometryIdentifier(GeometryPath);

                Path GeometryJsonFile = ResourcePack.resolve("assets").resolve("minecraft").resolve("optifine").resolve("cem").resolve(Entity).resolve(GeometryPath + ".json");
                String GeometryMinecraftPath = "assets/minecraft/textures/entity/" + Entity + "/" + TextureKey;

                if (!Files.exists(GeometryJsonFile)) {
                    continue;
                }

                JsonObject GeometryJson;

                try (FileReader Reader = new FileReader(GeometryJsonFile.toFile())) {
                    GeometryJson = JsonParser.parseReader(Reader).getAsJsonObject();
                }

                JsonArray TexturesArray;

                if (GeometryJson.has("textures") && GeometryJson.get("textures").isJsonArray()) {
                    TexturesArray = GeometryJson.getAsJsonArray("textures");
                } else {
                    TexturesArray = new JsonArray();
                    GeometryJson.add("textures", TexturesArray);
                }

                JsonPrimitive TexturePath = new JsonPrimitive(GeometryMinecraftPath);

                if (!TexturesArray.contains(TexturePath)) {
                    TexturesArray.add(TexturePath);
                }

                try (FileWriter Writer = new FileWriter(GeometryJsonFile.toFile())) {
                    Gson Gson = new GsonBuilder().setPrettyPrinting().create();
                    Gson.toJson(GeometryJson, Writer);
                }
            }
        }
    }

    private static void ReadJsonFile(Path File) {
        try {
            String EntityIdentifier = GetValue(File, "minecraft:client_entity.description.identifier");
            String EntityName = GetEntityName(EntityIdentifier);

            CopyTextureToGeometry(File, EntityName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}