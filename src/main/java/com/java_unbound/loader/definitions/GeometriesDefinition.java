package com.java_unbound.loader.definitions;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.java_unbound.JavaUnbound;
import com.java_unbound.loader.resourcepack.Folder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class GeometriesDefinition {
    private static final Map<String, JsonElement> GeoemtryFiles = new HashMap<>();

    private static final Path BaseGeoemtryFolder = Folder.GetConfigFolder().resolve("models");
    private static final Path SubpackGeoemtryFolder0 = Folder.GetConfigFolder().resolve("subpacks").resolve("SP0").resolve("models");
    private static final Path SubpackGeoemtryFolder1 = Folder.GetConfigFolder().resolve("subpacks").resolve("SP1").resolve("models");
    private static final Path SubpackGeoemtryFolder2 = Folder.GetConfigFolder().resolve("subpacks").resolve("SP2").resolve("models");

    private static final Path[] GeoemtryPriorityOrder = {SubpackGeoemtryFolder2, BaseGeoemtryFolder, SubpackGeoemtryFolder1, SubpackGeoemtryFolder0};

    private static void AddJsonToFiles(Path GeometryPath) {
        try {
            String JsonContent = Files.readString(GeometryPath);
            JsonElement Root = JsonParser.parseString(JsonContent);

            if (!Root.isJsonObject()) {
                JavaUnbound.LOGGER.error("Geometry file root is not a JSON object: " + GeometryPath);
                return;
            }

            JsonObject RootObject = Root.getAsJsonObject();

            if (!RootObject.has("minecraft:geometry") || !RootObject.get("minecraft:geometry").isJsonArray()) {
                JavaUnbound.LOGGER.error("Geometry file has no minecraft:geometry array: " + GeometryPath);
                return;
            }

            JsonArray Geometries = RootObject.getAsJsonArray("minecraft:geometry");

            for (JsonElement Geometry : Geometries) {
                if (!Geometry.isJsonObject()) {continue;}

                JsonObject GeometryObject = Geometry.getAsJsonObject();

                if (!GeometryObject.has("description") || !GeometryObject.get("description").isJsonObject()) {continue;}

                JsonObject Description = GeometryObject.getAsJsonObject("description");

                if (!Description.has("identifier") || !Description.get("identifier").isJsonPrimitive()) {continue;}

                String Identifier = Description.get("identifier").getAsString();

                GeoemtryFiles.putIfAbsent(Identifier, Geometry);
            }

        } catch (IOException | RuntimeException Exception) {
            JavaUnbound.LOGGER.error("Failed to load geometry file: " + GeometryPath);
            Exception.printStackTrace();
        }
    }

    public static void LoadGeometries() throws IOException {
        GeoemtryFiles.clear();

        for (Path TextureFolder : GeoemtryPriorityOrder) {
            if (!Files.isDirectory(TextureFolder)) {
                continue;
            }

            try (Stream<Path> Paths = Files.walk(TextureFolder)) {
                Paths.filter(Files::isRegularFile).filter(Path -> Path.getFileName().toString().toLowerCase().endsWith(".json")).forEach(GeometriesDefinition::AddJsonToFiles);
            }
        }
    }

    public static String GetJavaUnboundPath(Path GeometryPath) {
        Path JavaUnboundFolder = Folder.GetConfigFolder();

        return JavaUnboundFolder.relativize(GeometryPath).toString().replace('\\', '/');
    }

    public static Path GetGeometryByName(String GeometryName) {
        JsonElement Geometry = GeoemtryFiles.get(GeometryName);

        if (Geometry == null) {return null;}

        return Path.of(GeometryName);
    }

    public static Path GetGeometryByOrevillePath(String GeometryName) {
        int LastSlash = GeometryName.lastIndexOf('/');

        if (LastSlash != -1) {
            GeometryName = GeometryName.substring(LastSlash + 1);
        }

        JsonElement Geometry = GeoemtryFiles.get(GeometryName);

        if (Geometry == null) {return null;}

        return Path.of(GeometryName);
    }

    public static Path GetGeometryByPath(String GeometryPath) {
        return Path.of(GeometryPath);
    }
}