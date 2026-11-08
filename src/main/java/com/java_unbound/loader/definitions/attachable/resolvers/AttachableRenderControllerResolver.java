package com.java_unbound.loader.definitions.attachable.resolvers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.java_unbound.JavaUnbound;
import com.java_unbound.loader.definitions.entity.resolvers.EntityRenderControllerResolver;
import com.java_unbound.loader.definitions.entity.types.render_controller.EntityRenderControllerDefinition;
import com.java_unbound.loader.definitions.entity.types.render_controller.EntityRenderControllerReference;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AttachableRenderControllerResolver {
    private static final HashMap<Path, List<EntityRenderControllerReference>> CachedAttachableRenderControllers = new HashMap<>();

    private static JsonObject ReadJson(Path JsonFile) {
        try {
            return JsonParser.parseString(Files.readString(JsonFile)).getAsJsonObject();
        } catch (Exception Exception) {
            JavaUnbound.LOGGER.error("Failed to read JSON file: {}", JsonFile, Exception);
            return null;
        }
    }

    private static JsonObject ReadObject(JsonObject Parent, String Key) {
        JsonElement Element = Parent.get(Key);
        return Element != null && Element.isJsonObject() ? Element.getAsJsonObject() : null;
    }

    private static JsonArray GetRenderControllerReferences(Path AttachableFile) {
        JsonObject Root = ReadJson(AttachableFile);
        if (Root == null) {return null;}

        JsonElement AttachableElement = Root.get("minecraft:attachable");

        if (AttachableElement == null || !AttachableElement.isJsonObject()) {
            JavaUnbound.LOGGER.error("No minecraft:attachable found: {}", AttachableFile);
            return null;
        }

        JsonObject Description = ReadObject(AttachableElement.getAsJsonObject(), "description");

        if (Description == null) {
            JavaUnbound.LOGGER.error("No attachable description found: {}", AttachableFile);
            return null;
        }

        JsonElement ReferencesElement = Description.get("render_controllers");
        JsonArray ReferencesArray = null;

        if (ReferencesElement != null && ReferencesElement.isJsonArray()) {
            ReferencesArray = ReferencesElement.getAsJsonArray();
        } else if (ReferencesElement != null && ReferencesElement.isJsonPrimitive()) {
            ReferencesArray = new JsonArray();
            ReferencesArray.add(ReferencesElement);
        }

        if (ReferencesArray == null) {
            JavaUnbound.LOGGER.warn("No render_controllers found: {}", AttachableFile);
            return null;
        }

        return ReferencesArray;
    }

    private static List<EntityRenderControllerReference> ResolveAttachableRenderControllers(Path AttachableFile) {
        Path CacheKey = AttachableFile.toAbsolutePath().normalize();

        List<EntityRenderControllerReference> Cached = CachedAttachableRenderControllers.get(CacheKey);
        if (Cached != null) {return Cached;}

        JavaUnbound.LOGGER.debug("Resolving attachable render controllers from: {}", AttachableFile);

        List<EntityRenderControllerReference> RenderControllers = new ArrayList<>();
        JsonArray ReferencesArray = GetRenderControllerReferences(AttachableFile);

        if (ReferencesArray == null) {
            CachedAttachableRenderControllers.put(CacheKey, RenderControllers);
            return RenderControllers;
        }

        List<EntityRenderControllerReference> References = EntityRenderControllerResolver.ParseReferences(ReferencesArray);
        HashMap<String, EntityRenderControllerDefinition> Available = EntityRenderControllerResolver.GetAvailableRenderControllers();

        for (EntityRenderControllerReference Reference : References) {
            EntityRenderControllerDefinition Definition = Available.get(Reference.Identifier);

            if (Definition == null) {
                JavaUnbound.LOGGER.warn("Could not resolve render controller: {} | Attachable: {}", Reference.Identifier, AttachableFile);
                continue;
            }

            Reference.Definition = Definition;
            RenderControllers.add(Reference);

            JavaUnbound.LOGGER.debug("Resolved render controller: {} | Condition: {}", Reference.Identifier, Reference.Condition);
        }

        JavaUnbound.LOGGER.debug("Finished resolving attachable render controllers: {} | Resolved: {}/{}", AttachableFile, RenderControllers.size(), References.size());
        CachedAttachableRenderControllers.put(CacheKey, RenderControllers);

        return RenderControllers;
    }

    public static synchronized List<EntityRenderControllerReference> GetAttachableRenderControllers(Path AttachableFile) {
        return new ArrayList<>(ResolveAttachableRenderControllers(AttachableFile));
    }

    public static synchronized EntityRenderControllerReference GetAttachableRenderController(Path AttachableFile, String Identifier) {
        if (Identifier == null || Identifier.isBlank()) {
            JavaUnbound.LOGGER.error("Cannot resolve render controller: identifier is null or blank.");
            return null;
        }

        for (EntityRenderControllerReference Reference : ResolveAttachableRenderControllers(AttachableFile)) {
            if (Identifier.equals(Reference.Identifier)) {return Reference;}
        }

        JavaUnbound.LOGGER.debug("Render controller not found: {} | Attachable: {}", Identifier, AttachableFile);
        return null;
    }

    public static synchronized void ClearCache() {
        CachedAttachableRenderControllers.clear();
        EntityRenderControllerResolver.ClearCache(); // shared definitions must be reloaded too, or references go stale

        JavaUnbound.LOGGER.debug("Cleared attachable render controller cache.");
    }
}