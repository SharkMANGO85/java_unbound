package com.java_unbound.loader.files;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.java_unbound.JavaUnbound;
import com.java_unbound.loader.definitions.attachable.AttachableDefinition;
import com.java_unbound.loader.definitions.entity.resolvers.EntityGeometryResolver;
import com.java_unbound.loader.definitions.entity.types.geometry.EntityGeometryDefinition;
import com.java_unbound.loader.resourcepack.Folder;
import com.java_unbound.utils.variables.HashMaps;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AttachableGeometryLoader {
    private static final Gson PrettyGson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public static Path GetOutputFolder() {
        return Folder.GetConfigFolder().resolve("assets").resolve("geometries").resolve("attachables");
    }

    public static void LoadAttachableGeometryFiles() {
        Path OutputFolder = GetOutputFolder();

        try {
            Files.createDirectories(OutputFolder);
        } catch (IOException Exception) {
            JavaUnbound.LOGGER.error("Failed to create attachable geometry folder: {}", OutputFolder, Exception);
            return;
        }

        Set<String> Written = new HashSet<>();

        for (AttachableDefinition Attachable : HashMaps.Attachables.values()) {
            if (Attachable == null || Attachable.Geomtries == null) {continue;}

            for (Map.Entry<String, EntityGeometryDefinition> Entry : Attachable.Geomtries.entrySet()) {
                EntityGeometryDefinition Geometry = Entry.getValue();
                if (Geometry == null) {continue;}

                String Identifier = Geometry.Identifier != null ? Geometry.Identifier : Entry.getKey();
                if (Identifier == null || Identifier.isBlank()) {continue;}
                if (!Written.add(Identifier)) {continue;}

                WriteGeometryFile(OutputFolder, Identifier);
            }
        }

        JavaUnbound.LOGGER.debug("Wrote attachable geometry files | Count: {} | Folder: {}", Written.size(), OutputFolder);
    }

    private static void WriteGeometryFile(Path OutputFolder, String Identifier) {
        JsonObject RawGeometry = EntityGeometryResolver.GetGeometryJson(Identifier);

        if (RawGeometry == null) {
            JavaUnbound.LOGGER.error("Could not write geometry file, raw JSON not found: {}", Identifier);
            return;
        }

        JsonArray GeometryArray = new JsonArray();
        GeometryArray.add(RawGeometry);

        JsonObject Root = new JsonObject();
        Root.addProperty("format_version", "1.12.0");
        Root.add("minecraft:geometry", GeometryArray);

        Path OutputFile = OutputFolder.resolve(ToFileName(Identifier) + ".json");

        try {
            Files.writeString(OutputFile, PrettyGson.toJson(Root));

            String FileName = ToFileName(Identifier) + ".json";

            Folder.SetIdentifier("geometries/attachables/" + FileName, "assets/geometries/attachables/" + FileName);

            JavaUnbound.LOGGER.debug("Wrote geometry file: {} -> {}", Identifier, OutputFile);
        } catch (IOException Exception) {
            JavaUnbound.LOGGER.error("Failed to write geometry file: {}", OutputFile, Exception);
        }
    }

    private static String ToFileName(String Identifier) {
        return Identifier.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}