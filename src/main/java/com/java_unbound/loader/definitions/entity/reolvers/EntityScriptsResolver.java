package com.java_unbound.loader.definitions.entity.reolvers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.java_unbound.JavaUnbound;
import com.java_unbound.loader.definitions.entity.types.scripts.EntityScriptsAnimate;
import com.java_unbound.loader.definitions.entity.types.scripts.EntityScriptsDefinition;
import com.java_unbound.loader.definitions.entity.types.scripts.EntityScriptsVariables;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EntityScriptsResolver {
    private static JsonObject ReadJson(Path JsonFile) {
        try {
            JsonElement Element = JsonParser.parseString(Files.readString(JsonFile));

            if (!Element.isJsonObject()) {
                JavaUnbound.LOGGER.debug("Skipping JSON file (root is not an object): {}", JsonFile);
                return null;
            }

            return Element.getAsJsonObject();
        } catch (Exception Exception) {
            JavaUnbound.LOGGER.error("Failed to read JSON file: {}", JsonFile, Exception);
            return null;
        }
    }

    private static JsonObject GetScriptsObject(Path EntityFile) {
        JsonObject Root = ReadJson(EntityFile);
        if (Root == null) {return null;}

        JsonObject ClientEntity = ReadObject(Root, "minecraft:client_entity");

        if (ClientEntity == null) {
            JavaUnbound.LOGGER.error("No minecraft:client_entity found: {}", EntityFile);
            return null;
        }

        JsonObject Description = ReadObject(ClientEntity, "description");

        if (Description == null) {
            JavaUnbound.LOGGER.error("No entity description found: {}", EntityFile);
            return null;
        }

        JsonObject Scripts = ReadObject(Description, "scripts");

        if (Scripts == null) {
            JavaUnbound.LOGGER.debug("No scripts found: {}", EntityFile);
        }

        return Scripts;
    }

    private static EntityScriptsDefinition ParseScripts(JsonObject Json) {
        EntityScriptsDefinition Definition = new EntityScriptsDefinition();

        Definition.Initialize = ReadStringList(Json.get("initialize"));
        Definition.PreAnimation = ReadStringList(Json.get("pre_animation"));
        Definition.Animate = ParseAnimate(Json.get("animate"));
        Definition.Variables = ParseVariables(Json.get("variables"));

        Definition.Scale = ReadMolang(Json.get("scale"));
        Definition.ScaleX = ReadMolang(Json.get("scalex"));
        Definition.ScaleY = ReadMolang(Json.get("scaley"));
        Definition.ScaleZ = ReadMolang(Json.get("scalez"));

        Definition.ParentSetup = ReadMolang(Json.get("parent_setup"));
        Definition.ShouldUpdateEffectsOffscreen = ReadMolang(Json.get("should_update_effects_offscreen"));
        Definition.ShouldUpdateBonesAndEffectsOffscreen = ReadMolang(Json.get("should_update_bones_and_effects_offscreen"));

        return Definition;
    }

    private static List<EntityScriptsAnimate> ParseAnimate(JsonElement Json) {
        List<EntityScriptsAnimate> Result = new ArrayList<>();
        if (Json == null || !Json.isJsonArray()) {return Result;}

        for (JsonElement Element : Json.getAsJsonArray()) {
            if (Element.isJsonPrimitive()) {
                String Key = Element.getAsString();
                if (Key.isBlank()) {continue;}

                EntityScriptsAnimate Animate = new EntityScriptsAnimate();
                Animate.Key = Key;
                Animate.Value = null;
                Result.add(Animate);
                continue;
            }

            if (Element.isJsonObject()) {
                for (Map.Entry<String, JsonElement> Entry : Element.getAsJsonObject().entrySet()) {
                    if (Entry.getKey().isBlank()) {continue;}

                    String Value = ReadMolang(Entry.getValue());
                    if (Value == null) {continue;}

                    EntityScriptsAnimate Animate = new EntityScriptsAnimate();
                    Animate.Key = Entry.getKey();
                    Animate.Value = Value;
                    Result.add(Animate);
                }
            }
        }

        return Result;
    }

    private static List<EntityScriptsVariables> ParseVariables(JsonElement Json) {
        List<EntityScriptsVariables> Result = new ArrayList<>();
        if (Json == null || !Json.isJsonObject()) {return Result;}

        for (Map.Entry<String, JsonElement> Entry : Json.getAsJsonObject().entrySet()) {
            String Value = ReadMolang(Entry.getValue());
            if (Value == null) {continue;}

            EntityScriptsVariables Variable = new EntityScriptsVariables();
            Variable.Key = Entry.getKey();
            Variable.Value = Value;
            Result.add(Variable);
        }

        return Result;
    }

    private static JsonObject ReadObject(JsonObject Parent, String Key) {
        JsonElement Element = Parent.get(Key);
        return Element != null && Element.isJsonObject() ? Element.getAsJsonObject() : null;
    }

    private static String ReadMolang(JsonElement Element) {
        if (Element == null || !Element.isJsonPrimitive()) {return null;}
        return Element.getAsString();
    }

    private static List<String> ReadStringList(JsonElement Element) {
        List<String> Result = new ArrayList<>();
        if (Element == null) {return Result;}

        if (Element.isJsonPrimitive()) {
            Result.add(Element.getAsString());
            return Result;
        }

        if (!Element.isJsonArray()) {return Result;}

        JsonArray Array = Element.getAsJsonArray();
        for (JsonElement Entry : Array) {
            String Value = ReadMolang(Entry);
            if (Value != null) {Result.add(Value);}
        }

        return Result;
    }

    public static EntityScriptsDefinition GetEntityScripts(Path JsonFile) {
        if (JsonFile == null) {
            JavaUnbound.LOGGER.error("Cannot load entity scripts: JSON file path is null.");
            return new EntityScriptsDefinition();
        }

        if (!Files.isRegularFile(JsonFile)) {
            JavaUnbound.LOGGER.error("Entity JSON file does not exist: {}", JsonFile);
            return new EntityScriptsDefinition();
        }

        JavaUnbound.LOGGER.debug("Loading entity scripts from: {}", JsonFile);

        JsonObject ScriptsObject = GetScriptsObject(JsonFile);

        if (ScriptsObject == null) {
            return new EntityScriptsDefinition();
        }

        try {
            EntityScriptsDefinition Definition = ParseScripts(ScriptsObject);

            JavaUnbound.LOGGER.debug("Loaded entity scripts: {} | Initialize: {} | PreAnimation: {} | Animate: {} | Variables: {}", JsonFile, Definition.Initialize.size(), Definition.PreAnimation.size(), Definition.Animate.size(), Definition.Variables.size());

            return Definition;
        } catch (Exception Exception) {
            JavaUnbound.LOGGER.error("Failed to parse entity scripts from: {}", JsonFile, Exception);
            return new EntityScriptsDefinition();
        }
    }
}