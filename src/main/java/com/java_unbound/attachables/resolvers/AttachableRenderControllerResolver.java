
package com.java_unbound.attachables.resolvers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.java_unbound.JavaUnbound;
import com.java_unbound.attachables.utilities.AttachableRenderControllerUtilities;
import com.java_unbound.shared.definitions.attachables.AttachableRenderControllerDefinition;
import com.java_unbound.shared.resourcepack.Folder;
import com.java_unbound.shared.utilities.JsonUtilities;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public class AttachableRenderControllerResolver {
    private static final Map<String, JsonObject> RenderControllerCache = new HashMap<>();
    private static final Set<String> MissingGeomtryCache = new HashSet<>();

    public static HashMap<String, AttachableRenderControllerDefinition> ResolveRenderControllers(Path AttachablePath) {
        String Identifier = JsonUtilities.GetString("minecraft:attachable.description.identifier", AttachablePath);

        if (Identifier == null) {
            JavaUnbound.LOGGER.error("Could not find minecraft:attachable.description.identifier for " + AttachablePath);
            return new HashMap<>();
        }

        JsonElement RenderControllerElement = JsonUtilities.GetElement("minecraft:attachable.description.render_controllers", AttachablePath);

        if (RenderControllerElement == null) {
            JavaUnbound.LOGGER.error("Could not find minecraft:attachable.description.render_controllers for " + AttachablePath);
            return new HashMap<>();
        }

        LoadRenderControllers();

        HashMap<String, AttachableRenderControllerDefinition> ResolvedRenderControllers = new HashMap<>();

        if (RenderControllerElement.isJsonArray()) {
            JsonArray RenderControllerArray = RenderControllerElement.getAsJsonArray();

            for (JsonElement Element : RenderControllerArray) {
                if (Element.isJsonPrimitive() && Element.getAsJsonPrimitive().isString()) {
                    String RenderControllerIdentifier = Element.getAsString();

                    ResolveRenderController(RenderControllerIdentifier, RenderControllerIdentifier, ResolvedRenderControllers, null);
                } else if (Element.isJsonObject()) {
                    for (Map.Entry<String, JsonElement> Entry : Element.getAsJsonObject().entrySet()) {
                        String RenderControllerIdentifier = Entry.getKey();
                        String RenderControllerCondition = Entry.getValue().getAsString();

                        ResolveRenderController(RenderControllerIdentifier, RenderControllerIdentifier, ResolvedRenderControllers, RenderControllerCondition);
                    }
                } else {
                    JavaUnbound.LOGGER.error("Invalid render_controller entry for " + AttachablePath + ": " + Element);
                }
            }
        } else if (RenderControllerElement.isJsonPrimitive() && RenderControllerElement.getAsJsonPrimitive().isString()) {
            JavaUnbound.LOGGER.warn("Molang render_controllers expression cannot be resolved statically for {}: {}", AttachablePath, RenderControllerElement.getAsString());
        } else {
            JavaUnbound.LOGGER.error("Invalid minecraft:attachable.description.render_controllers format for " + AttachablePath);
        }

        return ResolvedRenderControllers;
    }

    private static void ResolveRenderController(String RenderControllerIdentifier, String CacheIdentifier, HashMap<String, AttachableRenderControllerDefinition> ResolvedRenderControllers, String RenderControllerCondition) {
        JsonObject RenderControllerObject = GetRenderControllerObject(RenderControllerIdentifier);

        if (RenderControllerObject == null) {
            if (MissingGeomtryCache.add(CacheIdentifier)) {
                JavaUnbound.LOGGER.error("Could not resolve render_controller: {}", RenderControllerIdentifier);
            }

            return;
        }

        AttachableRenderControllerDefinition Definition = new AttachableRenderControllerDefinition();

        Definition.Condition = RenderControllerCondition;
        Definition.RenderControllerObject = RenderControllerObject;

        ResolvedRenderControllers.put(RenderControllerIdentifier, Definition);
    }

    public static void ClearCache() {
        RenderControllerCache.clear();
        MissingGeomtryCache.clear();
    }

    private static JsonObject GetRenderControllerObject(String RenderControllerIdentifier) {
        return RenderControllerCache.get(RenderControllerIdentifier);
    }

    private static void LoadRenderControllers() {
        if (!RenderControllerCache.isEmpty()) {return;}

        Path ConfigFolder = Folder.GetAddonFolder();

        if (ConfigFolder == null || !Files.isDirectory(ConfigFolder)) {
            JavaUnbound.LOGGER.error("Config folder does not exist");
            return;
        }

        Path SubpackRenderControllerFolder = ConfigFolder.resolve("subpacks").resolve(JavaUnbound.SUB_PACK).resolve("render_controllers");
        Path BaseRenderControllerFolder = ConfigFolder.resolve("render_controllers");

        LoadRenderControllerFolder(SubpackRenderControllerFolder);
        LoadRenderControllerFolder(BaseRenderControllerFolder);
    }


    private static void LoadRenderControllerFolder(Path RenderControllerFolder) {
        if (!Files.isDirectory(RenderControllerFolder)) {return;}

        try (Stream<Path> Paths = Files.walk(RenderControllerFolder)) {
            Paths.filter(Files::isRegularFile)
                    .filter(JsonFile -> JsonFile.toString().endsWith(".json"))
                    .forEach(JsonFile -> {
                        JsonElement RenderControllersElement = JsonUtilities.GetElement("render_controllers", JsonFile);

                        if (RenderControllersElement != null && RenderControllersElement.isJsonArray()) {
                            for (JsonElement RenderControllerElement : RenderControllersElement.getAsJsonArray()) {
                                CacheRenderController(RenderControllerElement);
                            }

                            return;
                        }

                        RenderControllersElement = JsonUtilities.GetElement("render_controllers", JsonFile);
                        if (RenderControllersElement == null || !RenderControllersElement.isJsonObject()) {return;}

                        for (Map.Entry<String, JsonElement> Entry : RenderControllersElement.getAsJsonObject().entrySet()) {
                            String RenderControllerIdentifier = Entry.getKey();

                            if (!Entry.getValue().isJsonObject()) {continue;}

                            CacheRenderController(RenderControllerIdentifier, Entry.getValue().getAsJsonObject());
                        }
                    });
        } catch (IOException Exception) {
            JavaUnbound.LOGGER.error("Failed to load render_controllers from: {}", RenderControllerFolder, Exception);
        }
    }

    private static void CacheRenderController(String RenderControllerIdentifier, JsonObject RenderControllerObject) {
        if (RenderControllerIdentifier.isBlank()) {return;}

        if (RenderControllerCache.putIfAbsent(RenderControllerIdentifier, RenderControllerObject) == null) {
            JavaUnbound.LOGGER.debug("Cached render_controller: {}", RenderControllerIdentifier);
        }
    }

    private static void CacheRenderController(JsonElement RenderControllerElement) {
        if (!RenderControllerElement.isJsonObject()) {return;}

        JsonObject RenderControllerObject = RenderControllerElement.getAsJsonObject();
        JsonElement DescriptionElement = RenderControllerObject.get("description");

        if (DescriptionElement == null || !DescriptionElement.isJsonObject()) {return;}

        JsonElement IdentifierElement = DescriptionElement.getAsJsonObject().get("identifier");
        if (IdentifierElement == null || !IdentifierElement.isJsonPrimitive()) {return;}

        String RenderControllerIdentifier = IdentifierElement.getAsString();
        if (RenderControllerIdentifier.isBlank()) {return;}

        if (RenderControllerCache.putIfAbsent(RenderControllerIdentifier, RenderControllerObject) == null) {
            JavaUnbound.LOGGER.debug("Cached render_controller: {}", RenderControllerIdentifier);
        }
    }
}
