package com.java_unbound.loader.definitions.entity.resolvers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.java_unbound.JavaUnbound;
import com.java_unbound.loader.definitions.entity.types.animation.*;
import com.java_unbound.loader.resourcepack.Folder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class EntityAnimationResolver {
    private static HashMap<String, EntityAnimationDefinition> CachedAvailableAnimations;
    private static final HashMap<Path, HashMap<String, EntityAnimationDefinition>> CachedEntityAnimations = new HashMap<>();
    private static HashMap<String, JsonObject> CachedAnimationJson;

    @FunctionalInterface
    private interface AnimationVisitor {
        void Visit(String Identifier, JsonObject AnimationObject, Path AnimationFile);
    }

    public static JsonObject AsObject(JsonElement Element) {
        if (Element == null || Element.isJsonNull()) {return null;}
        if (Element.isJsonObject()) {return Element.getAsJsonObject();}

        if (Element.isJsonArray()) {
            JsonObject Merged = null;

            for (JsonElement Entry : Element.getAsJsonArray()) {
                JsonObject Object = AsObject(Entry);
                if (Object == null) {continue;}

                if (Merged == null) {Merged = new JsonObject();}
                for (Map.Entry<String, JsonElement> Property : Object.entrySet()) {
                    Merged.add(Property.getKey(), Property.getValue());
                }
            }

            return Merged;
        }

        return null;
    }

    public static List<JsonObject> AsObjectList(JsonElement Element) {
        List<JsonObject> Result = new ArrayList<>();
        if (Element == null || Element.isJsonNull()) {return Result;}

        if (Element.isJsonArray()) {
            for (JsonElement Entry : Element.getAsJsonArray()) {
                Result.addAll(AsObjectList(Entry));
            }
        } else if (Element.isJsonObject()) {
            Result.add(Element.getAsJsonObject());
        }

        return Result;
    }

    private static JsonObject ReadObject(JsonObject Parent, String Key) {
        return AsObject(Parent.get(Key));
    }

    public static List<Path> GetAnimationFolders() {
        Path ConfigFolder = Folder.GetConfigFolder();
        List<Path> Animations = new ArrayList<>();

        if (ConfigFolder.resolve("subpacks").resolve(JavaUnbound.SUB_PACK).resolve("animations").toFile().exists()) {
            Animations.add(ConfigFolder.resolve("subpacks").resolve(JavaUnbound.SUB_PACK).resolve("animations"));
        }

        if (ConfigFolder.resolve("animations").toFile().exists()) {
            Animations.add(ConfigFolder.resolve("animations"));
        }

        return Animations;
    }

    private static JsonObject ReadJson(Path JsonFile) {
        try {
            return AsObject(JsonParser.parseString(Files.readString(JsonFile)));
        } catch (Exception Exception) {
            JavaUnbound.LOGGER.error("Failed to read JSON file: {}", JsonFile, Exception);
            return null;
        }
    }

    private static void ForEachAnimation(Path AnimationFolder, AnimationVisitor Visitor) {
        if (!Files.isDirectory(AnimationFolder)) {
            JavaUnbound.LOGGER.error("Animation folder does not exist: {}", AnimationFolder);
            return;
        }

        try (Stream<Path> FilesStream = Files.walk(AnimationFolder)) {
            FilesStream
                    .filter(Files::isRegularFile)
                    .filter(File -> File.toString().endsWith(".json"))
                    .sorted(Comparator.comparing(Path::toString)) // deterministic priority between files
                    .forEach(AnimationFile -> {
                        JsonObject Root = ReadJson(AnimationFile);
                        if (Root == null) {return;}

                        JsonObject AnimationsObject = ReadObject(Root, "animations");
                        if (AnimationsObject == null) {return;}

                        for (Map.Entry<String, JsonElement> Entry : AnimationsObject.entrySet()) {
                            JsonObject AnimationObject = AsObject(Entry.getValue());
                            if (AnimationObject == null) {continue;}

                            String Identifier = Entry.getKey();
                            if (Identifier == null || Identifier.isBlank()) {continue;}

                            Visitor.Visit(Identifier, AnimationObject, AnimationFile);
                        }
                    });
        } catch (IOException Exception) {
            JavaUnbound.LOGGER.error("Failed to search animation folder: {}", AnimationFolder, Exception);
        }
    }

    private static HashMap<String, JsonObject> LoadAnimationJsonFiles() {
        HashMap<String, JsonObject> Animations = new HashMap<>();

        for (Path AnimationFolder : GetAnimationFolders()) {
            ForEachAnimation(AnimationFolder, (Identifier, AnimationObject, AnimationFile) -> Animations.putIfAbsent(Identifier, AnimationObject));
        }

        JavaUnbound.LOGGER.debug("Loaded raw animation JSON cache | Animations: {}", Animations.size());

        return Animations;
    }

    public static synchronized JsonObject GetAnimationJson(String AnimationIdentifier) {
        if (AnimationIdentifier == null || AnimationIdentifier.isBlank()) {
            return null;
        }

        if (CachedAnimationJson == null) {
            CachedAnimationJson = LoadAnimationJsonFiles();
        }

        JsonObject Animation = CachedAnimationJson.get(AnimationIdentifier);

        if (Animation == null) {
            JavaUnbound.LOGGER.debug("Could not find animation JSON: {}", AnimationIdentifier);
        }

        return Animation;
    }

    private static JsonObject GetAnimationMappings(Path EntityFile) {
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

        JsonElement AnimationElement = Description.get("animations");

        if (AnimationElement == null) {
            JavaUnbound.LOGGER.debug("No animation mappings found: {}", EntityFile);
            return null;
        }

        JsonObject Mappings = AsObject(AnimationElement);

        if (Mappings == null) {
            JavaUnbound.LOGGER.error("Invalid animation mappings: {}", EntityFile);
            return null;
        }

        return Mappings;
    }

    private static EntityAnimationDefinition ParseAnimationDefinition(String Identifier, JsonObject Json) {
        EntityAnimationDefinition Definition = new EntityAnimationDefinition();
        Definition.Identifier = Identifier;

        JsonElement LoopElement = Json.get("loop");
        if (LoopElement != null && LoopElement.isJsonPrimitive()) {
            JsonPrimitive LoopPrimitive = LoopElement.getAsJsonPrimitive();

            if (LoopPrimitive.isString() && LoopPrimitive.getAsString().equalsIgnoreCase("hold_on_last_frame")) {
                Definition.HoldOnLastFrame = true;
            } else {
                Definition.Loop = ReadBoolean(Json, "loop", false);
            }
        }

        Definition.AnimationLength = ReadNullableFloat(Json, "animation_length");
        Definition.OverridePreviousAnimation = ReadBoolean(Json, "override_previous_animation", false);
        Definition.StartDelay = ReadString(Json, "start_delay", null);
        Definition.LoopDelay = ReadString(Json, "loop_delay", null);
        Definition.AnimTimeUpdate = ReadString(Json, "anim_time_update", null);
        Definition.BlendWeight = ReadString(Json, "blend_weight", null);

        JsonObject BonesObject = ReadObject(Json, "bones");

        if (BonesObject != null) {
            for (Map.Entry<String, JsonElement> Entry : BonesObject.entrySet()) {
                JsonObject BoneObject = AsObject(Entry.getValue());
                if (BoneObject == null) {continue;}

                Definition.Bones.put(Entry.getKey(), ParseBone(BoneObject));
            }
        }

        ParseParticleEffects(Json.get("particle_effects"), Definition);
        ParseSoundEffects(Json.get("sound_effects"), Definition);
        ParseTimeline(Json.get("timeline"), Definition);

        return Definition;
    }

    private static EntityAnimationBone ParseBone(JsonObject Json) {
        EntityAnimationBone Bone = new EntityAnimationBone();

        Bone.Rotation = ParseChannel(Json.get("rotation"));
        Bone.Position = ParseChannel(Json.get("position"));
        Bone.Scale = ParseChannel(Json.get("scale"));

        JsonObject RelativeTo = ReadObject(Json, "relative_to");
        if (RelativeTo != null) {
            Bone.RelativeToRotation = ReadString(RelativeTo, "rotation", null);
        }

        return Bone;
    }

    private static EntityAnimationChannel ParseChannel(JsonElement Json) {
        if (Json == null || Json.isJsonNull()) {return null;}

        EntityAnimationChannel Channel = new EntityAnimationChannel();
        JsonObject KeyframeObject = AsObject(Json);

        if (KeyframeObject != null) {
            List<EntityAnimationKeyframe> Keyframes = new ArrayList<>();

            for (Map.Entry<String, JsonElement> Entry : KeyframeObject.entrySet()) {
                EntityAnimationKeyframe Keyframe = ParseKeyframe(Entry.getKey(), Entry.getValue());
                if (Keyframe != null) {Keyframes.add(Keyframe);}
            }

            if (Keyframes.isEmpty()) {return null;}

            Keyframes.sort(Comparator.comparingDouble(Keyframe -> Keyframe.Time));
            Channel.Keyframes = Keyframes;
            return Channel;
        }

        Channel.Value = ReadVector(Json);

        return Channel.Value != null ? Channel : null;
    }

    private static EntityAnimationKeyframe ParseKeyframe(String TimeKey, JsonElement Json) {
        float Time;
        try {
            Time = Float.parseFloat(TimeKey.trim());
        } catch (NumberFormatException Exception) {
            JavaUnbound.LOGGER.error("Invalid keyframe time: {}", TimeKey);
            return null;
        }

        EntityAnimationKeyframe Keyframe = new EntityAnimationKeyframe();
        Keyframe.Time = Time;

        JsonObject Object = AsObject(Json);

        if (Object != null) {
            Keyframe.Pre = ReadVector(Object.get("pre"));
            Keyframe.Post = ReadVector(Object.get("post"));
            Keyframe.LerpMode = ReadString(Object, "lerp_mode", Keyframe.LerpMode);

            if (Keyframe.Pre == null) {Keyframe.Pre = Keyframe.Post;}
            if (Keyframe.Post == null) {Keyframe.Post = Keyframe.Pre;}
        } else {
            Keyframe.Pre = ReadVector(Json);
            Keyframe.Post = Keyframe.Pre;
        }

        if (Keyframe.Pre == null || Keyframe.Post == null) {
            JavaUnbound.LOGGER.error("Invalid keyframe value at time: {}", TimeKey);
            return null;
        }

        return Keyframe;
    }

    private static void ParseParticleEffects(JsonElement Json, EntityAnimationDefinition Definition) {
        JsonObject EventMap = AsObject(Json);
        if (EventMap == null) {return;}

        for (Map.Entry<String, JsonElement> Entry : EventMap.entrySet()) {
            Float Time = ParseTime(Entry.getKey());
            if (Time == null) {continue;}

            List<EntityAnimationParticleEffect> Effects = new ArrayList<>();

            for (JsonObject EffectObject : AsObjectList(Entry.getValue())) {
                EntityAnimationParticleEffect Effect = new EntityAnimationParticleEffect();
                Effect.Effect = ReadString(EffectObject, "effect", null);
                Effect.Locator = ReadString(EffectObject, "locator", null);
                Effect.PreEffectScript = ReadString(EffectObject, "pre_effect_script", null);

                if (Effect.Effect != null) {Effects.add(Effect);}
            }

            if (!Effects.isEmpty()) {
                Definition.ParticleEffects.computeIfAbsent(Time, Key -> new ArrayList<>()).addAll(Effects);
            }
        }
    }

    private static void ParseSoundEffects(JsonElement Json, EntityAnimationDefinition Definition) {
        if (Json == null || !Json.isJsonObject()) {return;}

        for (Map.Entry<String, JsonElement> Entry : Json.getAsJsonObject().entrySet()) {
            Float Time = ParseTime(Entry.getKey());
            if (Time == null) {continue;}

            List<EntityAnimationSoundEffect> Effects = new ArrayList<>();

            for (JsonObject EffectObject : AsObjectList(Entry.getValue())) {
                EntityAnimationSoundEffect Effect = new EntityAnimationSoundEffect();
                Effect.Effect = ReadString(EffectObject, "effect", null);

                if (Effect.Effect != null) {Effects.add(Effect);}
            }

            if (!Effects.isEmpty()) {
                Definition.SoundEffects.computeIfAbsent(Time, Key -> new ArrayList<>()).addAll(Effects);
            }
        }
    }

    private static void ParseTimeline(JsonElement Json, EntityAnimationDefinition Definition) {
        if (Json == null || !Json.isJsonObject()) {return;}

        for (Map.Entry<String, JsonElement> Entry : Json.getAsJsonObject().entrySet()) {
            Float Time = ParseTime(Entry.getKey());
            if (Time == null) {continue;}

            List<String> Scripts = new ArrayList<>();
            JsonElement Value = Entry.getValue();

            if (Value.isJsonPrimitive()) {
                Scripts.add(Value.getAsString());
            } else if (Value.isJsonArray()) {
                for (JsonElement ScriptElement : Value.getAsJsonArray()) {
                    if (ScriptElement.isJsonPrimitive()) {Scripts.add(ScriptElement.getAsString());}
                }
            }

            if (!Scripts.isEmpty()) {
                Definition.Timeline.computeIfAbsent(Time, Key -> new ArrayList<>()).addAll(Scripts);
            }
        }
    }

    private static Float ParseTime(String TimeKey) {
        try {
            return Float.parseFloat(TimeKey.trim());
        } catch (NumberFormatException Exception) {
            JavaUnbound.LOGGER.error("Invalid event time: {}", TimeKey);
            return null;
        }
    }

    private static String ReadString(JsonObject Parent, String Key, String Default) {
        JsonElement Element = Parent.get(Key);
        return Element != null && Element.isJsonPrimitive() ? Element.getAsString() : Default;
    }

    private static boolean ReadBoolean(JsonObject Parent, String Key, boolean Default) {
        JsonElement Element = Parent.get(Key);
        if (Element == null || !Element.isJsonPrimitive()) {return Default;}

        JsonPrimitive Primitive = Element.getAsJsonPrimitive();
        if (Primitive.isBoolean()) {return Primitive.getAsBoolean();}
        if (Primitive.isString()) {
            String Text = Primitive.getAsString();

            if (Text.equalsIgnoreCase("true")) {return true;}
            if (Text.equalsIgnoreCase("false")) {return false;}
        }

        return Default;
    }

    private static Float ReadNullableFloat(JsonObject Parent, String Key) {
        JsonElement Element = Parent.get(Key);
        if (Element == null || !Element.isJsonPrimitive()) {return null;}

        try {
            return Element.getAsJsonPrimitive().isNumber() ? Element.getAsNumber().floatValue() : Float.parseFloat(Element.getAsString().trim());
        } catch (NumberFormatException Exception) {
            return null;
        }
    }

    private static String[] ReadVector(JsonElement Element) {
        if (Element == null || Element.isJsonNull()) {return null;}

        if (Element.isJsonPrimitive()) {
            String Value = Element.getAsString();
            return new String[]{Value, Value, Value};
        }

        if (!Element.isJsonArray()) {return null;}

        JsonArray Array = Element.getAsJsonArray();
        if (Array.size() != 3) {return null;}

        String[] Values = new String[3];

        for (int Index = 0; Index < 3; Index++) {
            JsonElement Component = Array.get(Index);
            if (!Component.isJsonPrimitive()) {return null;}
            Values[Index] = Component.getAsString();
        }

        return Values;
    }

    private static EntityAnimationDefinition ParseAnimation(String Identifier, JsonObject AnimationObject, Path AnimationFile) {
        if (AnimationObject == null) {return null;}

        try {
            return ParseAnimationDefinition(Identifier, AnimationObject);
        } catch (Exception Exception) {
            JavaUnbound.LOGGER.error("Failed to deserialize animation: {} | File: {}", Identifier, AnimationFile, Exception);
            return null;
        }
    }

    private static HashMap<String, EntityAnimationDefinition> LoadAnimationFiles() {
        HashMap<String, EntityAnimationDefinition> Animations = new HashMap<>();

        List<Path> AnimationFolders = GetAnimationFolders();

        if (AnimationFolders.isEmpty()) {
            JavaUnbound.LOGGER.error("No animation folders exist.");
            return Animations;
        }

        for (Path AnimationFolder : AnimationFolders) {
            JavaUnbound.LOGGER.debug("Searching animation files in: {}", AnimationFolder);

            ForEachAnimation(AnimationFolder, (Identifier, AnimationObject, AnimationFile) -> {
                if (Animations.containsKey(Identifier)) {
                    JavaUnbound.LOGGER.debug("Skipping animation (already loaded from higher priority folder): {} | File: {}", Identifier, AnimationFile);
                    return;
                }

                EntityAnimationDefinition Definition = ParseAnimation(Identifier, AnimationObject, AnimationFile);
                if (Definition == null) {return;}

                Animations.put(Identifier, Definition);

                JavaUnbound.LOGGER.debug("Found animation: {} | File: {} | Bones: {}", Identifier, AnimationFile, Definition.Bones.size());
            });
        }

        JavaUnbound.LOGGER.debug("Finished animation search | Found: {}", Animations.size());
        return Animations;
    }

    private static synchronized HashMap<String, EntityAnimationDefinition> GetAvailableAnimations() {
        if (CachedAvailableAnimations == null) {
            JavaUnbound.LOGGER.debug("Loading animation cache...");

            CachedAvailableAnimations = LoadAnimationFiles();

            JavaUnbound.LOGGER.debug("Animation cache loaded | Animations: {}", CachedAvailableAnimations.size());
        }

        return CachedAvailableAnimations;
    }

    private static HashMap<String, EntityAnimationDefinition> ResolveEntityAnimations(Path EntityFile) {
        Path CacheKey = EntityFile.toAbsolutePath().normalize();

        HashMap<String, EntityAnimationDefinition> CachedAnimations = CachedEntityAnimations.get(CacheKey);

        if (CachedAnimations != null) {
            return CachedAnimations;
        }

        JavaUnbound.LOGGER.debug("Resolving entity animations from: {}", EntityFile);

        JsonObject AnimationMappings = GetAnimationMappings(EntityFile);

        HashMap<String, EntityAnimationDefinition> Animations = new HashMap<>();

        if (AnimationMappings == null) {
            CachedEntityAnimations.put(CacheKey, Animations);
            return Animations;
        }

        HashMap<String, EntityAnimationDefinition> AvailableAnimations = GetAvailableAnimations();

        for (Map.Entry<String, JsonElement> Entry : AnimationMappings.entrySet()) {
            String AnimationAlias = Entry.getKey();
            JsonElement IdentifierElement = Entry.getValue();

            if (!IdentifierElement.isJsonPrimitive()) {continue;}

            String AnimationIdentifier = IdentifierElement.getAsString();

            if (AnimationIdentifier.startsWith("controller.")) {continue;}

            EntityAnimationDefinition Definition = AvailableAnimations.get(AnimationIdentifier);

            if (Definition == null) {
                JavaUnbound.LOGGER.debug("Could not resolve animation: {} | Alias: {} | Entity: {}", AnimationIdentifier, AnimationAlias, EntityFile);
                continue;
            }

            Animations.put(AnimationAlias, Definition);
            JavaUnbound.LOGGER.debug("Resolved animation: {} -> {} | Bones: {}", AnimationAlias, AnimationIdentifier, Definition.Bones.size());
        }

        JavaUnbound.LOGGER.debug("Finished resolving entity animations: {} | Resolved: {}/{}", EntityFile, Animations.size(), AnimationMappings.size());
        CachedEntityAnimations.put(CacheKey, Animations);

        return Animations;
    }

    public static synchronized HashMap<String, EntityAnimationDefinition> GetEntityAnimations(Path EntityFile) {
        return new HashMap<>(ResolveEntityAnimations(EntityFile));
    }

    public static synchronized EntityAnimationDefinition GetEntityAnimation(Path EntityFile, String AnimationAlias) {
        if (AnimationAlias == null || AnimationAlias.isBlank()) {
            JavaUnbound.LOGGER.error("Cannot resolve animation: alias is null or blank.");
            return null;
        }

        EntityAnimationDefinition Definition = ResolveEntityAnimations(EntityFile).get(AnimationAlias);

        if (Definition == null) {
            JavaUnbound.LOGGER.debug("Animation alias not found: {} | Entity: {}", AnimationAlias, EntityFile);
        }

        return Definition;
    }

    public static synchronized EntityAnimationDefinition GetAnimation(String AnimationIdentifier) {
        if (AnimationIdentifier == null || AnimationIdentifier.isBlank()) {return null;}
        return GetAvailableAnimations().get(AnimationIdentifier);
    }

    public static synchronized void ClearCache() {
        CachedAvailableAnimations = null;
        CachedAnimationJson = null;
        CachedEntityAnimations.clear();

        JavaUnbound.LOGGER.debug("Cleared entity animation cache.");
    }
}