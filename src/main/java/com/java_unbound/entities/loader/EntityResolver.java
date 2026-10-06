package com.java_unbound.entities.loader;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.java_unbound.JavaUnbound;
import com.java_unbound.loader.definitions.global_use.GeometriesDefinition;
import com.java_unbound.loader.definitions.global_use.RenderControllersDefinition;
import com.java_unbound.loader.definitions.global_use.TexturesDefinition;

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
        JsonElement Element = Description.get("render_controllers");

        if (Element == null || !Element.isJsonArray()) {
            JavaUnbound.LOGGER.error("RENDER CONTROLLER: Entity has no render controllers array: " + Identifier);
            return new HashMap<>();
        }

        HashMap<String, JsonElement> Resolved = new HashMap<>();

        for (JsonElement Controller : Element.getAsJsonArray()) {
            if (Controller.isJsonPrimitive()) {
                ResolveRenderController(Controller.getAsString(), Identifier, Resolved);
            } else if (Controller.isJsonObject()) {
                for (String ControllerIdentifier : Controller.getAsJsonObject().keySet()) {
                    ResolveRenderController(ControllerIdentifier, Identifier, Resolved);
                }
            }
        }
        return Resolved;
    }

    private static void ResolveRenderController(String Identifier, String EntityIdentifier, HashMap<String, JsonElement> Resolved) {
        JsonElement Controller = RenderControllersDefinition.GetRenderControllerByIdentifier(Identifier);

        if (Controller == null) {
            JavaUnbound.LOGGER.error("RENDER CONTROLLER: Could not resolve render controller '{}' for entity '{}'", Identifier, EntityIdentifier);
            return;
        }

        Resolved.put(Identifier, Controller);
    }

    public static HashMap<String, JsonArray> GetScripts(String Identifier, JsonObject Description) {
        JsonElement ScriptsElement = Description.get("scripts");

        if (ScriptsElement == null || !ScriptsElement.isJsonObject()) {
            JavaUnbound.LOGGER.error("SCRIPTS: Entity has no scripts object: " + Identifier);
            return new HashMap<>();
        }

        JsonObject Scripts = ScriptsElement.getAsJsonObject();
        HashMap<String, JsonArray> ResolvedScripts = new HashMap<>();

        for (Map.Entry<String, JsonElement> Entry : Scripts.entrySet()) {
            String ScriptName = Entry.getKey();
            JsonElement ScriptElement = Entry.getValue();
            JsonArray ScriptArray = ScriptElement.isJsonArray() ? ScriptElement.getAsJsonArray() : new JsonArray();

            if (!ScriptElement.isJsonArray()) {
                ScriptArray.add(ScriptElement);
            }

            ResolvedScripts.put(ScriptName, ScriptArray);
        }

        return ResolvedScripts;
    }
}