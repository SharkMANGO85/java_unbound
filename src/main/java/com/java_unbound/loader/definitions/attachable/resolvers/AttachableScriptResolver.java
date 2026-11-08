package com.java_unbound.loader.definitions.attachable.resolvers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.java_unbound.JavaUnbound;
import com.java_unbound.loader.definitions.attachable.types.scripts.AttachableScriptsAnimate;
import com.java_unbound.loader.definitions.attachable.types.scripts.AttachableScriptsDefinition;
import com.java_unbound.loader.definitions.attachable.types.scripts.AttachableScriptsVariables;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AttachableScriptResolver {
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

    private static JsonObject GetScriptsObject(Path AttachableFile) {
        JsonObject Root = ReadJson(AttachableFile);
        if (Root == null) {return null;}

        JsonObject ClientAttachable = ReadObject(Root, "minecraft:attachable");

        if (ClientAttachable == null) {
            JavaUnbound.LOGGER.error("No minecraft:attachable found: {}", AttachableFile);
            return null;
        }

        JsonObject Description = ReadObject(ClientAttachable, "description");

        if (Description == null) {
            JavaUnbound.LOGGER.error("No attachable description found: {}", AttachableFile);
            return null;
        }

        JsonObject Scripts = ReadObject(Description, "scripts");

        if (Scripts == null) {
            JavaUnbound.LOGGER.debug("No scripts found: {}", AttachableFile);
        }

        return Scripts;
    }

    private static AttachableScriptsDefinition ParseScripts(JsonObject Json) {
        AttachableScriptsDefinition Definition = new AttachableScriptsDefinition();

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

    private static List<AttachableScriptsAnimate> ParseAnimate(JsonElement Json) {
        List<AttachableScriptsAnimate> Result = new ArrayList<>();
        if (Json == null || !Json.isJsonArray()) {return Result;}

        for (JsonElement Element : Json.getAsJsonArray()) {
            if (Element.isJsonPrimitive()) {
                String Key = Element.getAsString();
                if (Key.isBlank()) {continue;}

                AttachableScriptsAnimate Animate = new AttachableScriptsAnimate();
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

                    AttachableScriptsAnimate Animate = new AttachableScriptsAnimate();
                    Animate.Key = Entry.getKey();
                    Animate.Value = Value;
                    Result.add(Animate);
                }
            }
        }

        return Result;
    }

    private static List<AttachableScriptsVariables> ParseVariables(JsonElement Json) {
        List<AttachableScriptsVariables> Result = new ArrayList<>();
        if (Json == null || !Json.isJsonObject()) {return Result;}

        for (Map.Entry<String, JsonElement> Entry : Json.getAsJsonObject().entrySet()) {
            String Value = ReadMolang(Entry.getValue());
            if (Value == null) {continue;}

            AttachableScriptsVariables Variable = new AttachableScriptsVariables();
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

    public static AttachableScriptsDefinition GetAttableScripts(Path JsonFile) {
        if (JsonFile == null) {
            JavaUnbound.LOGGER.error("Cannot load attachable scripts: JSON file path is null.");
            return new AttachableScriptsDefinition();
        }

        if (!Files.isRegularFile(JsonFile)) {
            JavaUnbound.LOGGER.error("Attachable JSON file does not exist: {}", JsonFile);
            return new AttachableScriptsDefinition();
        }

        JavaUnbound.LOGGER.debug("Loading attachable scripts from: {}", JsonFile);

        JsonObject ScriptsObject = GetScriptsObject(JsonFile);

        if (ScriptsObject == null) {
            return new AttachableScriptsDefinition();
        }

        try {
            AttachableScriptsDefinition Definition = ParseScripts(ScriptsObject);

            JavaUnbound.LOGGER.debug("Loaded attachable scripts: {} | Initialize: {} | PreAnimation: {} | Animate: {} | Variables: {}", JsonFile, Definition.Initialize.size(), Definition.PreAnimation.size(), Definition.Animate.size(), Definition.Variables.size());

            return Definition;
        } catch (Exception Exception) {
            JavaUnbound.LOGGER.error("Failed to parse attachable scripts from: {}", JsonFile, Exception);
            return new AttachableScriptsDefinition();
        }
    }
}