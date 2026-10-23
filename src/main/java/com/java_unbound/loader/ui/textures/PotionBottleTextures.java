package com.java_unbound.loader.ui.textures;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.java_unbound.JavaUnbound;
import com.java_unbound.loader.resourcepack.Folder;
import com.java_unbound.loader.resourcepack.ResourceMapper;
import com.java_unbound.loader.resourcepack.VirtualResources;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public final class PotionBottleTextures {
    private static final Path PotionTextureFolder = Folder.GetConfigFolder().resolve("subpacks").resolve("SP2").resolve("textures").resolve("items");
    private static final Gson Gson = new GsonBuilder().setPrettyPrinting().create();
    private static final Map<String, String> PotionMappings = new LinkedHashMap<>();

    static {
        PotionMappings.put("absorption", "absorption");
        PotionMappings.put("blindness", "blindness");
        PotionMappings.put("confusion", "nausea");
        PotionMappings.put("damageBoost", "strength");
        PotionMappings.put("digSlowdown", "slowness");
        PotionMappings.put("digSpeed", "haste");
        PotionMappings.put("fireResistance", "fire_resistance");
        PotionMappings.put("harm", "harming");
        PotionMappings.put("heal", "healing");
        PotionMappings.put("healthBoost", "health_boost");
        PotionMappings.put("hunger", "hunger");
        PotionMappings.put("infested", "infested");
        PotionMappings.put("invisibility", "invisibility");
        PotionMappings.put("jump", "jump_boost");
        PotionMappings.put("levitation", "levitation");
        PotionMappings.put("moveSlowdown", "slowness");
        PotionMappings.put("moveSpeed", "speed");
        PotionMappings.put("nightVision", "night_vision");
        PotionMappings.put("oozing", "oozing");
        PotionMappings.put("poison", "poison");
        PotionMappings.put("regeneration", "regeneration");
        PotionMappings.put("resistance", "resistance");
        PotionMappings.put("saturation", "saturation");
        PotionMappings.put("slowFall", "slow_falling");
        PotionMappings.put("turtleMaster", "turtle_master");
        PotionMappings.put("waterBreathing", "water_breathing");
        PotionMappings.put("weakness", "weakness");
        PotionMappings.put("weaving", "weaving");
        PotionMappings.put("windCharged", "wind_charged");
        PotionMappings.put("wither", "wither");
        PotionMappings.put("luck", "luck");
    }

    private PotionBottleTextures() {}

    public static void LoadPotionBottleTextures() {
        LoadPotionTextures("potion_bottle_", "potions");
        LoadPotionTextures("potion_bottle_splash_", "splash_potions");
        LoadPotionTextures("potion_bottle_lingering_", "lingering_potions");

        MapSpecialTextures();

        GeneratePotionItem("potion", "potions");
        GeneratePotionItem("splash_potion", "splash_potions");
        GeneratePotionItem("lingering_potion", "lingering_potions");
    }

    private static void LoadPotionTextures(String SourcePrefix, String TargetFolder) {
        for (Map.Entry<String, String> Entry : PotionMappings.entrySet()) {
            String SourceName = SourcePrefix + Entry.getKey() + ".png";
            Path SourcePath = PotionTextureFolder.resolve(SourceName);

            if (!Files.isRegularFile(SourcePath)) {continue;}

            String TargetName = Entry.getValue();
            String TargetPath = "textures/item/" + TargetFolder + "/" + TargetName + ".png";
            String SourceIdentifier = "subpacks/SP2/textures/items/" + SourceName;

            Folder.SetIdentifier(TargetPath, SourceIdentifier);

            GenerateTextureModel(TargetFolder, TargetName);
        }

        GeneratePotionVariants(TargetFolder);
    }

    private static void MapSpecialTextures() {
        MapTexture("potion_bottle_empty.png", "textures/item/glass_bottle.png");
        MapTexture("potion_overlay.png", "textures/misc/potion_overlay.png");
    }

    private static void MapTexture(String SourceName, String TargetPath) {
        Path SourcePath = PotionTextureFolder.resolve(SourceName);

        if (!Files.isRegularFile(SourcePath)) {
            JavaUnbound.LOGGER.warn("[PotionBottleTextures] Missing special texture: {}", SourcePath);
            return;
        }

        Folder.SetIdentifier(TargetPath, "subpacks/SP2/textures/items/" + SourceName);
    }

    private static void GenerateTextureModel(String TargetFolder, String TextureName) {
        JsonObject Root = new JsonObject();
        Root.addProperty("parent", "minecraft:item/generated");

        JsonObject Textures = new JsonObject();

        Textures.addProperty("layer0", "minecraft:item/" + TargetFolder + "/" + TextureName);
        Root.add("textures", Textures);
        VirtualResources.Set("models/item/" + TargetFolder + "/" + TextureName + ".json", Gson.toJson(Root));
    }

    private static void GeneratePotionVariants(String TargetFolder) {
        for (String TextureName : PotionMappings.values()) {
            String BaseTexturePath = "textures/item/" + TargetFolder + "/" + TextureName + ".png";
            if (!ResourceMapper.HasIdentifier(BaseTexturePath)) {continue;}

            GenerateVariantModel(TargetFolder, TextureName + "_long", TextureName);
            GenerateVariantModel(TargetFolder, TextureName + "2", TextureName);
        }
    }

    private static void GenerateVariantModel(String TargetFolder, String VariantName, String TextureName) {
        JsonObject Root = new JsonObject();
        Root.addProperty("parent", "minecraft:item/generated");

        JsonObject Textures = new JsonObject();

        Textures.addProperty("layer0", "minecraft:item/" + TargetFolder + "/" + TextureName);
        Root.add("textures", Textures);
        VirtualResources.Set("models/item/" + TargetFolder + "/" + VariantName + ".json", Gson.toJson(Root));
    }

    private static Map<String, String> GetVanillaPotionModels() {
        Map<String, String> Models = new LinkedHashMap<>();

        Models.put("night_vision", "night_vision");
        Models.put("long_night_vision", "night_vision_long");

        Models.put("invisibility", "invisibility");
        Models.put("long_invisibility", "invisibility_long");

        Models.put("leaping", "jump_boost");
        Models.put("long_leaping", "jump_boost_long");
        Models.put("strong_leaping", "jump_boost2");

        Models.put("fire_resistance", "fire_resistance");
        Models.put("long_fire_resistance", "fire_resistance_long");

        Models.put("swiftness", "speed");
        Models.put("long_swiftness", "speed_long");
        Models.put("strong_swiftness", "speed2");

        Models.put("slowness", "slowness");
        Models.put("long_slowness", "slowness_long");
        Models.put("strong_slowness", "slowness2");

        Models.put("turtle_master", "turtle_master");
        Models.put("long_turtle_master", "turtle_master_long");
        Models.put("strong_turtle_master", "turtle_master2");

        Models.put("water_breathing", "water_breathing");
        Models.put("long_water_breathing", "water_breathing_long");

        Models.put("healing", "healing");
        Models.put("strong_healing", "healing2");

        Models.put("harming", "harming");
        Models.put("strong_harming", "harming2");

        Models.put("poison", "poison");
        Models.put("long_poison", "poison_long");
        Models.put("strong_poison", "poison2");

        Models.put("regeneration", "regeneration");
        Models.put("long_regeneration", "regeneration_long");
        Models.put("strong_regeneration", "regeneration2");

        Models.put("strength", "strength");
        Models.put("long_strength", "strength_long");
        Models.put("strong_strength", "strength2");

        Models.put("weakness", "weakness");
        Models.put("long_weakness", "weakness_long");

        Models.put("slow_falling", "slow_falling");
        Models.put("long_slow_falling", "slow_falling_long");

        Models.put("wind_charged", "wind_charged");
        Models.put("weaving", "weaving");
        Models.put("oozing", "oozing");
        Models.put("infested", "infested");

        Models.put("luck", "luck");

        return Models;
    }

    private static void GeneratePotionItem(String ItemName, String TargetFolder) {
        JsonObject Root = new JsonObject();
        JsonObject Select = new JsonObject();

        Select.addProperty("type", "minecraft:select");
        Select.addProperty("property", "minecraft:component");
        Select.addProperty("component", "minecraft:potion_contents");

        JsonArray Cases = new JsonArray();
        Map<String, String> PotionModels = GetVanillaPotionModels();

        for (Map.Entry<String, String> Entry : PotionModels.entrySet()) {
            String PotionId = Entry.getKey();
            String ModelName = Entry.getValue();

            String TextureName = GetBaseTextureName(ModelName);
            String TexturePath = "textures/item/" + TargetFolder + "/" + TextureName + ".png";

            if (!ResourceMapper.HasIdentifier(TexturePath)) {continue;}

            JsonObject Case = new JsonObject();

            Case.addProperty("when", "minecraft:" + PotionId);

            JsonObject Model = new JsonObject();

            Model.addProperty("type", "minecraft:model");
            Model.addProperty("model", "minecraft:item/" + TargetFolder + "/" + ModelName);

            Case.add("model", Model);
            Cases.add(Case);
        }

        Select.add("cases", Cases);

        JsonObject Fallback = new JsonObject();
        Fallback.addProperty("type", "minecraft:model");

        String FallbackModel = FindFallbackModel(TargetFolder);

        Fallback.addProperty("model", "minecraft:item/" + TargetFolder + "/" + FallbackModel);
        Select.add("fallback", Fallback);
        Root.add("model", Select);
        VirtualResources.Set("items/" + ItemName + ".json", Gson.toJson(Root));
    }

    private static String GetBaseTextureName(String ModelName) {
        if (ModelName.endsWith("_long")) {
            return ModelName.substring(0, ModelName.length() - "_long".length());
        }

        if (ModelName.endsWith("2")) {
            return ModelName.substring(0, ModelName.length() - 1);
        }

        return ModelName;
    }

    private static String FindFallbackModel(String TargetFolder) {
        String[] Preferred = {"water", "mundane", "awkward", "strength", "healing"};

        for (String Candidate : Preferred) {
            String TexturePath = "textures/item/" + TargetFolder + "/" + Candidate + ".png";

            if (ResourceMapper.HasIdentifier(TexturePath)) {
                return Candidate;
            }
        }

        for (String Candidate : PotionMappings.values()) {
            String TexturePath = "textures/item/" + TargetFolder + "/" + Candidate + ".png";

            if (ResourceMapper.HasIdentifier(TexturePath)) {
                return Candidate;
            }
        }

        return "strength";
    }
}