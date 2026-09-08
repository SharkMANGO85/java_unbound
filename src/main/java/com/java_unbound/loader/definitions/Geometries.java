package com.java_unbound.loader.definitions;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.java_unbound.JavaUnbound;
import com.java_unbound.loader.resourcepack.Folder;
import com.java_unbound.utils.functions.GetJsonFilesInPath;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Geometries {
    public static final Map<String, String> GeometryFiles = new HashMap<>();

    private static final Path Subpack0Folder = Folder.GetResourceFolder().resolve("subpacks").resolve("SP0");
    private static final Path Subpack1Folder = Folder.GetResourceFolder().resolve("subpacks").resolve("SP1");
    private static final Path Subpack2Folder = Folder.GetResourceFolder().resolve("subpacks").resolve("SP2");

    private static final Path BaseGeometriesFolder = Folder.GetResourceFolder().resolve("models");
    private static final Path Subpack0GeometriesFolder = Subpack0Folder.resolve("models");
    private static final Path Subpack1GeometriesFolder = Subpack1Folder.resolve("models");
    private static final Path Subpack2GeometriesFolder = Subpack2Folder.resolve("models");

    public static void LoadGeometries() {
        GeometryFiles.clear();

        AddGeometries(BaseGeometriesFolder);
        AddGeometries(Subpack0GeometriesFolder);
        AddGeometries(Subpack1GeometriesFolder);
        AddGeometries(Subpack2GeometriesFolder);

        JavaUnbound.LOGGER.info("----------------------------------------------------------------------------------------------");
        JavaUnbound.LOGGER.info("Geometry count: {}", GeometryFiles.size());
        JavaUnbound.LOGGER.info("----------------------------------------------------------------------------------------------");
    }

    public static JsonElement GetGeometry(String Identifier) {
        String Geometry = GeometryFiles.get(Identifier);
        return Geometry == null ? null : JsonParser.parseString(Geometry);
    }

    public static List<JsonElement> GetGeometries(JsonElement GeometryElement) {
        List<JsonElement> Geometries = new ArrayList<>();

        if (GeometryElement == null) {
            return Geometries;
        }

        if (GeometryElement.isJsonPrimitive()) {
            JsonElement Geometry = GetGeometry(GeometryElement.getAsString());

            if (Geometry != null) {
                Geometries.add(Geometry);
            }

            return Geometries;
        }

        if (GeometryElement.isJsonObject()) {
            for (JsonElement Value : GeometryElement.getAsJsonObject().asMap().values()) {
                if (!Value.isJsonPrimitive()) {
                    continue;
                }

                JsonElement Geometry = GetGeometry(Value.getAsString());

                if (Geometry != null) {
                    Geometries.add(Geometry);
                }
            }
        }

        return Geometries;
    }

    private static void AddGeometries(Path GeometryFolder) {
        if (!Files.isDirectory(GeometryFolder)) {
            return;
        }

        List<Path> FilesInFolder = GetJsonFilesInPath.GetDescendant(GeometryFolder);

        for (Path File : FilesInFolder) {
            try {
                String Content = Files.readString(File);
                JsonElement Root = JsonParser.parseString(Content);

                if (!Root.isJsonObject()) {
                    continue;
                }

                JsonObject RootObject = Root.getAsJsonObject();
                JsonElement GeometryElement = RootObject.get("minecraft:geometry");

                if (GeometryElement == null || !GeometryElement.isJsonArray()) {
                    continue;
                }

                JsonArray Geometries = GeometryElement.getAsJsonArray();

                for (JsonElement Geometry : Geometries) {
                    if (!Geometry.isJsonObject()) {
                        continue;
                    }

                    JsonObject GeometryObject = Geometry.getAsJsonObject();
                    JsonElement DescriptionElement = GeometryObject.get("description");

                    if (DescriptionElement == null || !DescriptionElement.isJsonObject()) {
                        continue;
                    }

                    JsonElement IdentifierElement = DescriptionElement.getAsJsonObject().get("identifier");

                    if (IdentifierElement == null || !IdentifierElement.isJsonPrimitive()) {
                        continue;
                    }

                    String Identifier = IdentifierElement.getAsString();
                    GeometryFiles.put(Identifier, Geometry.toString());
                }
            } catch (IOException | RuntimeException Exception) {
                Exception.printStackTrace();
            }
        }
    }
}