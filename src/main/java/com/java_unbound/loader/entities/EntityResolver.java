package com.java_unbound.loader.entities;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.java_unbound.JavaUnbound;
import com.java_unbound.loader.definitions.EntitiesDefinition;
import com.java_unbound.loader.definitions.GeometriesDefinition;
import com.java_unbound.loader.definitions.RenderControllersDefinition;
import com.java_unbound.loader.definitions.TexturesDefinition;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class EntityResolver {
    public static HashMap<String, Path> GetTextures(String Identifier, JsonObject Description) {
        JsonElement TexturesElement = Description.get("textures");

        if (TexturesElement == null || !TexturesElement.isJsonObject()) {
            JavaUnbound.LOGGER.error("TEXTURES: Entity has no textures object: " + Identifier);
            return new HashMap<>();
        }

        JsonObject Textures = TexturesElement.getAsJsonObject();
        HashMap<String, Path> ResolvedTextures = new HashMap<>();

        for (Map.Entry<String, JsonElement> Entry : Textures.entrySet()) {
            String Key = Entry.getKey();
            String TexturePath = Entry.getValue().getAsString();
            Path Texture = TexturesDefinition.GetTextureByOrevillePath(TexturePath);

            if (Texture == null) {
                JavaUnbound.LOGGER.error("TEXTURES: Could not resolve texture '{}' for entity '{}'", TexturePath, Identifier);
                continue;
            }

            ResolvedTextures.put(Key, Texture);
        }

        return ResolvedTextures;
    }

    public static HashMap<String, JsonElement> GetGeometries(String Identifier, JsonObject Description) {
        JsonElement GeometriesElement = Description.get("geometry");

        if (GeometriesElement == null || !GeometriesElement.isJsonObject()) {
            JavaUnbound.LOGGER.error("GEOMETRIES: Entity has no geometries object: " + Identifier);
            return new HashMap<>();
        }

        JsonObject Geometries = GeometriesElement.getAsJsonObject();
        HashMap<String, JsonElement> ResolvedGeometries = new HashMap<>();

        for (Map.Entry<String, JsonElement> Entry : Geometries.entrySet()) {
            String Key = Entry.getKey();
            String GeometryPath = Entry.getValue().getAsString();
            JsonElement Geometry = GeometriesDefinition.GetGeometryByIdentifier(GeometryPath);

            if (Geometry == null) {
                JavaUnbound.LOGGER.error("GEOMETRIES: Could not resolve geometry '{}' for entity '{}'", GeometryPath, Identifier);
                continue;
            }

            ResolvedGeometries.put(Key, Geometry);
        }

        return ResolvedGeometries;
    }

    public static HashMap<String, JsonElement> GetRenderControllers(String Identifier, JsonObject Description) {
        JsonElement RenderControllersElement = Description.get("render_controllers");

        if (RenderControllersElement == null || !RenderControllersElement.isJsonArray()) {
            JavaUnbound.LOGGER.error("RENDER CONTROLLER: Entity has no render controllers array: " + Identifier);
            return new HashMap<>();
        }

        JsonArray RenderControllers = RenderControllersElement.getAsJsonArray();
        HashMap<String, JsonElement> ResolvedRenderControllers = new HashMap<>();

        for (JsonElement RenderControllerElement : RenderControllers) {
            if (RenderControllerElement.isJsonPrimitive()) {
                String RenderControllerIdentifier = RenderControllerElement.getAsString();
                JsonElement RenderController = RenderControllersDefinition.GetRenderControllerByIdentifier(RenderControllerIdentifier);

                if (RenderController == null) {
                    JavaUnbound.LOGGER.error("RENDER CONTROLLER: Could not resolve render controller '{}' for entity '{}'", RenderControllerIdentifier, Identifier);
                    continue;
                }

                ResolvedRenderControllers.put(RenderControllerIdentifier, RenderController);
                continue;
            }

            if (RenderControllerElement.isJsonObject()) {
                JsonObject ConditionalController = RenderControllerElement.getAsJsonObject();

                for (Map.Entry<String, JsonElement> Entry : ConditionalController.entrySet()) {
                    String RenderControllerIdentifier = Entry.getKey();
                    JsonElement Condition = Entry.getValue();
                    JsonElement RenderController = RenderControllersDefinition.GetRenderControllerByIdentifier(RenderControllerIdentifier);

                    if (RenderController == null) {
                        JavaUnbound.LOGGER.error("RENDER CONTROLLER: Could not resolve render controller '{}' for entity '{}'", RenderControllerIdentifier, Identifier);
                        continue;
                    }

                    ResolvedRenderControllers.put(RenderControllerIdentifier, RenderController);
                }
            }
        }

        return ResolvedRenderControllers;
    }
}