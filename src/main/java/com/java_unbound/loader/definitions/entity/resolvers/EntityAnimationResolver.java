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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class EntityAnimationResolver {
    private static HashMap<String, EntityAnimationDefinition> CachedAvailableAnimations;
    private static final HashMap<Path, List<EntityAnimationReference>> CachedEntityAnimations = new HashMap<>();
    private static HashMap<String, JsonObject> CachedAnimationJson;

    public static List<Path> GetAnimationFolders() {
        Path ConfigFolder = Folder.GetConfigFolder();
        List<Path> AnimationFolders = new ArrayList<>();

        if (ConfigFolder.resolve("subpacks").resolve(JavaUnbound.SUB_PACK).resolve("animations").toFile().exists()) {
            AnimationFolders.add(ConfigFolder.resolve("subpacks").resolve(JavaUnbound.SUB_PACK).resolve("animations"));
        }

        if (ConfigFolder.resolve("animations").toFile().exists()) {
            AnimationFolders.add(ConfigFolder.resolve("animations"));
        }

        return AnimationFolders;
    }

    private static JsonObject ReadJson(Path JsonFile) {
        try {
            return JsonParser.parseString(Files.readString(JsonFile)).getAsJsonObject();
        } catch (Exception Exception) {
            JavaUnbound.LOGGER.error("Failed to read JSON file: {}", JsonFile, Exception);
            return null;
        }
    }

    private static synchronized HashMap<String, JsonObject> GetAvailableAnimationJson() {
        if (CachedAnimationJson == null) {
            CachedAnimationJson = new HashMap<>();

            for (Path AnimationFolder : GetAnimationFolders()) {
                if (!Files.isDirectory(AnimationFolder)) {continue;}

                try (Stream<Path> FilesStream = Files.walk(AnimationFolder)) {
                    FilesStream.filter(Files::isRegularFile)
                            .filter(File -> File.toString().endsWith(".json"))
                            .forEach(AnimationFile -> {
                                JsonObject Root = ReadJson(AnimationFile);
                                if (Root == null) {return;}

                                JsonObject AnimationsObject = ReadObject(Root, "animations");
                                if (AnimationsObject == null) {return;}

                                for (Map.Entry<String, JsonElement> Entry : AnimationsObject.entrySet()) {
                                    String Identifier = Entry.getKey();
                                    if (Identifier == null || Identifier.isBlank() || !Entry.getValue().isJsonObject()) {continue;}
                                    if (CachedAnimationJson.containsKey(Identifier)) {continue;}

                                    CachedAnimationJson.put(Identifier, Entry.getValue().getAsJsonObject());
                                }
                            });
                } catch (IOException Exception) {
                    JavaUnbound.LOGGER.error("Failed to search animation JSON files: {}", AnimationFolder, Exception);
                }
            }
        }

        return CachedAnimationJson;
    }

    public static synchronized JsonObject GetAnimationJson(String Identifier) {
        if (Identifier == null || Identifier.isBlank()) {return null;}

        JsonObject Json = GetAvailableAnimationJson().get(Identifier);

        return Json != null ? Json.deepCopy() : null;
    }


    private static JsonObject GetEntityDescription(Path EntityFile) {
        JsonObject Root = ReadJson(EntityFile);
        if (Root == null) {return null;}

        JsonElement ClientEntityElement = Root.get("minecraft:client_entity");

        if (ClientEntityElement == null || !ClientEntityElement.isJsonObject()) {
            JavaUnbound.LOGGER.error("No minecraft:client_entity found: {}", EntityFile);
            return null;
        }

        JsonObject Description = ReadObject(ClientEntityElement.getAsJsonObject(), "description");

        if (Description == null) {
            JavaUnbound.LOGGER.error("No entity description found: {}", EntityFile);
            return null;
        }

        return Description;
    }

    private static JsonArray GetAnimateArray(JsonObject Description, Path EntityFile) {
        JsonObject Scripts = ReadObject(Description, "scripts");

        if (Scripts == null) {
            JavaUnbound.LOGGER.debug("No scripts found: {}", EntityFile);
            return null;
        }

        JsonElement AnimateElement = Scripts.get("animate");
        JsonArray AnimateArray = null;

        if (AnimateElement != null && AnimateElement.isJsonArray()) {
            AnimateArray = AnimateElement.getAsJsonArray();
        } else if (AnimateElement != null && AnimateElement.isJsonPrimitive()) {
            AnimateArray = new JsonArray();
            AnimateArray.add(AnimateElement);
        }

        if (AnimateArray == null) {
            JavaUnbound.LOGGER.debug("No scripts.animate found: {}", EntityFile);
            return null;
        }

        return AnimateArray;
    }

    private static LinkedHashMap<String, String> ParseAnimationMap(JsonObject Description) {
        LinkedHashMap<String, String> Result = new LinkedHashMap<>();

        JsonObject AnimationsObject = ReadObject(Description, "animations");
        if (AnimationsObject == null) {return Result;}

        for (Map.Entry<String, JsonElement> Entry : AnimationsObject.entrySet()) {
            String Value = ReadString(Entry.getValue());
            if (Value == null || Value.isBlank()) {continue;}

            Result.put(Entry.getKey(), Value);
        }

        return Result;
    }

    private static List<EntityAnimationReference> ParseReferences(JsonArray AnimateArray) {
        List<EntityAnimationReference> References = new ArrayList<>();

        for (JsonElement Element : AnimateArray) {
            if (Element.isJsonPrimitive()) {
                String ShortName = Element.getAsString();
                if (ShortName.isBlank()) {continue;}

                EntityAnimationReference Reference = new EntityAnimationReference();
                Reference.ShortName = ShortName;

                References.add(Reference);

                continue;
            }

            if (Element.isJsonObject()) {
                for (Map.Entry<String, JsonElement> Entry : Element.getAsJsonObject().entrySet()) {
                    if (Entry.getKey().isBlank()) {continue;}

                    String Condition = ReadString(Entry.getValue());
                    if (Condition == null) {continue;}

                    EntityAnimationReference Reference = new EntityAnimationReference();
                    Reference.ShortName = Entry.getKey();
                    Reference.Condition = Condition;
                    References.add(Reference);
                }
            }
        }

        return References;
    }

    private static EntityAnimationDefinition ParseAnimation(String Identifier, JsonObject Json) {
        EntityAnimationDefinition Definition = new EntityAnimationDefinition();

        Definition.Identifier = Identifier;

        ParseLoop(Json.get("loop"), Definition);

        JsonElement AnimationLength = Json.get("animation_length");
        if (AnimationLength != null && AnimationLength.isJsonPrimitive()) {Definition.AnimationLength = AnimationLength.getAsFloat();}

        String AnimTimeUpdate = ReadValue(Json.get("anim_time_update"));
        if (AnimTimeUpdate != null) {Definition.AnimTimeUpdate = AnimTimeUpdate;}

        String BlendWeight = ReadValue(Json.get("blend_weight"));
        if (BlendWeight != null) {Definition.BlendWeight = BlendWeight;}

        String LoopDelay = ReadValue(Json.get("loop_delay"));
        if (LoopDelay != null) {Definition.LoopDelay = LoopDelay;}

        String StartDelay = ReadValue(Json.get("start_delay"));
        if (StartDelay != null) {Definition.StartDelay = StartDelay;}

        Definition.OverridePreviousAnimation = ReadBoolean(Json, "override_previous_animation", Definition.OverridePreviousAnimation);

        JsonObject BonesObject = ReadObject(Json, "bones");

        if (BonesObject != null) {
            for (Map.Entry<String, JsonElement> Entry : BonesObject.entrySet()) {
                if (Entry.getKey().isBlank()) {continue;}
                if (!Entry.getValue().isJsonObject()) {continue;}

                Definition.Bones.put(Entry.getKey(), ParseBone(Entry.getValue().getAsJsonObject()));
            }
        }

        ParseTimeline(ReadObject(Json, "timeline"), Definition, Identifier);
        ParseEffects(ReadObject(Json, "particle_effects"), Definition.ParticleEffects, Identifier);
        ParseEffects(ReadObject(Json, "sound_effects"), Definition.SoundEffects, Identifier);

        return Definition;
    }

    private static void ParseLoop(JsonElement Element, EntityAnimationDefinition Definition) {
        if (Element == null || !Element.isJsonPrimitive()) {return;}

        JsonPrimitive Primitive = Element.getAsJsonPrimitive();

        if (Primitive.isBoolean()) {
            Definition.Loop = Primitive.getAsBoolean() ? EntityAnimationLoopMode.LOOP : EntityAnimationLoopMode.ONCE;
        } else if ("hold_on_last_frame".equals(Primitive.getAsString())) {
            Definition.Loop = EntityAnimationLoopMode.HOLD_ON_LAST_FRAME;
        }
    }

    private static EntityAnimationBone ParseBone(JsonObject Json) {
        EntityAnimationBone Bone = new EntityAnimationBone();

        Bone.Rotation = ParseChannel(Json.get("rotation"));
        Bone.Position = ParseChannel(Json.get("position"));
        Bone.Scale = ParseChannel(Json.get("scale"));

        JsonObject RelativeTo = ReadObject(Json, "relative_to");
        if (RelativeTo != null) {
            String RotationRelativeTo = ReadString(RelativeTo.get("rotation"));

            if (RotationRelativeTo != null) {
                Bone.RotationRelativeTo = RotationRelativeTo;
            }
        }

        return Bone;
    }

    private static EntityAnimationChannel ParseChannel(JsonElement Element) {
        if (Element == null) {return null;}

        EntityAnimationChannel Channel = new EntityAnimationChannel();

        if (Element.isJsonObject()) {
            Channel.Keyframes = new LinkedHashMap<>();

            for (Map.Entry<String, JsonElement> Entry : Element.getAsJsonObject().entrySet()) {
                try {
                    Channel.Keyframes.put(Float.parseFloat(Entry.getKey()), ParseKeyframe(Entry.getValue()));
                } catch (NumberFormatException Exception) {
                    JavaUnbound.LOGGER.warn("Invalid animation keyframe time: {}", Entry.getKey());
                }
            }

            return Channel;
        }

        Channel.Static = ReadVector(Element);
        return Channel;
    }

    private static EntityAnimationKeyframe ParseKeyframe(JsonElement Element) {
        EntityAnimationKeyframe Keyframe = new EntityAnimationKeyframe();

        if (Element.isJsonObject()) {
            JsonObject Object = Element.getAsJsonObject();

            JsonElement Pre = Object.get("pre");
            JsonElement Post = Object.get("post");

            Keyframe.Post = ReadVector(Post != null ? Post : Pre);
            Keyframe.Pre = Pre != null ? ReadVector(Pre) : Keyframe.Post;

            String LerpMode = ReadString(Object.get("lerp_mode"));

            if (LerpMode != null) {
                Keyframe.LerpMode = LerpMode;
            }

            return Keyframe;
        }

        Keyframe.Pre = Keyframe.Post = ReadVector(Element);

        return Keyframe;
    }

    private static void ParseTimeline(JsonObject Timeline, EntityAnimationDefinition Definition, String Identifier) {
        if (Timeline == null) {return;}

        for (Map.Entry<String, JsonElement> Entry : Timeline.entrySet()) {
            try {
                Definition.Timeline.put(Float.parseFloat(Entry.getKey()), ReadStringList(Entry.getValue()));
            } catch (NumberFormatException Exception) {
                JavaUnbound.LOGGER.warn("Invalid timeline keyframe: {} | Animation: {}", Entry.getKey(), Identifier);
            }
        }
    }

    private static void ParseEffects(JsonObject Effects, Map<Float, List<EntityAnimationEffect>> Output, String Identifier) {
        if (Effects == null) {return;}

        for (Map.Entry<String, JsonElement> Entry : Effects.entrySet()) {
            List<EntityAnimationEffect> List = new ArrayList<>();

            if (Entry.getValue().isJsonArray()) {
                for (JsonElement Element : Entry.getValue().getAsJsonArray()) {
                    if (!Element.isJsonObject()) {continue;}
                    List.add(ParseEffect(Element.getAsJsonObject()));
                }
            } else if (Entry.getValue().isJsonObject()) {
                List.add(ParseEffect(Entry.getValue().getAsJsonObject()));
            }

            try {
                Output.put(Float.parseFloat(Entry.getKey()), List);
            } catch (NumberFormatException Exception) {
                JavaUnbound.LOGGER.warn("Invalid effect keyframe: {} | Animation: {}", Entry.getKey(), Identifier);
            }
        }
    }

    private static EntityAnimationEffect ParseEffect(JsonObject Object) {
        EntityAnimationEffect Effect = new EntityAnimationEffect();

        Effect.Effect = ReadString(Object.get("effect"));
        Effect.Locator = ReadString(Object.get("locator"));
        Effect.PreEffectScript = ReadString(Object.get("pre_effect_script"));

        return Effect;
    }

    private static String[] ReadVector(JsonElement Element) {
        String[] Result = new String[]{"0", "0", "0"};
        if (Element == null) {return Result;}

        if (Element.isJsonArray()) {
            JsonArray Array = Element.getAsJsonArray();

            for (int Index = 0; Index < 3; Index++) {
                String Value = Index < Array.size() ? ReadValue(Array.get(Index)) : null;
                Result[Index] = Value != null ? Value : "0";
            }

            return Result;
        }

        String Value = ReadValue(Element);
        if (Value == null) {return Result;}

        return new String[]{Value, Value, Value};
    }

    private static JsonObject ReadObject(JsonObject Parent, String Key) {
        JsonElement Element = Parent.get(Key);

        return Element != null && Element.isJsonObject() ? Element.getAsJsonObject() : null;
    }

    private static String ReadString(JsonElement Element) {
        if (Element == null || !Element.isJsonPrimitive()) {return null;}

        return Element.getAsString();
    }

    private static String ReadValue(JsonElement Element) {
        if (Element == null || !Element.isJsonPrimitive()) {return null;}

        JsonPrimitive Primitive = Element.getAsJsonPrimitive();
        return Primitive.isNumber() ? Primitive.getAsBigDecimal().stripTrailingZeros().toPlainString() : Primitive.getAsString();
    }

    private static List<String> ReadStringList(JsonElement Element) {
        List<String> Values = new ArrayList<>();
        if (Element == null) {return Values;}

        if (Element.isJsonArray()) {
            for (JsonElement Entry : Element.getAsJsonArray()) {
                String Value = ReadString(Entry);
                if (Value != null) {Values.add(Value);}
            }
            return Values;
        }

        String Value = ReadString(Element);
        if (Value != null) {Values.add(Value);}

        return Values;
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

    private static EntityAnimationDefinition ParseAnimationSafe(String Identifier, JsonObject AnimationObject, Path AnimationFile) {
        try {
            return ParseAnimation(Identifier, AnimationObject);
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
            LoadAnimationFolder(AnimationFolder, Animations);
        }

        JavaUnbound.LOGGER.debug("Finished animation search | Found: {}", Animations.size());
        return Animations;
    }

    private static void LoadAnimationFolder(Path AnimationFolder, HashMap<String, EntityAnimationDefinition> Animations) {
        if (!Files.isDirectory(AnimationFolder)) {
            JavaUnbound.LOGGER.error("Animation folder does not exist: {}", AnimationFolder);
            return;
        }

        JavaUnbound.LOGGER.debug("Searching animation files in: {}", AnimationFolder);

        try (Stream<Path> FilesStream = Files.walk(AnimationFolder)) {
            FilesStream
                    .filter(Files::isRegularFile)
                    .filter(File -> File.toString().endsWith(".json"))
                    .forEach(AnimationFile -> {
                        JsonObject Root = ReadJson(AnimationFile);
                        if (Root == null) {return;}

                        JsonObject AnimationsObject = ReadObject(Root, "animations");
                        if (AnimationsObject == null) {return;}

                        for (Map.Entry<String, JsonElement> Entry : AnimationsObject.entrySet()) {
                            String Identifier = Entry.getKey();

                            if (Identifier == null || Identifier.isBlank()) {continue;}
                            if (!Entry.getValue().isJsonObject()) {continue;}

                            if (Animations.containsKey(Identifier)) {
                                JavaUnbound.LOGGER.debug("Skipping animation (already loaded from higher priority folder): {} | File: {}", Identifier, AnimationFile);
                                continue;
                            }

                            EntityAnimationDefinition Definition = ParseAnimationSafe(Identifier, Entry.getValue().getAsJsonObject(), AnimationFile);
                            if (Definition == null) {continue;}

                            Animations.put(Identifier, Definition);

                            JavaUnbound.LOGGER.debug("Found animation: {} | File: {} | Bones: {}", Identifier, AnimationFile, Definition.Bones.size());
                        }
                    });
        } catch (IOException Exception) {
            JavaUnbound.LOGGER.error("Failed to search animation files: {}", AnimationFolder, Exception);
        }
    }

    private static synchronized HashMap<String, EntityAnimationDefinition> GetAvailableAnimations() {
        if (CachedAvailableAnimations == null) {
            JavaUnbound.LOGGER.debug("Loading animation cache...");

            CachedAvailableAnimations = LoadAnimationFiles();

            JavaUnbound.LOGGER.debug("Animation cache loaded | Animations: {}", CachedAvailableAnimations.size());
        }

        return CachedAvailableAnimations;
    }

    private static List<EntityAnimationReference> ResolveEntityAnimations(Path EntityFile) {
        Path CacheKey = EntityFile.toAbsolutePath().normalize();

        List<EntityAnimationReference> CachedAnimations = CachedEntityAnimations.get(CacheKey);

        if (CachedAnimations != null) {
            return CachedAnimations;
        }

        List<EntityAnimationReference> Animations = new ArrayList<>();
        JsonObject Description = GetEntityDescription(EntityFile);

        if (Description == null) {
            CachedEntityAnimations.put(CacheKey, Animations);
            return Animations;
        }

        LinkedHashMap<String, String> AnimationMap = ParseAnimationMap(Description);

        HashMap<String, String> Conditions = new HashMap<>();
        JsonArray AnimateArray = GetAnimateArray(Description, EntityFile);

        if (AnimateArray != null) {
            for (EntityAnimationReference AnimateReference : ParseReferences(AnimateArray)) {
                if (AnimateReference.Condition == null) {continue;}
                Conditions.putIfAbsent(AnimateReference.ShortName, AnimateReference.Condition);
            }
        }

        HashMap<String, EntityAnimationDefinition> AvailableAnimations = GetAvailableAnimations();

        int AnimationReferenceCount = 0;

        for (Map.Entry<String, String> Entry : AnimationMap.entrySet()) {
            String ShortName = Entry.getKey();
            String Identifier = Entry.getValue();

            if (!Identifier.startsWith("animation.")) {continue;}

            AnimationReferenceCount++;

            EntityAnimationDefinition Definition = AvailableAnimations.get(Identifier);

            if (Definition == null) {
                JavaUnbound.LOGGER.warn("Could not resolve animation: {} ({}) | Entity: {}", Identifier, ShortName, EntityFile);
                continue;
            }

            EntityAnimationReference Reference = new EntityAnimationReference();
            Reference.ShortName = ShortName;
            Reference.Identifier = Identifier;
            Reference.Definition = Definition;
            Reference.Condition = Conditions.get(ShortName);

            Animations.add(Reference);

            JavaUnbound.LOGGER.debug("Resolved animation: {} ({}) | Condition: {}", Identifier, ShortName, Reference.Condition);
        }

        JavaUnbound.LOGGER.debug("Finished resolving entity animations: {} | Resolved: {}/{}", EntityFile, Animations.size(), AnimationReferenceCount);
        CachedEntityAnimations.put(CacheKey, Animations);

        return Animations;
    }

    public static synchronized List<EntityAnimationReference> GetEntityAnimations(Path EntityFile) {
        return new ArrayList<>(ResolveEntityAnimations(EntityFile));
    }

    public static synchronized EntityAnimationReference GetEntityAnimation(Path EntityFile, String Identifier) {
        if (Identifier == null || Identifier.isBlank()) {
            JavaUnbound.LOGGER.error("Cannot resolve animation: identifier is null or blank.");
            return null;
        }

        for (EntityAnimationReference Reference : ResolveEntityAnimations(EntityFile)) {
            if (Identifier.equals(Reference.Identifier) || Identifier.equals(Reference.ShortName)) {
                return Reference;
            }
        }

        JavaUnbound.LOGGER.debug("Animation not found: {} | Entity: {}", Identifier, EntityFile);
        return null;
    }

    public static synchronized EntityAnimationDefinition GetAnimation(String Identifier) {
        if (Identifier == null || Identifier.isBlank()) {return null;}

        return GetAvailableAnimations().get(Identifier);
    }

    public static synchronized void ClearCache() {
        CachedAvailableAnimations = null;
        CachedAnimationJson = null;
        CachedEntityAnimations.clear();

        JavaUnbound.LOGGER.debug("Cleared entity animation cache.");
    }
}