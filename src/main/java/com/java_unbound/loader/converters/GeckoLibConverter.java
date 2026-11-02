
package com.java_unbound.loader.converters;

import com.google.gson.*;
import com.java_unbound.JavaUnbound;
import com.java_unbound.loader.definitions.entity.EntityDefinition;
import com.java_unbound.loader.definitions.entity.reolvers.EntityGeometryResolver;
import com.java_unbound.loader.definitions.entity.types.geometry.EntityGeometryDefinition;
import com.java_unbound.loader.resourcepack.Folder;
import com.java_unbound.utils.variables.HashMaps;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GeckoLibConverter {
    private static final Gson PrettyGson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final Pattern ArrayPattern = Pattern.compile("\\[(.*?)\\]", Pattern.DOTALL);

    public static String FormatJson(JsonElement Data) {
        String Text = PrettyGson.toJson(Data);

        Matcher Matcher = ArrayPattern.matcher(Text);
        StringBuffer Formatted = new StringBuffer();

        while (Matcher.find()) {
            String Content = Matcher.group(1);

            if (Content.contains("{") || Content.contains("}") || Content.contains("[") || Content.contains("]")) {
                Matcher.appendReplacement(Formatted, Matcher.quoteReplacement(Matcher.group(0)));
                continue;
            }

            Content = Content.replaceAll("\\s*,\\s*", ",").trim();
            String CollapsedArray = "[" + Content + "]";

            Matcher.appendReplacement(Formatted, Matcher.quoteReplacement(CollapsedArray));
        }

        Matcher.appendTail(Formatted);

        return Formatted + "\n";
    }

    public static void LoadResources() throws IOException {
        ClearResources();

        HashSet<String> ExportedGeometries = new HashSet<>();

        for (EntityDefinition EntityRegistry : HashMaps.Entities.values()) {
            for (EntityGeometryDefinition Geometry : EntityRegistry.Geometries.values()) {
                if (!ExportedGeometries.add(Geometry.Identifier)) {continue;}

                JsonObject GeometryObject = EntityGeometryResolver.GetGeometryJson(Geometry.Identifier);

                if (GeometryObject == null) {
                    JavaUnbound.LOGGER.error("Could not find geometry JSON: {}", Geometry.Identifier);
                    continue;
                }

                JsonObject RootObject = new JsonObject();
                RootObject.addProperty("format_version", "1.12.0");

                JsonArray Geometries = new JsonArray();
                Geometries.add(GeometryObject);

                RootObject.add("minecraft:geometry", Geometries);

                JsonObject Description = GeometryObject.getAsJsonObject("description");
                String Identifier = Description.get("identifier").getAsString();

                Path Config = Folder.GetConfigsFolder();
                if (Config == null) {continue;}

                Path GeometryFolder = Folder.GetConfigFolder().resolve("assets").resolve("geckolib").resolve("resources").resolve("geometries");
                Files.createDirectories(GeometryFolder);
                Path GeometryFile = GeometryFolder.resolve(Identifier + ".geo.json");
                Files.writeString(GeometryFile, FormatJson(RootObject));
                Folder.SetIdentifier("geckolib/models/entity/" + Identifier + ".geo.json", "assets/geckolib/resources/geometries/" + Identifier + ".geo.json");
            }
        }
    }

    public static void ClearResources() throws IOException {
        Path Config = Folder.GetConfigsFolder();
        if (Config == null) {return;}

        Path GeometryFolder = Config.resolve("java_unbound_mod").resolve("assets").resolve("geckolib").resolve("resources").resolve("geometries");

        Files.createDirectories(GeometryFolder);

        try (var Paths = Files.walk(GeometryFolder)) {
            Paths.filter(Path -> !Path.equals(GeometryFolder))
                    .sorted(Comparator.reverseOrder())
                    .forEach(Path -> {
                        try {
                            Files.deleteIfExists(Path);
                        } catch (IOException Exception) {
                            throw new RuntimeException(Exception);
                        }
                    });
        }
    }
}
