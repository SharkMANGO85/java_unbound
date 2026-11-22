package com.java_unbound.attachables.resolvers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.java_unbound.JavaUnbound;
import com.java_unbound.shared.resourcepack.Folder;
import com.java_unbound.shared.utilities.JsonUtilities;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class AttachableGeometryResolver {
    private static final Map<String, JsonObject> GeometryCache = new HashMap<>();
    private static List<String> MissingGeomtryCache = new ArrayList<>();

    public static HashMap<String, JsonObject> ResolveGeometries(Path AttachablePath) {
        String Identifier = JsonUtilities.GetString("minecraft:attachable.description.identifier", AttachablePath);

        if (Identifier == null) {
            JavaUnbound.LOGGER.error("Could not find minecraft:attachable.description.identifier for " + AttachablePath);
            return new HashMap<>();
        }

        JsonElement GeometriesElement = JsonUtilities.GetElement("minecraft:attachable.description.geometry", AttachablePath);

        if (GeometriesElement == null) {
            JavaUnbound.LOGGER.error("Could not find minecraft:attachable.description.geometry for " + AttachablePath);
            return new HashMap<>();
        }

        LoadGeometries();

        HashMap<String, JsonObject> ResolvedGeometries = new HashMap<>();

        if (GeometriesElement.isJsonPrimitive() && GeometriesElement.getAsJsonPrimitive().isString()) {
            String GeometryPath = GeometriesElement.getAsString();
            JsonObject GeometryObject = GetGeometryObject(GeometryPath);

            if (GeometryObject != null) {
                ResolvedGeometries.put(Identifier, GeometryObject);
            }
        } else if (GeometriesElement.isJsonObject()) {
            for (Map.Entry<String, JsonElement> Entry : GeometriesElement.getAsJsonObject().entrySet()) {
                String GeometryIdentifier = Entry.getKey();
                String GeometryPath = Entry.getValue().getAsString();

                JsonObject GeometryObject = GetGeometryObject(GeometryPath);

                if (GeometryObject == null) {
                    if (!MissingGeomtryCache.contains(GeometryIdentifier)) {
                        MissingGeomtryCache.add(GeometryIdentifier);

                        JavaUnbound.LOGGER.error("Could not resolve geometry: " + GeometryPath);
                    }

                    continue;
                }

                ResolvedGeometries.put(GeometryIdentifier, GeometryObject);
            }
        } else {
            JavaUnbound.LOGGER.error("Invalid minecraft:attachable.description.geometry format for " + AttachablePath);
        }

        return ResolvedGeometries;
    }

    public static void ClearCache() {
        GeometryCache.clear();
        MissingGeomtryCache.clear();
    }

    private static JsonObject GetGeometryObject(String GeometryIdentifier) {
        JsonObject GeometryObject = GeometryCache.get(GeometryIdentifier);

        if (GeometryObject == null) {
            if (!MissingGeomtryCache.contains(GeometryIdentifier)) {
                MissingGeomtryCache.add(GeometryIdentifier);

                JavaUnbound.LOGGER.error("Could not find geometry: {}", GeometryIdentifier);
            }
        }

        return GeometryObject;
    }

    private static void LoadGeometries() {
        if (!GeometryCache.isEmpty()) {return;}

        Path ConfigFolder = Folder.GetAddonFolder();

        if (ConfigFolder == null || !ConfigFolder.toFile().exists()) {
            JavaUnbound.LOGGER.error("Config folder does not exist");
            return;
        }

        Path SubpackGeometryFolder = ConfigFolder.resolve("subpacks").resolve(JavaUnbound.SUB_PACK).resolve("models");
        Path BaseGeometryFolder = ConfigFolder.resolve("models");

        LoadGeometryFolder(SubpackGeometryFolder);
        LoadGeometryFolder(BaseGeometryFolder);
    }

    private static void LoadGeometryFolder(Path GeometryFolder) {
        if (!Files.isDirectory(GeometryFolder)) {return;}

        try (Stream<Path> Paths = Files.walk(GeometryFolder)) {
            Paths.filter(Files::isRegularFile)
                    .filter(JsonFile -> JsonFile.toString().endsWith(".json"))
                    .forEach(JsonFile -> {
                        JsonElement GeometriesElement = JsonUtilities.GetElement("minecraft:geometry", JsonFile);

                        if (GeometriesElement == null || !GeometriesElement.isJsonArray()) {return;}

                        for (JsonElement GeometryElement : GeometriesElement.getAsJsonArray()) {
                            CacheGeometry(GeometryElement);
                        }
                    });
        } catch (IOException Exception) {
            JavaUnbound.LOGGER.error("Failed to load geometries from: {}", GeometryFolder, Exception);
        }
    }

    private static void CacheGeometry(JsonElement GeometryElement) {
        if (!GeometryElement.isJsonObject()) {return;}

        JsonObject GeometryObject = GeometryElement.getAsJsonObject();
        JsonElement DescriptionElement = GeometryObject.get("description");

        if (DescriptionElement == null || !DescriptionElement.isJsonObject()) {return;}

        JsonElement IdentifierElement = DescriptionElement.getAsJsonObject().get("identifier");

        if (IdentifierElement == null || !IdentifierElement.isJsonPrimitive()) {return;}

        String GeometryIdentifier = IdentifierElement.getAsString();

        if (GeometryIdentifier.isBlank()) {return;}

        if (GeometryCache.putIfAbsent(GeometryIdentifier, GeometryObject) == null) {
            JavaUnbound.LOGGER.debug("Cached geometry: {}", GeometryIdentifier);
        }
    }
}