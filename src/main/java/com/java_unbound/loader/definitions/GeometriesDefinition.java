package com.java_unbound.loader.definitions;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.java_unbound.JavaUnbound;
import com.java_unbound.global.HashMaps;
import com.java_unbound.loader.resourcepack.Folder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class GeometriesDefinition {
    private static final Path BaseGeometryFolder = Folder.GetConfigFolder().resolve("models");

    private static final Path SubpackGeometryFolder0 = Folder.GetConfigFolder().resolve("subpacks").resolve("SP0").resolve("models");
    private static final Path SubpackGeometryFolder1 = Folder.GetConfigFolder().resolve("subpacks").resolve("SP1").resolve("models");
    private static final Path SubpackGeometryFolder2 = Folder.GetConfigFolder().resolve("subpacks").resolve("SP2").resolve("models");

    private static final Path[] GeometryPriorityOrder = {SubpackGeometryFolder2, BaseGeometryFolder, SubpackGeometryFolder1, SubpackGeometryFolder0};

    private static void AddJsonToFiles(Path GeometryPath) {
        try {
            String JsonContent = Files.readString(GeometryPath);
            JsonElement Root = JsonParser.parseString(JsonContent);

            if (!Root.isJsonObject()) {
                JavaUnbound.LOGGER.error("Geometry file root is not a JSON object: " + GeometryPath);
                return;
            }

            JsonObject RootObject = Root.getAsJsonObject();
            JsonElement GeometryElement = RootObject.get("minecraft:geometry");

            if (GeometryElement == null || !GeometryElement.isJsonArray()) {
                JavaUnbound.LOGGER.error("Geometry file has no minecraft:geometry array: " + GeometryPath);
                return;
            }

            JsonArray Geometries = GeometryElement.getAsJsonArray();

            for (JsonElement Geometry : Geometries) {
                if (!Geometry.isJsonObject()) {continue;}

                JsonObject GeometryObject = Geometry.getAsJsonObject();
                JsonElement DescriptionElement = GeometryObject.get("description");

                if (DescriptionElement == null || !DescriptionElement.isJsonObject()) {continue;}

                JsonObject Description = DescriptionElement.getAsJsonObject();
                JsonElement IdentifierElement = Description.get("identifier");

                if (IdentifierElement == null || !IdentifierElement.isJsonPrimitive()) {continue;}

                String Identifier = IdentifierElement.getAsString();

                HashMaps.GeoemtryFiles.putIfAbsent(Identifier, Geometry);
            }

        } catch (IOException | RuntimeException Exception) {
            JavaUnbound.LOGGER.error("Failed to load geometry file: " + GeometryPath, Exception);
        }
    }

    public static void LoadGeometries() throws IOException {
        HashMaps.GeoemtryFiles.clear();

        for (Path GeometryFolder : GeometryPriorityOrder) {
            if (!Files.isDirectory(GeometryFolder)) {continue;}

            try (Stream<Path> Paths = Files.walk(GeometryFolder)) {
                Paths.filter(Files::isRegularFile).filter(Path -> Path.getFileName().toString().toLowerCase().endsWith(".json")).forEach(GeometriesDefinition::AddJsonToFiles);
            }
        }
    }

    public static JsonElement GetGeometryByIdentifier(String Identifier) {
        if (Identifier == null || Identifier.isEmpty()) {return null;}

        return HashMaps.GeoemtryFiles.get(Identifier);
    }
}