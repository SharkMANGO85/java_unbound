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

    private static final Path SubpackFolder = Folder.GetResourceFolder().resolve("subpacks").resolve(JavaUnbound.SUBPACK);

    private static final Path BaseGeometriesFolder = Folder.GetResourceFolder().resolve("models");
    private static final Path SubpackGeometriesFolder = SubpackFolder.resolve("models");

    public static void LoadGeometries() {
        GeometryFiles.clear();

        AddGeometries(BaseGeometriesFolder);
        AddGeometries(SubpackGeometriesFolder);
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