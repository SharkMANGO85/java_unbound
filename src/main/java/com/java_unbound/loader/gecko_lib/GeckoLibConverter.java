
package com.java_unbound.loader.gecko_lib;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.java_unbound.JavaUnbound;
import com.java_unbound.entities.loaders.EntityRegistry;
import com.java_unbound.utils.variables.HashMaps;
import com.java_unbound.loader.resourcepack.Folder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

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

        for (EntityRegistry EntityRegistry : HashMaps.EntityRegistries.values()) {
            for (JsonElement Geometry : EntityRegistry.Geometries.values()) {
                if (!Geometry.isJsonObject()) {continue;}

                JsonObject GeometryObject = Geometry.getAsJsonObject();
                JsonObject RootObject = GeometryObject;
                JsonObject GeometryData = GeometryObject;

                JsonElement GeometryArrayElement = GeometryObject.get("minecraft:geometry");

                if (GeometryArrayElement != null && GeometryArrayElement.isJsonArray()) {
                    JsonArray Geometries = GeometryArrayElement.getAsJsonArray();

                    if (Geometries.isEmpty() || !Geometries.get(0).isJsonObject()) {
                        JavaUnbound.LOGGER.error("Geometry array is empty or invalid");
                        continue;
                    }

                    GeometryData = Geometries.get(0).getAsJsonObject();
                }

                JsonElement DescriptionElement = GeometryData.get("description");

                if (DescriptionElement == null || !DescriptionElement.isJsonObject()) {
                    JavaUnbound.LOGGER.error("Geometry description is missing");
                    continue;
                }

                JsonObject Description = DescriptionElement.getAsJsonObject();
                JsonElement IdentifierElement = Description.get("identifier");

                if (IdentifierElement == null || !IdentifierElement.isJsonPrimitive()) {
                    JavaUnbound.LOGGER.error("Geometry identifier is missing");
                    continue;
                }

                String Identifier = IdentifierElement.getAsString();

                if (Identifier.isEmpty()) {
                    JavaUnbound.LOGGER.error("Geometry identifier is empty");
                    continue;
                }

                Path Config = Folder.GetConfigsFolder();
                if (Config == null) {continue;}

                Path GeometryFolder = Config.resolve("java_unbound_mod").resolve("assets").resolve("geckolib").resolve("resources").resolve("geometries");

                Files.createDirectories(GeometryFolder);

                Path GeometryFile = GeometryFolder.resolve(Identifier + ".geo.json");

                Files.writeString(GeometryFile, FormatJson(RootObject));
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
