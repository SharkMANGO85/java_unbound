package com.java_unbound.loader.definitions.entity.resolvers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.java_unbound.JavaUnbound;
import com.java_unbound.loader.definitions.entity.types.animation_controller.*;
import com.java_unbound.loader.resourcepack.Folder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;

public class EntityAnimationControllerResolver {
    private static HashMap<String, EntityAnimationControllerDefinition> CachedAvailableAnimationControllers;
    private static final HashMap<Path, List<EntityAnimationControllerReference>> CachedEntityAnimationControllers = new HashMap<>();
    private static HashMap<String, JsonObject> CachedAnimationControllerJson;

    public static List<Path> GetAnimationControllerFolders() {
        Path ConfigFolder = Folder.GetConfigFolder();
        List<Path> AnimationControllerFolders = new ArrayList<>();

        if (ConfigFolder.resolve("subpacks").resolve(JavaUnbound.SUB_PACK).resolve("animation_controllers").toFile().exists()) {
            AnimationControllerFolders.add(ConfigFolder.resolve("subpacks").resolve(JavaUnbound.SUB_PACK).resolve("animation_controllers"));
        }

        if (ConfigFolder.resolve("animation_controllers").toFile().exists()) {
            AnimationControllerFolders.add(ConfigFolder.resolve("animation_controllers"));
        }

        return AnimationControllerFolders;
    }

    private static JsonObject ReadJson(Path JsonFile) {
        try {
            return JsonParser.parseString(Files.readString(JsonFile)).getAsJsonObject();
        } catch (Exception Exception) {
            JavaUnbound.LOGGER.error("Failed to read JSON file: {}", JsonFile, Exception);
            return null;
        }
    }
    private static synchronized HashMap<String, JsonObject> GetAvailableAnimationControllerJson() {
        if (CachedAnimationControllerJson == null) {
            CachedAnimationControllerJson = new HashMap<>();

            for (Path AnimationControllerFolder : GetAnimationControllerFolders()) {
                if (!Files.isDirectory(AnimationControllerFolder)) {continue;}

                try (Stream<Path> FilesStream = Files.walk(AnimationControllerFolder)) {
                    FilesStream.filter(Files::isRegularFile)
                            .filter(File -> File.toString().endsWith(".json"))
                            .forEach(AnimationControllerFile -> {
                                JsonObject Root = ReadJson(AnimationControllerFile);
                                if (Root == null) {return;}

                                JsonObject ControllersObject = ReadObject(Root, "animation_controllers");
                                if (ControllersObject == null) {return;}

                                for (Map.Entry<String, JsonElement> Entry : ControllersObject.entrySet()) {
                                    String Identifier = Entry.getKey();
                                    if (Identifier == null || Identifier.isBlank() || !Entry.getValue().isJsonObject()) {continue;}
                                    if (CachedAnimationControllerJson.containsKey(Identifier)) {continue;}

                                    CachedAnimationControllerJson.put(Identifier, Entry.getValue().getAsJsonObject());
                                }
                            });
                } catch (IOException Exception) {
                    JavaUnbound.LOGGER.error("Failed to search animation controller JSON files: {}", AnimationControllerFolder, Exception);
                }
            }
        }

        return CachedAnimationControllerJson;
    }

    public static synchronized JsonObject GetAnimationControllerJson(String Identifier) {
        if (Identifier == null || Identifier.isBlank()) {return null;}

        JsonObject Json = GetAvailableAnimationControllerJson().get(Identifier);

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
            JavaUnbound.LOGGER.warn("No scripts found: {}", EntityFile);
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
            JavaUnbound.LOGGER.warn("No scripts.animate found: {}", EntityFile);
            return null;
        }

