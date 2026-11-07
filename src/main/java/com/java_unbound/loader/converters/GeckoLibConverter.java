package com.java_unbound.loader.converters;

import com.google.gson.*;
import com.java_unbound.JavaUnbound;
import com.java_unbound.loader.definitions.entity.EntityDefinition;
import com.java_unbound.loader.definitions.entity.resolvers.EntityAnimationResolver;
import com.java_unbound.loader.definitions.entity.resolvers.EntityGeometryResolver;
import com.java_unbound.loader.definitions.entity.types.animation.EntityAnimationDefinition;
import com.java_unbound.loader.definitions.entity.types.geometry.EntityGeometryDefinition;
import com.java_unbound.loader.resourcepack.Folder;
import com.java_unbound.utils.variables.HashMaps;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GeckoLibConverter {
    private static final Gson PrettyGson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final Pattern ArrayPattern = Pattern.compile("\\[(.*?)\\]", Pattern.DOTALL);
    private static final Set<String> AxisKeys = Set.of("x", "y", "z");
    private static final String[] Axes = {"x", "y", "z"};

    private static void FixGeometry(JsonObject GeometryObject) {
        if (!GeometryObject.has("bones")) {return;}

        for (JsonElement BoneElement : GeometryObject.getAsJsonArray("bones")) {
            if (!BoneElement.isJsonObject()) {continue;}

            JsonObject Bone = BoneElement.getAsJsonObject();
            if (!Bone.has("cubes") || !Bone.get("cubes").isJsonArray()) {continue;}

            for (JsonElement CubeElement : Bone.getAsJsonArray("cubes")) {
                if (!CubeElement.isJsonObject()) {continue;}

                JsonObject Cube = CubeElement.getAsJsonObject();
                if (!Cube.has("uv") || !Cube.get("uv").isJsonObject()) {continue;}

                JsonArray Size = Cube.getAsJsonArray("size");

                int X = Size.get(0).getAsInt();
                int Y = Size.get(1).getAsInt();
                int Z = Size.get(2).getAsInt();

                JsonObject UV = Cube.getAsJsonObject("uv");

                String[] Directions = {"down", "up", "north", "south", "west", "east"};
                int[][] FaceSizes = {{X, Z}, {X, Z}, {X, Y}, {X, Y}, {Z, Y}, {Z, Y}};

                for (int i = 0; i < Directions.length; i++) {
                    if (!UV.has(Directions[i]) || !UV.get(Directions[i]).isJsonObject()) {continue;}

                    JsonObject Face = UV.getAsJsonObject(Directions[i]);

                    if (!Face.has("uv_size")) {
                        JsonArray UVSize = new JsonArray();

                        UVSize.add(FaceSizes[i][0]);
                        UVSize.add(FaceSizes[i][1]);

                        Face.add("uv_size", UVSize);
                    }
                }
            }
        }
    }

    private static void FixAnimation(JsonObject AnimationObject) {
        JsonElement BonesElement = AnimationObject.get("bones");
        if (BonesElement == null || !BonesElement.isJsonObject()) {return;}

        for (Map.Entry<String, JsonElement> Entry : BonesElement.getAsJsonObject().entrySet()) {
            if (!Entry.getValue().isJsonObject()) {continue;}

            JsonObject Bone = Entry.getValue().getAsJsonObject();

            JsonElement RelativeTo = Bone.get("relative_to");
            if (RelativeTo != null && RelativeTo.isJsonObject()) {
                JsonElement Rotation = RelativeTo.getAsJsonObject().get("rotation");

                if (Rotation != null && Rotation.isJsonPrimitive()) {
                    Bone.add("relative_to", Rotation);
                } else {
                    Bone.remove("relative_to");
                }
            }

            FixChannel(Bone, "rotation", 0);
            FixChannel(Bone, "position", 0);
            FixChannel(Bone, "scale", 1);
        }
    }

    private static boolean IsAxisObject(JsonElement Element) {
        if (Element == null || !Element.isJsonObject()) {return false;}

        JsonObject Object = Element.getAsJsonObject();
        return !Object.keySet().isEmpty() && AxisKeys.containsAll(Object.keySet());
    }

    private static void ApplyAxis(JsonElement[] Values, int Axis, JsonElement NewValue) {
        if (NewValue.isJsonPrimitive() && NewValue.getAsJsonPrimitive().isString()) {
            String Previous = "(" + Values[Axis].getAsString() + ")";
            String Expression = NewValue.getAsString().replaceAll("\\bthis\\b", Matcher.quoteReplacement(Previous));
            Values[Axis] = new JsonPrimitive(Expression);
        } else {
            Values[Axis] = NewValue;
        }
    }

    private static void ApplyAxisObject(JsonElement[] Values, JsonObject Object) {
        for (int Axis = 0; Axis < 3; Axis++) {
            JsonElement Value = Object.get(Axes[Axis]);

            if (Value != null && Value.isJsonPrimitive()) {
                ApplyAxis(Values, Axis, Value);
            }
        }
    }

    private static JsonElement NormalizeVector(JsonElement Vector, int Default) {
        if (Vector == null) {return null;}

        boolean NeedsFix = IsAxisObject(Vector);

        if (Vector.isJsonArray()) {
            for (JsonElement Entry : Vector.getAsJsonArray()) {
                if (IsAxisObject(Entry)) {
                    NeedsFix = true;
                    break;
                }
            }
        }

        if (!NeedsFix) {return Vector;}

        JsonElement[] Values = {new JsonPrimitive(Default), new JsonPrimitive(Default), new JsonPrimitive(Default)};

        if (Vector.isJsonObject()) {
            ApplyAxisObject(Values, Vector.getAsJsonObject());
        } else {
            JsonArray Array = Vector.getAsJsonArray();

            for (int Index = 0; Index < Array.size(); Index++) {
                JsonElement Entry = Array.get(Index);

                if (IsAxisObject(Entry)) {
                    ApplyAxisObject(Values, Entry.getAsJsonObject());
                } else if (Entry.isJsonPrimitive() && Index < 3) {
                    ApplyAxis(Values, Index, Entry);
                }
            }
        }

        JsonArray Result = new JsonArray();

        for (JsonElement Value : Values) {
            Result.add(Value);
        }

        return Result;
    }

    private static void FixChannel(JsonObject Bone, String ChannelName, int Default) {
        JsonElement Channel = Bone.get(ChannelName);
        if (Channel == null) {return;}

        if (Channel.isJsonArray() || IsAxisObject(Channel)) {
            Bone.add(ChannelName, NormalizeVector(Channel, Default));
            return;
        }

        if (!Channel.isJsonObject()) {return;}

        JsonObject ChannelObject = Channel.getAsJsonObject();

        for (String Time : new ArrayList<>(ChannelObject.keySet())) {
            JsonElement Value = ChannelObject.get(Time);

            if (Value.isJsonArray() || IsAxisObject(Value)) {
                ChannelObject.add(Time, NormalizeVector(Value, Default));
                continue;
            }

            if (!Value.isJsonObject()) {continue;}

            JsonObject Keyframe = Value.getAsJsonObject();

            for (String Key : new String[]{"pre", "post", "vector"}) {
                if (Keyframe.has(Key)) {
                    Keyframe.add(Key, NormalizeVector(Keyframe.get(Key), Default));
                }
            }
        }
    }

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

                FixGeometry(GeometryObject);

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

        ExportAnimations();
    }

    public static synchronized boolean ExportAnimations() {
        Set<String> ExportedAnimations = new HashSet<>();
        JsonObject AnimationsObject = new JsonObject();

        for (EntityDefinition EntityRegistry : HashMaps.Entities.values()) {
            if (EntityRegistry.Animations == null) {continue;}

            for (EntityAnimationDefinition Animation : EntityRegistry.Animations.values()) {
                if (Animation == null || !ExportedAnimations.add(Animation.Identifier)) {continue;}

                JsonObject AnimationJson = EntityAnimationResolver.GetAnimationJson(Animation.Identifier);

                if (AnimationJson == null) {
                    JavaUnbound.LOGGER.error("Could not find animation JSON: {}", Animation.Identifier);
                    continue;
                }

                AnimationJson = AnimationJson.deepCopy();
                FixAnimation(AnimationJson);

                AnimationsObject.add(Animation.Identifier, AnimationJson);
            }
        }

        if (AnimationsObject.size() == 0) {
            JavaUnbound.LOGGER.debug("No animations to export.");
            return false;
        }

        Path Config = Folder.GetConfigFolder();
        if (Config == null) {return false;}

        JsonObject RootObject = new JsonObject();
        RootObject.addProperty("format_version", "1.8.0");
        RootObject.add("animations", AnimationsObject);

        try {
            Path AnimationFolder = Config.resolve("assets").resolve("geckolib").resolve("resources").resolve("animations");
            Files.createDirectories(AnimationFolder);

            Path AnimationFile = AnimationFolder.resolve("animations.json");
            Files.writeString(AnimationFile, FormatJson(RootObject));

            Folder.SetIdentifier("geckolib" + "/animations/animations.json", "assets/" + "geckolib" + "/resources/animations/animations.json");

            JavaUnbound.LOGGER.debug("Exported animations | Count: {} | File: {}", AnimationsObject.size(), AnimationFile);
            return true;
        } catch (IOException Exception) {
            JavaUnbound.LOGGER.error("Failed to export animations file.", Exception);
            return false;
        }
    }

    public static void ClearResources() throws IOException {
        Path Config = Folder.GetConfigFolder();
        if (Config == null) {return;}

        Path ResourceFolder = Config.resolve("assets").resolve("geckolib").resolve("resources");

        ClearResourceFolder(ResourceFolder.resolve("geometries"));
        ClearResourceFolder(ResourceFolder.resolve("animation_controllers"));
        ClearResourceFolder(ResourceFolder.resolve("animations"));
        ClearResourceFolder(Config.resolve("assets").resolve("geckolib").resolve("resources").resolve("animations"));
    }

    private static void ClearResourceFolder(Path ResourceFolder) throws IOException {
        Files.createDirectories(ResourceFolder);

        try (var Paths = Files.walk(ResourceFolder)) {
            Paths.filter(Path -> !Path.equals(ResourceFolder))
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