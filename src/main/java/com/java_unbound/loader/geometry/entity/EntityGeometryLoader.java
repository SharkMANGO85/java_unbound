package com.java_unbound.loader.geometry.entity;

import com.google.gson.*;
import com.java_unbound.JavaUnbound;
import com.java_unbound.loader.resourcepack.Folder;
import com.java_unbound.loader.resourcepack.FolderResources;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public class EntityGeometryLoader {
    private static final String GeometryNamespace = "geometry.oreville_ans.";

    private static final Path EntityFolder = Folder.GetResourceFolder().resolve("entity");
    private static final Path EntityGeometryFolder = Folder.GetResourceFolder().resolve("models").resolve("entity");

    private static final Path Subpack0EntityGeometryFolder = Folder.GetResourceFolder().resolve("subpacks").resolve("SP0").resolve("models").resolve("entity");
    private static final Path Subpack1EntityGeometryFolder = Folder.GetResourceFolder().resolve("subpacks").resolve("SP1").resolve("models").resolve("entity");
    private static final Path Subpack2EntityGeometryFolder = Folder.GetResourceFolder().resolve("subpacks").resolve("SP2").resolve("models").resolve("entity");

    private static final Path BigJsonGeometryFile1 = EntityGeometryFolder.resolve("93ce12b8.json");
    private static final Path BigJsonGeometryFile2 = EntityGeometryFolder.resolve("93ce21bc.json");
    private static final Path BigJsonGeometryFile3 = EntityGeometryFolder.resolve("93ce8356.json");

    private static final Path BigSubpack0JsonGeometryFile1 = Subpack0EntityGeometryFolder.resolve("17f4ac38.json");
    private static final Path BigSubpack0JsonGeometryFile2 = Subpack0EntityGeometryFolder.resolve("17f43b9a.json");
    private static final Path BigSubpack0JsonGeometryFile3 = Subpack0EntityGeometryFolder.resolve("33219e66.json");

    private static final Path BigSubpack1JsonGeometryFile1 = Subpack1EntityGeometryFolder.resolve("25e40acc.json");
    private static final Path BigSubpack1JsonGeometryFile2 = Subpack1EntityGeometryFolder.resolve("25e47b6a.json");
    private static final Path BigSubpack1JsonGeometryFile3 = Subpack1EntityGeometryFolder.resolve("41116d98.json");

    private static final Path BigSubpack2JsonGeometryFile1 = Subpack2EntityGeometryFolder.resolve("8e9c963c.json");
    private static final Path BigSubpack2JsonGeometryFile2 = Subpack2EntityGeometryFolder.resolve("8e9ca540.json");
    private static final Path BigSubpack2JsonGeometryFile3 = Subpack2EntityGeometryFolder.resolve("8e9d06da.json");

    public static void LoadEntityGeometries() throws IOException {
        CreateEntitiesFolder();

        if (!Files.exists(EntityFolder)) {
            return;
        }

        try (DirectoryStream<Path> JsonFiles = Files.newDirectoryStream(EntityFolder, "*.json")) {
            for (Path File : JsonFiles) {
                if (java.nio.file.Files.isRegularFile(File)) {
                    ReadJsonFile(File);
                }
            }
        }
    }

    private static String GetValue(Path File, String Path) throws IOException {
        String Json = Files.readString(File);
        JsonElement Element = JsonParser.parseString(Json);

        for (String Key : Path.split("\\.")) {
            if (!Element.isJsonObject()) {
                return null;
            }

            JsonObject Object = Element.getAsJsonObject();

            if (!Object.has(Key)) {
                return null;
            }

            Element = Object.get(Key);
        }

        if (Element.isJsonPrimitive()) {
            return Element.getAsString();
        }

        return Element.toString();
    }

    private static String GetEntityName(String Entity) {
        if (Entity == null || Entity.isBlank()) {
            return null;
        }

        return Entity.replace("minecraft:", "");
    }

    private static void CreateEntitiesFolder() throws IOException {
        Path ResourcePack = Folder.GetResourceFolder();
        FolderResources.CreateFolder(ResourcePack, "assets/minecraft/optifine/cem");
    }

    private static void CreateEntityFolder(String Entity) throws IOException {
        Path ResourcePack = Folder.GetResourceFolder();
        FolderResources.CreateFolder(ResourcePack, "assets/minecraft/optifine/cem/" + Entity);
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

    private static boolean GeometryIdentifiersMatch(String First, String Second) {
        String NormalizedFirst = NormalizeGeometryIdentifier(First);
        String NormalizedSecond = NormalizeGeometryIdentifier(Second);

        return !NormalizedFirst.isBlank() && NormalizedFirst.equals(NormalizedSecond);
    }

    private static Path Search(Path Name, Path Folder) {
        if (Name == null || Folder == null || !Files.exists(Folder)) {
            return null;
        }

        String Target = Name.toString();

        try (var Paths = Files.walk(Folder)) {
            return Paths
                    .filter(java.nio.file.Files::isRegularFile)
                    .filter(File -> {
                        String FileName = File.getFileName().toString();

                        if (FileName.equals(Target)) {
                            return true;
                        }

                        if (!Target.endsWith(".json") && FileName.equals(Target + ".json")) {
                            return true;
                        }

                        if (!FileName.toLowerCase().endsWith(".json")) {
                            return false;
                        }

                        try {
                            String Json = Files.readString(File);
                            JsonObject Root = JsonParser.parseString(Json).getAsJsonObject();
                            JsonArray Geometries = Root.getAsJsonArray("minecraft:geometry");

                            if (Geometries == null) {
                                return false;
                            }

                            for (JsonElement GeometryElement : Geometries) {
                                if (!GeometryElement.isJsonObject()) {
                                    continue;
                                }

                                JsonObject Geometry = GeometryElement.getAsJsonObject();
                                JsonObject Description = Geometry.getAsJsonObject("description");

                                if (Description == null) {
                                    continue;
                                }

                                JsonElement IdentifierElement = Description.get("identifier");

                                if (IdentifierElement == null || !IdentifierElement.isJsonPrimitive()) {
                                    continue;
                                }

                                String Identifier = IdentifierElement.getAsString();

                                if (GeometryIdentifiersMatch(Identifier, Target)) {
                                    return true;
                                }
                            }
                        } catch (Exception Ignored) {
                        }

                        return false;
                    })
                    .findFirst()
                    .orElse(null);

        } catch (IOException E) {
            JavaUnbound.LOGGER.error("Failed to search for '{}' in '{}'", Target, Folder, E);
            return null;
        }
    }

    private static JsonObject SearchInBigFile(String Identifier, Path BigFile) {
        if (Identifier == null || Identifier.isBlank() || BigFile == null || !Files.isRegularFile(BigFile)) {
            return null;
        }

        try {
            String Json = Files.readString(BigFile);
            JsonObject Root = JsonParser.parseString(Json).getAsJsonObject();
            JsonArray Geometries = Root.getAsJsonArray("minecraft:geometry");

            if (Geometries == null) {
                return null;
            }

            for (JsonElement GeometryElement : Geometries) {
                if (!GeometryElement.isJsonObject()) {
                    continue;
                }

                JsonObject Geometry = GeometryElement.getAsJsonObject();
                JsonObject Description = Geometry.getAsJsonObject("description");

                if (Description == null) {
                    continue;
                }

                JsonElement IdentifierElement = Description.get("identifier");

                if (IdentifierElement == null || !IdentifierElement.isJsonPrimitive()) {
                    continue;
                }

                String GeometryIdentifier = IdentifierElement.getAsString();

                if (!GeometryIdentifiersMatch(GeometryIdentifier, Identifier)) {
                    continue;
                }

                JavaUnbound.LOGGER.debug("Found geometry '{}' in '{}'", GeometryIdentifier, BigFile);
                return Geometry.deepCopy();
            }

        } catch (Exception E) {
            JavaUnbound.LOGGER.error("Failed to search geometry '{}' in '{}'", Identifier, BigFile, E);
        }

        return null;
    }

    private static JsonObject GetGeometryFromFile(Path File, String TargetIdentifier) {
        if (File == null || !Files.isRegularFile(File)) {
            return null;
        }

        try {
            String Json = Files.readString(File);
            JsonObject Root = JsonParser.parseString(Json).getAsJsonObject();
            JsonArray Geometries = Root.getAsJsonArray("minecraft:geometry");

            if (Geometries == null) {
                return null;
            }

            for (JsonElement GeometryElement : Geometries) {
                if (!GeometryElement.isJsonObject()) {
                    continue;
                }

                JsonObject Geometry = GeometryElement.getAsJsonObject();
                JsonObject Description = Geometry.getAsJsonObject("description");

                if (Description == null) {
                    continue;
                }

                JsonElement IdentifierElement = Description.get("identifier");

                if (IdentifierElement == null || !IdentifierElement.isJsonPrimitive()) {
                    continue;
                }

                String GeometryIdentifier = IdentifierElement.getAsString();

                if (GeometryIdentifiersMatch(GeometryIdentifier, TargetIdentifier)) {
                    return Geometry.deepCopy();
                }
            }

        } catch (Exception E) {
            JavaUnbound.LOGGER.error("Failed to read geometry file '{}'", File, E);
        }

        return null;
    }

    private static void CopyEntityGeometries(Path File, String Entity) throws IOException {
        String GeometriesJson = GetValue(File, "minecraft:client_entity.description.geometry");

        if (GeometriesJson == null || GeometriesJson.isBlank()) {
            return;
        }

        JsonObject Geometries;

        try {
            Geometries = JsonParser.parseString(GeometriesJson).getAsJsonObject();
        } catch (Exception E) {
            JavaUnbound.LOGGER.error("Failed to parse geometry JSON from {}", File, E);
            return;
        }

        Path ResourcePack = Folder.GetResourceFolder();

        Path DestinationFolder = ResourcePack
                .resolve("assets")
                .resolve("minecraft")
                .resolve("optifine")
                .resolve("cem")
                .resolve(Entity);

        Files.createDirectories(DestinationFolder);

        Set<String> CopiedGeometries = new HashSet<>();

        Gson Gson = new GsonBuilder().setPrettyPrinting().create();

        for (String GeometryKey : Geometries.keySet()) {
            JsonElement GeometryElement = Geometries.get(GeometryKey);

            if (!GeometryElement.isJsonPrimitive()) {
                continue;
            }

            String GeometryIdentifier = GeometryElement.getAsString();

            if (GeometryIdentifier == null || GeometryIdentifier.isBlank()) {
                continue;
            }

            String NormalizedIdentifier = NormalizeGeometryIdentifier(GeometryIdentifier);

            if (!CopiedGeometries.add(NormalizedIdentifier)) {
                continue;
            }

            String GeometryFileName = NormalizedIdentifier;

            if (!GeometryFileName.endsWith(".json")) {
                GeometryFileName += ".json";
            }

            Path Destination = DestinationFolder.resolve(GeometryFileName);

            JsonObject Geometry = null;

            Path GeometryPath = Path.of(GeometryIdentifier);

            Path Source = Search(GeometryPath, EntityGeometryFolder);

            if (Source == null) {
                Source = Search(GeometryPath, Subpack0EntityGeometryFolder);
            }

            if (Source == null) {
                Source = Search(GeometryPath, Subpack1EntityGeometryFolder);
            }

            if (Source == null) {
                Source = Search(GeometryPath, Subpack2EntityGeometryFolder);
            }

            if (Source != null) {
                Geometry = GetGeometryFromFile(Source, GeometryIdentifier);

                if (Geometry != null) {
                    JavaUnbound.LOGGER.debug("Found geometry '{}' in '{}'", GeometryIdentifier, Source);
                }
            }

            if (Geometry == null) {
                Path[] BigFiles = {
                        BigJsonGeometryFile1,
                        BigJsonGeometryFile2,
                        BigJsonGeometryFile3,
                        BigSubpack0JsonGeometryFile1,
                        BigSubpack0JsonGeometryFile2,
                        BigSubpack0JsonGeometryFile3,
                        BigSubpack1JsonGeometryFile1,
                        BigSubpack1JsonGeometryFile2,
                        BigSubpack1JsonGeometryFile3,
                        BigSubpack2JsonGeometryFile1,
                        BigSubpack2JsonGeometryFile2,
                        BigSubpack2JsonGeometryFile3
                };

                for (Path BigFile : BigFiles) {
                    Geometry = SearchInBigFile(GeometryIdentifier, BigFile);

                    if (Geometry != null) {
                        break;
                    }
                }
            }

            if (Geometry == null) {
                JavaUnbound.LOGGER.error("Geometry not found: {}", GeometryIdentifier);
                continue;
            }

            Files.writeString(Destination, CreateGeometryFile(Geometry, Gson));

            JavaUnbound.LOGGER.debug("Copied geometry '{}' -> '{}'", GeometryIdentifier, Destination);
        }
    }

    private static String CreateGeometryFile(JsonObject Geometry, Gson Gson) {
        JsonObject Result = new JsonObject();
        Result.addProperty("format_version", "1.12.0");

        JsonArray Geometries = new JsonArray();
        Geometries.add(Geometry.deepCopy());

        Result.add("minecraft:geometry", Geometries);

        return Gson.toJson(Result);
    }

    private static void ReadJsonFile(Path File) {
        try {
            String EntityIdentifier = GetValue(File, "minecraft:client_entity.description.identifier");

            if (EntityIdentifier == null || EntityIdentifier.isBlank()) {
                JavaUnbound.LOGGER.error("Entity identifier missing in '{}'", File);
                return;
            }

            String EntityName = GetEntityName(EntityIdentifier);

            if (EntityName == null || EntityName.isBlank()) {
                return;
            }

            CreateEntityFolder(EntityName);
            CopyEntityGeometries(File, EntityName);

        } catch (IOException E) {
            JavaUnbound.LOGGER.error("Failed to process entity file '{}'", File, E);
        }
    }
}