        return AnimateArray;
    }

    private static HashMap<String, String> ParseAnimationMap(JsonObject Description) {
        HashMap<String, String> Result = new HashMap<>();

        JsonObject AnimationsObject = ReadObject(Description, "animations");
        if (AnimationsObject == null) {return Result;}

        for (Map.Entry<String, JsonElement> Entry : AnimationsObject.entrySet()) {
            String Value = ReadString(Entry.getValue());
            if (Value == null || Value.isBlank()) {continue;}

            Result.put(Entry.getKey(), Value);
        }

        return Result;
    }

    private static List<EntityAnimationControllerReference> ParseReferences(JsonArray AnimateArray) {
        List<EntityAnimationControllerReference> References = new ArrayList<>();

        for (JsonElement Element : AnimateArray) {
            if (Element.isJsonPrimitive()) {
                String ShortName = Element.getAsString();
                if (ShortName.isBlank()) {continue;}

                EntityAnimationControllerReference Reference = new EntityAnimationControllerReference();
                Reference.ShortName = ShortName;

                References.add(Reference);

                continue;
            }

            if (Element.isJsonObject()) {
                for (Map.Entry<String, JsonElement> Entry : Element.getAsJsonObject().entrySet()) {
                    if (Entry.getKey().isBlank()) {continue;}

                    String Condition = ReadString(Entry.getValue());
                    if (Condition == null) {continue;}

                    EntityAnimationControllerReference Reference = new EntityAnimationControllerReference();
                    Reference.ShortName = Entry.getKey();
                    Reference.Condition = Condition;

                    References.add(Reference);
                }
            }
        }

        return References;
    }

    private static EntityAnimationControllerDefinition ParseAnimationController(String Identifier, JsonObject Json) {
        EntityAnimationControllerDefinition Definition = new EntityAnimationControllerDefinition();

        Definition.Identifier = Identifier;

        String InitialState = ReadString(Json.get("initial_state"));
        if (InitialState != null && !InitialState.isBlank()) {Definition.InitialState = InitialState;}

        JsonObject StatesObject = ReadObject(Json, "states");

        if (StatesObject != null) {
            for (Map.Entry<String, JsonElement> Entry : StatesObject.entrySet()) {
                if (Entry.getKey().isBlank()) {continue;}
                if (!Entry.getValue().isJsonObject()) {continue;}

                Definition.States.put(Entry.getKey(), ParseState(Entry.getKey(), Entry.getValue().getAsJsonObject()));
            }
        }

        if (!Definition.States.containsKey(Definition.InitialState)) {
            JavaUnbound.LOGGER.warn("Animation controller has no initial state '{}': {}", Definition.InitialState, Identifier);
        }

        return Definition;
    }

    private static EntityAnimationControllerState ParseState(String Name, JsonObject Json) {
        EntityAnimationControllerState State = new EntityAnimationControllerState();

        State.Name = Name;
        State.Animations = ParseAnimations(ReadArray(Json, "animations"));
        State.Transitions = ParseTransitions(ReadArray(Json, "transitions"));
        State.OnEntry = ParseStringList(ReadArray(Json, "on_entry"));
        State.OnExit = ParseStringList(ReadArray(Json, "on_exit"));
        State.ParticleEffects = ParseEffects(ReadArray(Json, "particle_effects"));
        State.SoundEffects = ParseEffects(ReadArray(Json, "sound_effects"));
        State.BlendViaShortestPath = ReadBoolean(Json, "blend_via_shortest_path", State.BlendViaShortestPath);

        ParseBlendTransition(Json.get("blend_transition"), State);

        return State;
    }

    private static List<EntityAnimationControllerAnimation> ParseAnimations(JsonArray Array) {
        List<EntityAnimationControllerAnimation> Animations = new ArrayList<>();
        if (Array == null) {return Animations;}

        for (JsonElement Element : Array) {
            if (Element.isJsonPrimitive()) {
                String Name = Element.getAsString();
                if (Name.isBlank()) {continue;}

                EntityAnimationControllerAnimation Animation = new EntityAnimationControllerAnimation();
                Animation.Name = Name;
                Animations.add(Animation);
                continue;
            }

            if (Element.isJsonObject()) {
                for (Map.Entry<String, JsonElement> Entry : Element.getAsJsonObject().entrySet()) {
                    if (Entry.getKey().isBlank()) {continue;}

                    EntityAnimationControllerAnimation Animation = new EntityAnimationControllerAnimation();
                    Animation.Name = Entry.getKey();
                    Animation.Blend = ReadString(Entry.getValue());

                    Animations.add(Animation);
                }
            }
        }

        return Animations;
    }

    private static List<EntityAnimationControllerTransition> ParseTransitions(JsonArray Array) {
        List<EntityAnimationControllerTransition> Transitions = new ArrayList<>();
        if (Array == null) {return Transitions;}

        for (JsonElement Element : Array) {
            if (!Element.isJsonObject()) {continue;}

            for (Map.Entry<String, JsonElement> Entry : Element.getAsJsonObject().entrySet()) {
                if (Entry.getKey().isBlank()) {continue;}

                String Condition = ReadString(Entry.getValue());
                if (Condition == null) {continue;}

                EntityAnimationControllerTransition Transition = new EntityAnimationControllerTransition();
                Transition.TargetState = Entry.getKey();
                Transition.Condition = Condition;
                Transitions.add(Transition);
            }
        }

        return Transitions;
    }

    private static List<String> ParseStringList(JsonArray Array) {
        List<String> Values = new ArrayList<>();
        if (Array == null) {return Values;}

        for (JsonElement Element : Array) {
            String Value = ReadString(Element);
            if (Value != null) {Values.add(Value);}
        }

        return Values;
    }

    private static List<EntityAnimationControllerEffect> ParseEffects(JsonArray Array) {
        List<EntityAnimationControllerEffect> Effects = new ArrayList<>();
        if (Array == null) {return Effects;}

        for (JsonElement Element : Array) {
            if (!Element.isJsonObject()) {continue;}

            JsonObject Object = Element.getAsJsonObject();
            String EffectName = ReadString(Object.get("effect"));
            if (EffectName == null || EffectName.isBlank()) {continue;}

            EntityAnimationControllerEffect Effect = new EntityAnimationControllerEffect();
            Effect.Effect = EffectName;
            Effect.Locator = ReadString(Object.get("locator"));
            Effect.PreEffectScript = ReadString(Object.get("pre_effect_script"));

            Effects.add(Effect);
        }

        return Effects;
    }

    private static void ParseBlendTransition(JsonElement Element, EntityAnimationControllerState State) {
        if (Element == null) {return;}

        if (Element.isJsonPrimitive() && Element.getAsJsonPrimitive().isNumber()) {
            State.BlendTransition = Element.getAsFloat();
            return;
        }

        if (!Element.isJsonObject()) {return;}

        TreeMap<Float, Float> Curve = new TreeMap<>();

        for (Map.Entry<String, JsonElement> Entry : Element.getAsJsonObject().entrySet()) {
            try {
                if (!Entry.getValue().isJsonPrimitive()) {continue;}
                Curve.put(Float.parseFloat(Entry.getKey()), Entry.getValue().getAsFloat());
            } catch (NumberFormatException Exception) {
                JavaUnbound.LOGGER.warn("Invalid blend_transition keyframe: {} | State: {}", Entry.getKey(), State.Name);
            }
        }

        if (Curve.isEmpty()) {return;}

        State.BlendTransitionCurve = Curve;
        State.BlendTransition = Curve.lastKey();
    }

    private static JsonObject ReadObject(JsonObject Parent, String Key) {
        JsonElement Element = Parent.get(Key);
        return Element != null && Element.isJsonObject() ? Element.getAsJsonObject() : null;
    }

    private static JsonArray ReadArray(JsonObject Parent, String Key) {
        JsonElement Element = Parent.get(Key);
        return Element != null && Element.isJsonArray() ? Element.getAsJsonArray() : null;
    }

    private static String ReadString(JsonElement Element) {
        if (Element == null || !Element.isJsonPrimitive()) {return null;}
        return Element.getAsString();
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

    private static EntityAnimationControllerDefinition ParseAnimationControllerSafe(String Identifier, JsonObject AnimationControllerObject, Path AnimationControllerFile) {
        try {
            return ParseAnimationController(Identifier, AnimationControllerObject);
        } catch (Exception Exception) {
            JavaUnbound.LOGGER.error("Failed to deserialize animation controller: {} | File: {}", Identifier, AnimationControllerFile, Exception);
            return null;
        }
    }

    private static HashMap<String, EntityAnimationControllerDefinition> LoadAnimationControllerFiles() {
        HashMap<String, EntityAnimationControllerDefinition> AnimationControllers = new HashMap<>();

        List<Path> AnimationControllerFolders = GetAnimationControllerFolders();

        if (AnimationControllerFolders.isEmpty()) {
            JavaUnbound.LOGGER.error("No animation controller folders exist.");
            return AnimationControllers;
        }

        for (Path AnimationControllerFolder : AnimationControllerFolders) {
            LoadAnimationControllerFolder(AnimationControllerFolder, AnimationControllers);
        }

        JavaUnbound.LOGGER.debug("Finished animation controller search | Found: {}", AnimationControllers.size());
        return AnimationControllers;
    }

    private static void LoadAnimationControllerFolder(Path AnimationControllerFolder, HashMap<String, EntityAnimationControllerDefinition> AnimationControllers) {
        if (!Files.isDirectory(AnimationControllerFolder)) {
            JavaUnbound.LOGGER.error("Animation controller folder does not exist: {}", AnimationControllerFolder);
            return;
        }

        JavaUnbound.LOGGER.debug("Searching animation controller files in: {}", AnimationControllerFolder);

        try (Stream<Path> FilesStream = Files.walk(AnimationControllerFolder)) {
            FilesStream
                    .filter(Files::isRegularFile)
                    .filter(File -> File.toString().endsWith(".json"))
                    .forEach(AnimationControllerFile -> {
                        JsonObject Root = ReadJson(AnimationControllerFile);
                        if (Root == null) {return;}

                        JsonObject ControllersObject = ReadObject(Root, "animation_controllers");
                        if (ControllersObject == null) {return;}

                        for (Map.Entry<String, JsonElement> Entry : ControllersObject.entrySet()) {
                            String Identifier = Entry.getKey();

                            if (Identifier == null || Identifier.isBlank()) {continue;}
                            if (!Entry.getValue().isJsonObject()) {continue;}

                            if (AnimationControllers.containsKey(Identifier)) {
                                JavaUnbound.LOGGER.debug("Skipping animation controller (already loaded from higher priority folder): {} | File: {}", Identifier, AnimationControllerFile);
                                continue;
                            }

                            EntityAnimationControllerDefinition Definition = ParseAnimationControllerSafe(Identifier, Entry.getValue().getAsJsonObject(), AnimationControllerFile);
                            if (Definition == null) {continue;}

                            AnimationControllers.put(Identifier, Definition);

                            JavaUnbound.LOGGER.debug("Found animation controller: {} | File: {} | States: {}", Identifier, AnimationControllerFile, Definition.States.size());
                        }
                    });
        } catch (IOException Exception) {
            JavaUnbound.LOGGER.error("Failed to search animation controller files: {}", AnimationControllerFolder, Exception);
        }
    }

    private static synchronized HashMap<String, EntityAnimationControllerDefinition> GetAvailableAnimationControllers() {
        if (CachedAvailableAnimationControllers == null) {
            JavaUnbound.LOGGER.debug("Loading animation controller cache...");

            CachedAvailableAnimationControllers = LoadAnimationControllerFiles();

            JavaUnbound.LOGGER.debug("Animation controller cache loaded | Animation controllers: {}", CachedAvailableAnimationControllers.size());
        }

        return CachedAvailableAnimationControllers;
    }

    private static List<EntityAnimationControllerReference> ResolveEntityAnimationControllers(Path EntityFile) {
        Path CacheKey = EntityFile.toAbsolutePath().normalize();

        List<EntityAnimationControllerReference> CachedAnimationControllers = CachedEntityAnimationControllers.get(CacheKey);

        if (CachedAnimationControllers != null) {
            return CachedAnimationControllers;
        }

        JavaUnbound.LOGGER.debug("Resolving entity animation controllers from: {}", EntityFile);

        List<EntityAnimationControllerReference> AnimationControllers = new ArrayList<>();

        JsonObject Description = GetEntityDescription(EntityFile);
        JsonArray AnimateArray = Description != null ? GetAnimateArray(Description, EntityFile) : null;

        if (AnimateArray == null) {
            CachedEntityAnimationControllers.put(CacheKey, AnimationControllers);
            return AnimationControllers;
        }

        HashMap<String, String> AnimationMap = ParseAnimationMap(Description);
        List<EntityAnimationControllerReference> References = ParseReferences(AnimateArray);
        HashMap<String, EntityAnimationControllerDefinition> AvailableAnimationControllers = GetAvailableAnimationControllers();

        int ControllerReferenceCount = 0;

        for (EntityAnimationControllerReference Reference : References) {
            String Identifier = AnimationMap.get(Reference.ShortName);

            if (Identifier == null) {
                JavaUnbound.LOGGER.warn("Animate entry has no entry in animations: {} | Entity: {}", Reference.ShortName, EntityFile);
                continue;
            }

            if (!Identifier.startsWith("controller.")) {continue;}

            ControllerReferenceCount++;

            EntityAnimationControllerDefinition Definition = AvailableAnimationControllers.get(Identifier);

            if (Definition == null) {
                JavaUnbound.LOGGER.warn("Could not resolve animation controller: {} ({}) | Entity: {}", Identifier, Reference.ShortName, EntityFile);
                continue;
            }

            Reference.Identifier = Identifier;
            Reference.Definition = Definition;
            AnimationControllers.add(Reference);

            JavaUnbound.LOGGER.debug("Resolved animation controller: {} ({}) | Condition: {}", Identifier, Reference.ShortName, Reference.Condition);
        }

        JavaUnbound.LOGGER.debug("Finished resolving entity animation controllers: {} | Resolved: {}/{}", EntityFile, AnimationControllers.size(), ControllerReferenceCount);
        CachedEntityAnimationControllers.put(CacheKey, AnimationControllers);

        return AnimationControllers;
    }

    public static synchronized List<EntityAnimationControllerReference> GetEntityAnimationControllers(Path EntityFile) {
        return new ArrayList<>(ResolveEntityAnimationControllers(EntityFile));
    }

    public static synchronized EntityAnimationControllerReference GetEntityAnimationController(Path EntityFile, String Identifier) {
        if (Identifier == null || Identifier.isBlank()) {
            JavaUnbound.LOGGER.error("Cannot resolve animation controller: identifier is null or blank.");
            return null;
        }

        for (EntityAnimationControllerReference Reference : ResolveEntityAnimationControllers(EntityFile)) {
            if (Identifier.equals(Reference.Identifier) || Identifier.equals(Reference.ShortName)) {return Reference;}
        }

        JavaUnbound.LOGGER.debug("Animation controller not found: {} | Entity: {}", Identifier, EntityFile);
        return null;
    }

    public static synchronized void ClearCache() {
        CachedAvailableAnimationControllers = null;
        CachedAnimationControllerJson = null;
        CachedEntityAnimationControllers.clear();

        JavaUnbound.LOGGER.debug("Cleared entity animation controller cache.");
    }
}