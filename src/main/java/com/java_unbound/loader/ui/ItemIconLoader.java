package com.java_unbound.loader.ui;

import com.java_unbound.JavaUnbound;
import com.java_unbound.loader.resourcepack.Folder;
import net.minecraft.core.registries.BuiltInRegistries;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ItemIconLoader {
    private static final Path SubpackFolder = Folder.GetResourceFolder().resolve("subpacks").resolve(JavaUnbound.SUBPACK);
    private static final Path SubpackItemTextureFolder = SubpackFolder.resolve("textures").resolve("items");
    private static final Path SubpackBlockTextureFolder = SubpackFolder.resolve("textures").resolve("blocks");
    private static final Path ReportFolder = Folder.GetResourceFolder().resolve("logs");
    private static final Path UnusedTexturesFile = ReportFolder.resolve("unused_item_textures.txt");
    private static final Path ReportFile = ReportFolder.resolve("item_texture_report.txt");

    private static final Map<String, String> ResolvedCache = new ConcurrentHashMap<>();
    private static final Map<String, String> ManualMappings = new HashMap<>();
    private static final Set<String> UsedSourceTextures = new HashSet<>();
    private static final Set<String> UnusedSourceTextures = new LinkedHashSet<>();
    private static final Map<String, List<String>> TargetCollisions = new LinkedHashMap<>();

    private static final double MinimumScore = 0.82;
    private static final double MinimumScoreDifference = 0.08;

    private static int TotalTextures;
    private static int UsedTextures;
    private static int UnusedTextures;

    static {
        ManualMappings.put("apple_golden", "golden_apple");
        ManualMappings.put("carrot_golden", "golden_carrot");
        ManualMappings.put("gold_nautilus_armor", "golden_nautilus_armor");
        ManualMappings.put("wood_spear", "wooden_spear");
        ManualMappings.put("wood_axe", "wooden_axe");
        ManualMappings.put("wood_hoe", "wooden_hoe");
        ManualMappings.put("wood_pickaxe", "wooden_pickaxe");
        ManualMappings.put("wood_shovel", "wooden_shovel");
        ManualMappings.put("wood_sword", "wooden_sword");
        ManualMappings.put("gold_axe", "golden_axe");
        ManualMappings.put("gold_boots", "golden_boots");
        ManualMappings.put("gold_chestplate", "golden_chestplate");
        ManualMappings.put("gold_helmet", "golden_helmet");
        ManualMappings.put("gold_hoe", "golden_hoe");
        ManualMappings.put("gold_horse_armor", "golden_horse_armor");
        ManualMappings.put("gold_leggings", "golden_leggings");
        ManualMappings.put("gold_pickaxe", "golden_pickaxe");
        ManualMappings.put("gold_shovel", "golden_shovel");
        ManualMappings.put("gold_sword", "golden_sword");

        ManualMappings.put("beef_raw", "beef");
        ManualMappings.put("beef_cooked", "cooked_beef");
        ManualMappings.put("chicken_raw", "chicken");
        ManualMappings.put("chicken_cooked", "cooked_chicken");
        ManualMappings.put("mutton_raw", "mutton");
        ManualMappings.put("mutton_cooked", "cooked_mutton");
        ManualMappings.put("porkchop_raw", "porkchop");
        ManualMappings.put("porkchop_cooked", "cooked_porkchop");
        ManualMappings.put("rabbit_raw", "rabbit");
        ManualMappings.put("rabbit_cooked", "cooked_rabbit");
        ManualMappings.put("fish_raw", "cod");
        ManualMappings.put("fish_cooked", "cooked_cod");
        ManualMappings.put("fish_salmon_raw", "salmon");
        ManualMappings.put("fish_salmon_cooked", "cooked_salmon");
        ManualMappings.put("fish_pufferfish_raw", "pufferfish");
        ManualMappings.put("fish_clownfish_raw", "tropical_fish");
        ManualMappings.put("potato_baked", "baked_potato");
        ManualMappings.put("potato_poisonous", "poisonous_potato");
        ManualMappings.put("melon", "melon_slice");
        ManualMappings.put("melon_speckled", "glistering_melon_slice");
        ManualMappings.put("totem", "totem_of_undying");
        ManualMappings.put("nautilus", "nautilus_shell");
        ManualMappings.put("heartofthesea_closed", "heart_of_the_sea");
        ManualMappings.put("villagebell", "bell");
        ManualMappings.put("reeds", "sugar_cane");
        ManualMappings.put("netherbrick", "nether_brick");
        ManualMappings.put("slimeball", "slime_ball");
        ManualMappings.put("spider_eye_fermented", "fermented_spider_eye");
        ManualMappings.put("book_enchanted", "enchanted_book");
        ManualMappings.put("book_normal", "book");
        ManualMappings.put("book_writable", "writable_book");
        ManualMappings.put("book_written", "written_book");
        ManualMappings.put("broken_elytra", "elytra_broken");
        ManualMappings.put("fireworks", "firework_rocket");
        ManualMappings.put("fireworks_charge", "firework_star");

        ManualMappings.put("bucket_empty", "bucket");
        ManualMappings.put("bucket_lava", "lava_bucket");
        ManualMappings.put("bucket_water", "water_bucket");
        ManualMappings.put("bucket_axolotl", "axolotl_bucket");
        ManualMappings.put("bucket_cod", "cod_bucket");
        ManualMappings.put("bucket_pufferfish", "pufferfish_bucket");
        ManualMappings.put("bucket_salmon", "salmon_bucket");
        ManualMappings.put("bucket_tadpole", "tadpole_bucket");
        ManualMappings.put("bucket_tropical", "tropical_fish_bucket");
        ManualMappings.put("bucket_powder_snow", "powder_snow_bucket");
        ManualMappings.put("bucket_milk", "milk_bucket");
        ManualMappings.put("bucket_sulfur_cube", "sulfur_cube_bucket");

        ManualMappings.put("seeds_beetroot", "beetroot_seeds");
        ManualMappings.put("seeds_melon", "melon_seeds");
        ManualMappings.put("seeds_pumpkin", "pumpkin_seeds");
        ManualMappings.put("seeds_wheat", "wheat_seeds");

        ManualMappings.put("dye_powder_black", "black_dye");
        ManualMappings.put("dye_powder_blue", "blue_dye");
        ManualMappings.put("dye_powder_brown", "brown_dye");
        ManualMappings.put("dye_powder_cyan", "cyan_dye");
        ManualMappings.put("dye_powder_gray", "gray_dye");
        ManualMappings.put("dye_powder_green", "green_dye");
        ManualMappings.put("dye_powder_light_blue", "light_blue_dye");
        ManualMappings.put("dye_powder_lime", "lime_dye");
        ManualMappings.put("dye_powder_magenta", "magenta_dye");
        ManualMappings.put("dye_powder_orange", "orange_dye");
        ManualMappings.put("dye_powder_pink", "pink_dye");
        ManualMappings.put("dye_powder_purple", "purple_dye");
        ManualMappings.put("dye_powder_red", "red_dye");
        ManualMappings.put("dye_powder_silver", "light_gray_dye");
        ManualMappings.put("dye_powder_yellow", "yellow_dye");
        ManualMappings.put("dye_powder_white_new", "white_dye");
        ManualMappings.put("dye_powder_black_new", "black_dye");

        ManualMappings.put("turtle_shell_piece", "turtle_scute");
        ManualMappings.put("fishing_rod_cast", "fishing_rod_cast");
        ManualMappings.put("fishing_rod_uncast", "fishing_rod");
        ManualMappings.put("dye_powder_white", "bone_meal");
        ManualMappings.put("map_empty", "map");
        ManualMappings.put("map_filled", "filled_map");

        ManualMappings.put("potion_bottle_absorption", "potion");
        ManualMappings.put("potion_bottle_lingering", "lingering_potion");
        ManualMappings.put("potion_bottle_splash", "splash_potion");
        ManualMappings.put("potion_overlay", "potion_overlay");

        ManualMappings.put("tipped_arrow_base", "tipped_arrow_base");
        ManualMappings.put("tipped_arrow_head", "tipped_arrow_head");

        ManualMappings.put("crossbow_standby", "crossbow_standby");
        ManualMappings.put("crossbow_pulling_0", "crossbow_pulling_0");
        ManualMappings.put("crossbow_pulling_1", "crossbow_pulling_1");
        ManualMappings.put("crossbow_pulling_2", "crossbow_pulling_2");
        ManualMappings.put("crossbow_arrow", "crossbow_arrow");

        ManualMappings.put("bow_standby", "bow");
        ManualMappings.put("bow_pulling_0", "bow_pulling_0");
        ManualMappings.put("bow_pulling_1", "bow_pulling_1");
        ManualMappings.put("bow_pulling_2", "bow_pulling_2");
        ManualMappings.put("bow_arrow", "bow_arrow");

        ManualMappings.put("redstone_dust", "redstone");
        ManualMappings.put("sign_darkoak", "dark_oak_sign");

        ManualMappings.put("blocks/activator_rail_carried", "block/activator_rail");
        ManualMappings.put("blocks/allium_carried", "block/allium");
        ManualMappings.put("blocks/amethyst_cluster_carried", "block/amethyst_cluster");
        ManualMappings.put("blocks/azure_bluet_carried", "block/azure_bluet");
        ManualMappings.put("blocks/bamboo_sapling_carried", "block/bamboo_sapling");
        ManualMappings.put("blocks/blue_orchid_carried", "block/blue_orchid");
        ManualMappings.put("blocks/cactus_flower_carried", "block/cactus_flower");
        ManualMappings.put("blocks/cherry_sapling_carried", "block/cherry_sapling");
        ManualMappings.put("blocks/copper_bars_carried", "block/copper_bars");
        ManualMappings.put("blocks/copper_torch_carried", "block/copper_torch");

        ManualMappings.put("blocks/cornflower_carried", "block/cornflower");
        ManualMappings.put("blocks/crimson_fungus_carried", "block/crimson_fungus");
        ManualMappings.put("blocks/crimson_roots_carried", "block/crimson_roots");
        ManualMappings.put("blocks/dandelion_carried", "block/dandelion");
        ManualMappings.put("blocks/deadbush_carried", "block/dead_bush");

        ManualMappings.put("blocks/detector_rail_carried", "block/detector_rail");
        ManualMappings.put("blocks/end_rod_carried", "block/end_rod");
        ManualMappings.put("blocks/exposed_copper_bars_carried", "block/exposed_copper_bars");
        ManualMappings.put("blocks/eyeblossom_blooming_carried", "block/open_eyeblossom");
        ManualMappings.put("blocks/eyeblossom_dormant_carried", "block/closed_eyeblossom");
        ManualMappings.put("blocks/frogspawn_carried", "block/frogspawn");
        ManualMappings.put("blocks/glow_lichen_carried", "block/glow_lichen");
        ManualMappings.put("blocks/golden_dandelion_carried", "block/golden_dandelion");
        ManualMappings.put("blocks/hanging_roots_carried", "block/hanging_roots");

        ManualMappings.put("blocks/iron_bars_carried", "block/iron_bars");
        ManualMappings.put("blocks/ladder_carried", "block/ladder");
        ManualMappings.put("blocks/lever_carried", "block/lever");
        ManualMappings.put("blocks/lightning_rod_carried", "block/lightning_rod");
        ManualMappings.put("blocks/lilac_carried", "block/lilac");
        ManualMappings.put("blocks/lily_of_the_valley_carried", "block/lily_of_the_valley");

        ManualMappings.put("blocks/mushroom_brown_carried", "block/brown_mushroom");
        ManualMappings.put("blocks/mushroom_red_carried", "block/red_mushroom");
        ManualMappings.put("blocks/oxeye_daisy_carried", "block/oxeye_daisy");
        ManualMappings.put("blocks/oxidized_copper_bars_carried", "block/oxidized_copper_bars");

        ManualMappings.put("blocks/pale_hanging_moss_carried", "block/pale_hanging_moss");
        ManualMappings.put("blocks/pale_oak_sapling_carried", "block/pale_oak_sapling");
        ManualMappings.put("blocks/poppy_carried", "block/poppy");

        ManualMappings.put("blocks/powered_rail_carried", "block/powered_rail");
        ManualMappings.put("blocks/rail_carried", "block/rail");
        ManualMappings.put("blocks/redstone_torch_carried", "block/redstone_torch");
        ManualMappings.put("blocks/rose_bush_carried", "block/rose_bush");

        ManualMappings.put("blocks/sapling_acacia_carried", "block/acacia_sapling");
        ManualMappings.put("blocks/sapling_birch_carried", "block/birch_sapling");
        ManualMappings.put("blocks/sapling_jungle_carried", "block/jungle_sapling");
        ManualMappings.put("blocks/sapling_oak_carried", "block/oak_sapling");
        ManualMappings.put("blocks/sapling_roofed_oak_carried", "block/dark_oak_sapling");
        ManualMappings.put("blocks/sapling_spruce_carried", "block/spruce_sapling");

        ManualMappings.put("blocks/sculk_vein_carried", "block/sculk_vein");
        ManualMappings.put("blocks/short_dry_grass_carried", "block/short_dry_grass");
        ManualMappings.put("blocks/soul_torch_carried", "block/soul_torch");
        ManualMappings.put("blocks/spore_blossom_carried", "block/spore_blossom");
        ManualMappings.put("blocks/sulfur_spike_carried", "block/sulfur_spike");
        ManualMappings.put("blocks/sunflower_carried", "block/sunflower");
        ManualMappings.put("blocks/tall_dry_grass_carried", "block/tall_dry_grass");
        ManualMappings.put("blocks/torch_carried", "block/torch");
        ManualMappings.put("blocks/torchflower_carried", "block/torchflower");

        ManualMappings.put("blocks/trip_wire_carried", "block/tripwire");

        ManualMappings.put("blocks/tulip_orange_carried", "block/orange_tulip");
        ManualMappings.put("blocks/tulip_pink_carried", "block/pink_tulip");
        ManualMappings.put("blocks/tulip_red_carried", "block/red_tulip");
        ManualMappings.put("blocks/tulip_white_carried", "block/white_tulip");

        ManualMappings.put("blocks/twisting_vines_carried", "block/twisting_vines");
        ManualMappings.put("blocks/warped_fungus_carried", "block/warped_fungus");
        ManualMappings.put("blocks/warped_roots_carried", "block/warped_roots");
        ManualMappings.put("blocks/weathered_copper_bars_carried", "block/weathered_copper_bars");
        ManualMappings.put("blocks/web_carried", "block/cobweb");
        ManualMappings.put("blocks/weeping_vines_carried", "block/weeping_vines");
        ManualMappings.put("blocks/wither_rose_carried", "block/wither_rose");

        ManualMappings.put("blocks/amethyst_bud_large_carried", "block/large_amethyst_bud");
        ManualMappings.put("blocks/amethyst_bud_medium_carried", "block/medium_amethyst_bud");
        ManualMappings.put("blocks/amethyst_bud_small_carried", "block/small_amethyst_bud");

        //ManualMappings.put("map_locked", "filled_map");
        //ManualMappings.put("map_mansion", "filled_map");
        //ManualMappings.put("map_monument", "filled_map");
        //ManualMappings.put("map_nautilus", "filled_map");
        //ManualMappings.put("map_trial_chambers", "filled_map");
    }

    private ItemIconLoader() {
    }

    public static void LoadItemIcons() {
        ResetCounters();

        if (!Files.exists(SubpackItemTextureFolder) && !Files.exists(SubpackBlockTextureFolder)) {
            SaveReport();
            return;
        }

        Set<String> JavaItemNames = GetMinecraftItemNames();
        Map<String, String> NormalizedJavaNames = BuildNormalizedNames(JavaItemNames);

        try {
            if (Files.exists(SubpackItemTextureFolder)) {
                try (var Stream = Files.walk(SubpackItemTextureFolder)) {
                    Stream.filter(Files::isRegularFile).filter(ItemIconLoader::IsPng).forEach(Path -> ProcessTexture(Path, "items", JavaItemNames, NormalizedJavaNames));
                }
            }

            ProcessBlockMappings();

        } catch (IOException E) {
            JavaUnbound.LOGGER.error("Failed to load item textures", E);
        }

        SaveReport();
    }

    private static void ProcessTexture(Path Path, String TextureFolder, Set<String> JavaItemNames, Map<String, String> NormalizedJavaNames) {
        TotalTextures++;

        String SourceFileName = Path.getFileName().toString();
        String SourceName = RemoveExtension(SourceFileName);
        String RelativePath = GetRelativePath(Path, TextureFolder);
        String MappingName = TextureFolder + "/" + SourceName;

        if (ShouldIgnore(SourceName)) {
            UnusedSourceTextures.add(TextureFolder + "/" + RelativePath);
            UnusedTextures++;

            return;
        }

        String TargetFileName = ResolveTargetName(MappingName, SourceName, JavaItemNames, NormalizedJavaNames);

        if (TargetFileName == null) {
            UnusedSourceTextures.add(TextureFolder + "/" + RelativePath);
            UnusedTextures++;

            return;
        }

        String SourcePath = "subpacks/" + JavaUnbound.SUBPACK + "/textures/" + TextureFolder + "/" + RelativePath;
        String TargetPath = "textures/item/" + TargetFileName + ".png";
        List<String> Sources = TargetCollisions.computeIfAbsent(TargetPath, Key -> new ArrayList<>());

        Sources.add(TextureFolder + "/" + RelativePath);

        boolean IsManualMapping = ManualMappings.containsKey(MappingName) || ManualMappings.containsKey(SourceName);

        if (!IsManualMapping && Sources.size() > 1) {UnusedSourceTextures.add(TextureFolder + "/" + RelativePath);
            UnusedTextures++;

            return;
        }

        Folder.SetIdentifier(TargetPath, SourcePath);
        UsedSourceTextures.add(TextureFolder + "/" + RelativePath);

        UsedTextures++;

        JavaUnbound.LOGGER.info("Loaded item texture: " + SourcePath + " -> " + TargetPath);
    }

    private static void ProcessBlockMappings() {
        if (!Files.exists(SubpackBlockTextureFolder)) {
            JavaUnbound.LOGGER.warn("Block texture folder does not exist: " + SubpackBlockTextureFolder);
            return;
        }

        for (Map.Entry<String, String> Entry : ManualMappings.entrySet()) {
            String MappingName = Entry.getKey();

            if (!MappingName.startsWith("blocks/")) {
                continue;
            }

            String TargetName = Entry.getValue();
            Path SourcePath = FindBlockTexture(MappingName);

            if (SourcePath == null) {
                JavaUnbound.LOGGER.warn("Could not find block texture for mapping: " + MappingName);
                continue;
            }

            String RelativePath = GetRelativePath(SourcePath, "blocks");
            String SourceIdentifier = "subpacks/" + JavaUnbound.SUBPACK + "/textures/blocks/" + RelativePath;
            String TargetPath = "textures/" + TargetName + ".png";

            Folder.SetIdentifier(TargetPath, SourceIdentifier);
            UsedSourceTextures.add("blocks/" + RelativePath);
            JavaUnbound.LOGGER.info("Loaded block mapping: " + MappingName + " -> " + SourceIdentifier + " -> " + TargetPath);
        }
    }

    private static Path FindBlockTexture(String MappingName) {
        final String SearchName = RemoveExtension(MappingName.substring("blocks/".length()));

        if (SearchName.isEmpty()) {
            return null;
        }

        try (var Stream = Files.walk(SubpackBlockTextureFolder)) {
            return Stream.filter(Files::isRegularFile).filter(ItemIconLoader::IsPng).filter(FilePath -> {
                        String FileName = FilePath.getFileName().toString();
                        String FileNameWithoutExtension = RemoveExtension(FileName);

                        return FileNameWithoutExtension.equalsIgnoreCase(SearchName);
                    }).sorted(Comparator.comparing(FilePath -> FilePath.toString().toLowerCase(Locale.ROOT))).findFirst().orElse(null);
        } catch (IOException E) {
            JavaUnbound.LOGGER.error("Failed to search block textures for: " + MappingName, E);

            return null;
        }
    }

    private static String ResolveTargetName(String MappingName, String SourceName, Set<String> JavaItemNames, Map<String, String> NormalizedJavaNames) {
        String Cached = ResolvedCache.get(MappingName);

        if (Cached != null) {
            return Cached;
        }

        String Manual = ManualMappings.get(MappingName);

        if (Manual != null) {
            ResolvedCache.put(MappingName, Manual);
            return Manual;
        }

        Manual = ManualMappings.get(SourceName);

        if (Manual != null) {
            ResolvedCache.put(MappingName, Manual);
            return Manual;
        }

        if (JavaItemNames.contains(SourceName)) {
            ResolvedCache.put(MappingName, SourceName);
            return SourceName;
        }

        String Generic = ApplyGenericTransformation(SourceName);

        if (Generic != null) {
            if (SourceName.startsWith("bundle_") && (SourceName.endsWith("_open_back") || SourceName.endsWith("_open_front"))) {
                ResolvedCache.put(MappingName, Generic);

                return Generic;
            }

            if (SourceName.startsWith("light_block_")) {
                ResolvedCache.put(MappingName, Generic);

                return Generic;
            }

            if (JavaItemNames.contains(Generic)) {
                ResolvedCache.put(MappingName, Generic);

                return Generic;
            }
        }

        String NormalizedExact = NormalizedJavaNames.get(Normalize(SourceName));

        if (NormalizedExact != null) {
            ResolvedCache.put(MappingName, NormalizedExact);

            return NormalizedExact;
        }

        String BestMatch = FindBestMatch(SourceName, JavaItemNames);

        if (BestMatch != null) {
            ResolvedCache.put(MappingName, BestMatch);

            return BestMatch;
        }

        return null;
    }

    private static String ApplyGenericTransformation(String Name) {
        if (Name.startsWith("spawn_egg_")) {
            String Entity = Name.substring(10);

            if (Entity.equals("tropicalfish")) {
                Entity = "tropical_fish";
            }

            return Entity + "_spawn_egg";
        }

        if (Name.startsWith("egg_")) {
            return null;
        }

        if (Name.startsWith("harness_")) {
            return Name.substring(8) + "_harness";
        }

        if (Name.startsWith("candle_")) {
            return Name.substring(7) + "_candle";
        }

        if (Name.startsWith("dye_powder_")) {
            return Name.substring(11) + "_dye";
        }

        if (Name.startsWith("boat_")) {
            return Name.substring(5) + "_boat";
        }

        if (Name.startsWith("door_")) {
            return Name.substring(5) + "_door";
        }

        if (Name.startsWith("sign_")) {
            return Name.substring(5) + "_sign";
        }

        if (Name.startsWith("bed_")) {
            return Name.substring(4) + "_bed";
        }

        if (Name.equals("minecart_normal")) {
            return "minecart";
        }

        if (Name.startsWith("minecart_")) {
            return Name.substring(9) + "_minecart";
        }

        if (Name.startsWith("record_")) {
            return "music_disc_" + Name.substring(7);
        }

        if (Name.startsWith("potion_bottle_")) {
            return Name;
        }

        if (Name.startsWith("bundle_")) {
            String Value = Name.substring(7);

            if (Value.endsWith("_open_back")) {
                return Name;
            }

            if (Value.endsWith("_open_front")) {
                return Name;
            }

            if (Value.endsWith("_open")) {
                return null;
            }

            return Value + "_bundle";
        }

        if (Name.startsWith("light_block_")) {
            String Number = Name.substring("light_block_".length());

            try {
                return "light_" + String.format("%02d", Integer.parseInt(Number));
            } catch (NumberFormatException Exception) {
                return null;
            }
        }

        return null;
    }

    private static String FindBestMatch(String SourceName, Set<String> JavaItemNames) {
        String NormalizedSource = Normalize(SourceName);
        String BestMatch = null;

        double BestScore = 0.0;
        double SecondBestScore = 0.0;

        for (String JavaName : JavaItemNames) {
            double Score = CalculateScore(SourceName, NormalizedSource, JavaName);

            if (Score > BestScore) {
                SecondBestScore = BestScore;
                BestScore = Score;
                BestMatch = JavaName;
            } else if (Score > SecondBestScore) {
                SecondBestScore = Score;
            }
        }

        if (BestMatch == null) {
            return null;
        }

        if (BestScore < MinimumScore) {
            return null;
        }

        if (BestScore - SecondBestScore < MinimumScoreDifference) {
            return null;
        }

        return BestMatch;
    }

    private static double CalculateScore(String SourceName, String NormalizedSource, String JavaName) {
        String NormalizedJava = Normalize(JavaName);
        Set<String> SourceTokens = Tokenize(SourceName);
        Set<String> JavaTokens = Tokenize(JavaName);

        double TokenScore = TokenSimilarity(SourceTokens, JavaTokens);
        double NormalizedScore = Similarity(NormalizedSource, NormalizedJava);
        double RawScore = Similarity(SourceName, JavaName);
        double PrefixScore = PrefixSimilarity(SourceName, JavaName);
        double SemanticScore = SemanticTokenSimilarity(SourceTokens, JavaTokens);

        return TokenScore * 0.40 + NormalizedScore * 0.25 + SemanticScore * 0.20 + RawScore * 0.10 + PrefixScore * 0.05;
    }

    private static double TokenSimilarity(Set<String> A, Set<String> B) {
        if (A.isEmpty() || B.isEmpty()) {
            return 0.0;
        }

        Set<String> Intersection = new HashSet<>(A);
        Intersection.retainAll(B);

        Set<String> Union = new HashSet<>(A);
        Union.addAll(B);

        return (double) Intersection.size() / Union.size();
    }

    private static double SemanticTokenSimilarity(Set<String> A, Set<String> B) {
        return TokenSimilarity(ExpandTokens(A), ExpandTokens(B));
    }

    private static Set<String> ExpandTokens(Set<String> Tokens) {
        Set<String> Result = new HashSet<>(Tokens);

        for (String Token : Tokens) {
            switch (Token) {
                case "egg" -> Result.add("spawn");
                case "spawn" -> Result.add("egg");
                case "gold" -> Result.add("golden");
                case "golden" -> Result.add("gold");
                case "wood" -> Result.add("wooden");
                case "wooden" -> Result.add("wood");
                case "raw" -> Result.add("uncooked");
                case "cooked" -> Result.add("cook");
                case "silver" -> Result.add("lightgray");
                case "totem" -> Result.add("undying");
                case "nautilus" -> Result.add("shell");
                case "fireworks" -> Result.add("firework");
            }
        }

        return Result;
    }

    private static Set<String> Tokenize(String Name) {
        String Value = Name.toLowerCase(Locale.ROOT).replace(".png", "");

        return new HashSet<>(Arrays.asList(Value.split("_")));
    }

    private static String Normalize(String Name) {
        String Value = Name.toLowerCase(Locale.ROOT).replace(".png", "");

        Value = Value.replace("spawn_egg", "egg");
        Value = Value.replace("golden", "gold");
        Value = Value.replace("wooden", "wood");
        Value = Value.replace("cooked_", "");
        Value = Value.replace("_cooked", "");
        Value = Value.replace("_raw", "");
        Value = Value.replace("silverfish", "silver_fish");
        Value = Value.replace("nautilus_armor", "nautilusarmor");

        return Value;
    }

    private static double Similarity(String A, String B) {
        if (A.equals(B)) {
            return 1.0;
        }

        if (A.isEmpty() || B.isEmpty()) {
            return 0.0;
        }

        int Distance = LevenshteinDistance(A, B);

        return 1.0 - ((double) Distance / Math.max(A.length(), B.length()));
    }

    private static double PrefixSimilarity(String A, String B) {
        int Length = Math.min(A.length(), B.length());

        if (Length == 0) {
            return 0.0;
        }

        int Same = 0;

        for (int I = 0; I < Length; I++) {
            if (A.charAt(I) != B.charAt(I)) {
                break;
            }

            Same++;
        }

        return (double) Same / Length;
    }

    private static int LevenshteinDistance(String A, String B) {
        int[] Previous = new int[B.length() + 1];
        int[] Current = new int[B.length() + 1];

        for (int J = 0; J <= B.length(); J++) {
            Previous[J] = J;
        }

        for (int I = 1; I <= A.length(); I++) {
            Current[0] = I;

            for (int J = 1; J <= B.length(); J++) {
                int Cost = A.charAt(I - 1) == B.charAt(J - 1) ? 0 : 1;
                Current[J] = Math.min(Math.min(Current[J - 1] + 1, Previous[J] + 1), Previous[J - 1] + Cost);
            }

            int[] Swap = Previous;
            Previous = Current;
            Current = Swap;
        }

        return Previous[B.length()];
    }

    private static Set<String> GetMinecraftItemNames() {
        Set<String> Names = new HashSet<>();

        BuiltInRegistries.ITEM.keySet().forEach(Key -> {
            String Value = Key.toString();

            if (Value.startsWith("minecraft:")) {
                Names.add(Value.substring("minecraft:".length()));
            }
        });

        return Names;
    }

    private static Map<String, String> BuildNormalizedNames(Set<String> Names) {
        Map<String, String> Result = new HashMap<>();

        for (String Name : Names) {
            Result.putIfAbsent(Normalize(Name), Name);
        }

        return Result;
    }

    private static boolean ShouldIgnore(String Name) {
        return Name.startsWith("icon_") || Name.startsWith("atlas_") || Name.startsWith("gui_");
    }

    private static boolean IsPng(Path Path) {
        return Path.toString().toLowerCase(Locale.ROOT).endsWith(".png");
    }

    private static String RemoveExtension(String Name) {
        return Name.toLowerCase(Locale.ROOT).endsWith(".png") ? Name.substring(0, Name.length() - 4) : Name;
    }

    private static String GetRelativePath(Path Path, String TextureFolder) {
        Path Root = TextureFolder.equals("blocks") ? SubpackBlockTextureFolder : SubpackItemTextureFolder;

        return Root.relativize(Path).toString().replace(File.separator, "/");
    }

    private static void ResetCounters() {
        TotalTextures = 0;
        UsedTextures = 0;
        UnusedTextures = 0;

        ResolvedCache.clear();
        UsedSourceTextures.clear();
        UnusedSourceTextures.clear();
        TargetCollisions.clear();
    }

    private static void SaveReport() {
        try {
            Files.createDirectories(ReportFolder);

            List<String> Report = new ArrayList<>();

            Report.add("Java Unbound Item Texture Report");
            Report.add("================================");
            Report.add("Subpack: " + JavaUnbound.SUBPACK);
            Report.add("");
            Report.add("Total textures: " + TotalTextures);
            Report.add("Used textures: " + UsedTextures);
            Report.add("Unused textures: " + UnusedTextures);
            Report.add("Usage: " + (TotalTextures == 0 ? 0.0 : (double) UsedTextures / TotalTextures * 100.0) + "%");
            Report.add("");
            Report.add("Unused textures:");
            Report.add("");

            if (UnusedSourceTextures.isEmpty()) {
                Report.add("None");
            } else {
                Report.addAll(UnusedSourceTextures);
            }

            Report.add("");
            Report.add("Target collisions:");
            Report.add("");

            boolean HasCollision = false;

            for (Map.Entry<String, List<String>> Entry : TargetCollisions.entrySet()) {
                if (Entry.getValue().size() <= 1) {
                    continue;
                }

                HasCollision = true;
                Report.add(Entry.getKey());

                for (String Source : Entry.getValue()) {
                    Report.add("  <- " + Source);
                }

                Report.add("");
            }

            if (!HasCollision) {
                Report.add("None");
            }

            Files.write(UnusedTexturesFile, UnusedSourceTextures, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
            Files.write(ReportFile, Report, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
        } catch (IOException E) {
            E.printStackTrace();
        }
    }
}