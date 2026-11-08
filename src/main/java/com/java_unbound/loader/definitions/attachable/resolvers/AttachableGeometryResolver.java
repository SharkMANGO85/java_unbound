package com.java_unbound.loader.definitions.attachable.resolvers;

import com.java_unbound.JavaUnbound;
import com.java_unbound.loader.definitions.entity.resolvers.EntityGeometryResolver;
import com.java_unbound.loader.definitions.entity.types.geometry.EntityGeometryDefinition;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public final class AttachableGeometryResolver {
    private AttachableGeometryResolver() {}

    public static synchronized HashMap<String, EntityGeometryDefinition> GetAttachableGeometries(Path JsonFile) {
        HashMap<String, EntityGeometryDefinition> Geometries = new HashMap<>();

        try {
            JsonObject Root = JsonParser.parseString(Files.readString(JsonFile)).getAsJsonObject();
            JsonObject Attachable = Root.getAsJsonObject("minecraft:attachable");
            if (Attachable == null) {return Geometries;}

            JsonObject Description = Attachable.getAsJsonObject("description");
            if (Description == null) {return Geometries;}

            JsonElement GeometryElement = Description.get("geometry");
            if (GeometryElement == null) {return Geometries;}

            JsonObject GeometryMappings;

            if (GeometryElement.isJsonPrimitive()) {
                GeometryMappings = new JsonObject();
                GeometryMappings.add("default", GeometryElement);
            } else if (GeometryElement.isJsonObject()) {
                GeometryMappings = GeometryElement.getAsJsonObject();
            } else {
                return Geometries;
            }

            for (Map.Entry<String, JsonElement> Entry : GeometryMappings.entrySet()) {
                if (!Entry.getValue().isJsonPrimitive()) {continue;}

                String GeometryAlias = Entry.getKey();
                String GeometryIdentifier = Entry.getValue().getAsString();

                EntityGeometryDefinition Geometry = EntityGeometryResolver.GetGeometry(GeometryIdentifier);
                if (Geometry == null) {
                    JavaUnbound.LOGGER.debug("Could not resolve attachable geometry: {} | Alias: {} | File: {}", GeometryIdentifier, GeometryAlias, JsonFile);
                    continue;
                }

                Geometries.put(GeometryAlias, Geometry);
            }
        } catch (Exception Exception) {
            JavaUnbound.LOGGER.error("Failed to resolve attachable geometries: {}", JsonFile, Exception);
        }

        return Geometries;
    }
}