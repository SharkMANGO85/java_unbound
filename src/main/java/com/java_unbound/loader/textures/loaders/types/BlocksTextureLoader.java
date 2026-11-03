package com.java_unbound.loader.textures.loaders.types;

import static com.java_unbound.loader.textures.loaders.TextureLoader.ManualMappings;

public class BlocksTextureLoader {
    private static final String[] BedrockColors = {"black", "blue", "brown", "cyan", "gray", "green", "light_blue", "lime", "magenta", "orange", "pink", "purple", "red", "silver", "white", "yellow"};
    private static final String[] JavaColors = {"black", "blue", "brown", "cyan", "gray", "green", "light_blue", "lime", "magenta", "orange", "pink", "purple", "red", "light_gray", "white", "yellow"};
    private static final String[] CopperPrefixes = {"", "exposed_", "weathered_", "oxidized_"};

    private static void Map(String Bedrock, String Java) {
        ManualMappings.put("blocks/" + Bedrock, "block/" + Java);
    }

    private static void Same(String... Names) {
        for (String Name : Names) Map(Name, Name);
    }

    private static void SameIn(String Folder, String... Names) {
        for (String Name : Names) Map(Folder + "/" + Name, Name);
    }

    private static void Logs(String BedrockSide, String BedrockTop, String Java) {
        Map(BedrockSide, Java + "_log");
        Map(BedrockTop, Java + "_log_top");
    }

    private static void Stripped(String BedrockSide, String Java) {
        Map(BedrockSide, "stripped_" + Java + "_log");
        Map("stripped_" + Java + "_log_top", "stripped_" + Java + "_log_top");
    }

    private static void Colored(String BedrockPattern, String JavaPattern) {
        for (int I = 0; I < BedrockColors.length; I++) {
            Map(BedrockPattern.formatted(BedrockColors[I]), JavaPattern.formatted(JavaColors[I]));
        }
    }

    private static void Stages(String BedrockName, String JavaName, int Count) {
        for (int I = 0; I < Count; I++) {
            Map(BedrockName + "_stage_" + I, JavaName + "_stage" + I);
        }
    }

    public static void LoadBlocksTextures() {
        LoadTerrain();
        LoadWood();
        LoadColored();
        LoadCopper();
        LoadPlants();
        LoadPlacedPlants();
        LoadFunctional();
        LoadUnverified();

        ManualMappings.put("blocks/sandstone_normal", "block/sandstone");
    }

    private static void LoadTerrain() {
        Same("red_sand", "sand", "dirt", "stone", "cobblestone", "gravel", "clay", "leaf_litter", "deepslate", "bedrock", "lava_flow", "lava_still");

        Map("grass_top", "grass_block_top");
        Map("grass_side_carried", "grass_block_side");
        Map("grass_side", "grass_block_side_overlay");
        Map("grass_path_top", "dirt_path_top");
        Map("grass_path_side", "dirt_path_side");
        Map("grass_block_snow", "grass_block_snow");

        Map("dirt_podzol_side", "podzol_side");
        Map("dirt_podzol_top", "podzol_top");
        Map("dirt_with_roots", "rooted_dirt");

        Same("coal_ore", "copper_ore", "iron_ore", "gold_ore", "nether_gold_ore", "diamond_ore", "emerald_ore", "redstone_ore", "lapis_ore");
        Map("quartz_ore", "nether_quartz_ore");

        for (String Ore : new String[]{"coal", "copper", "iron", "gold", "diamond", "emerald", "redstone", "lapis"}) {
            Map("deepslate/deepslate_" + Ore + "_ore", "deepslate_" + Ore + "_ore");
        }

        SameIn("deepslate", "chiseled_deepslate", "cobbled_deepslate", "cracked_deepslate_bricks", "cracked_deepslate_tiles", "deepslate_bricks", "deepslate_tiles", "polished_deepslate", "deepslate_top");

        Map("stone_andesite", "andesite");
        Map("stone_granite", "granite");
        Map("stone_diorite", "diorite");
        Map("stone_andesite_smooth", "polished_andesite");
        Map("stone_granite_smooth", "polished_granite");
        Map("stone_diorite_smooth", "polished_diorite");

        Map("stonebrick", "stone_bricks");
        Map("stonebrick_carved", "chiseled_stone_bricks");
        Map("stonebrick_cracked", "cracked_stone_bricks");
        Map("stonebrick_mossy", "mossy_stone_bricks");
        Map("cobblestone_mossy", "mossy_cobblestone");
        Map("stone_slab_side", "smooth_stone_slab_side");
        Map("stone_slab_top", "smooth_stone");

        Same("bamboo_block", "bamboo_block_top", "bamboo_mosaic", "beacon", "bookshelf", "coarse_dirt", "hay_block_side", "hay_block_top", "nether_sprouts", "sea_pickle", "mangrove_roots_side", "mangrove_roots_top", "muddy_mangrove_roots_side", "muddy_mangrove_roots_top", "pale_hanging_moss_tip");
        Same("amethyst_block", "budding_amethyst", "calcite", "dripstone_block", "tuff", "tuff_bricks", "polished_tuff", "chiseled_tuff", "chiseled_tuff_top", "chiseled_tuff_bricks", "chiseled_tuff_bricks_top", "ancient_debris_side", "ancient_debris_top", "basalt_side", "basalt_top", "smooth_basalt", "polished_basalt_side", "polished_basalt_top", "blackstone", "blackstone_top", "gilded_blackstone", "polished_blackstone", "polished_blackstone_bricks", "cracked_polished_blackstone_bricks", "chiseled_polished_blackstone", "chiseled_nether_bricks", "cracked_nether_bricks", "netherrack", "soul_sand", "soul_soil", "magma", "glowstone", "shroomlight", "obsidian", "crying_obsidian", "end_stone", "purpur_block", "purpur_pillar", "purpur_pillar_top", "mud", "packed_mud", "mud_bricks", "moss_block", "pale_moss_block", "mycelium_side", "mycelium_top", "nether_wart_block", "warped_wart_block", "crimson_nylium_side", "crimson_nylium_top", "warped_nylium_side", "warped_nylium_top", "ochre_froglight_side", "ochre_froglight_top", "pearlescent_froglight_side", "pearlescent_froglight_top", "verdant_froglight_side", "verdant_froglight_top", "reinforced_deepslate_bottom", "reinforced_deepslate_side", "reinforced_deepslate_top", "resin_block", "resin_bricks", "resin_clump", "chiseled_resin_bricks", "powder_snow", "heavy_core", "coal_block", "diamond_block", "emerald_block", "gold_block", "iron_block", "lapis_block", "netherite_block", "redstone_block", "raw_copper_block", "raw_gold_block", "raw_iron_block", "sea_lantern", "tinted_glass", "bone_block_side", "bone_block_top", "target_side", "target_top", "lodestone_side", "lodestone_top", "ice", "blue_ice", "quartz_block_bottom", "quartz_block_side", "quartz_block_top", "quartz_bricks");

        Map("ice_packed", "packed_ice");
        Map("end_bricks", "end_stone_bricks");
        Map("brick", "bricks");
        Map("nether_brick", "nether_bricks");
        Map("red_nether_brick", "red_nether_bricks");
        Map("prismarine_bricks", "prismarine_bricks");
        Map("prismarine_dark", "dark_prismarine");
        Map("prismarine_rough", "prismarine");
        Map("slime", "slime_block");
        Map("snow", "snow");
        Map("sponge_wet", "wet_sponge");
        Same("sponge");
        Map("honey_bottom", "honey_block_bottom");
        Map("honey_side", "honey_block_side");
        Map("honey_top", "honey_block_top");
        Map("honeycomb", "honeycomb_block");
        Map("portal", "nether_portal");
        Map("water_still_grey", "water_still");
        Map("water_flow_grey", "water_flow");

        Map("quartz_block_chiseled", "chiseled_quartz_block");
        Map("quartz_block_chiseled_top", "chiseled_quartz_block_top");
        Map("quartz_block_lines", "quartz_pillar");
        Map("quartz_block_lines_top", "quartz_pillar_top");

        for (String Sand : new String[]{"sandstone", "red_sandstone"}) {
            Map(Sand + "_bottom", Sand + "_bottom");
            Map(Sand + "_top", Sand + "_top");
            Map(Sand + "_carved", "chiseled_" + Sand);
            Map(Sand + "_smooth", "cut_" + Sand);
        }

        for (int I = 0; I < 4; I++) {
            Map("suspicious_gravel_" + I, "suspicious_gravel_" + I);
            Map("suspicious_sand_" + I, "suspicious_sand_" + I);
            Map("frosted_ice_" + I, "frosted_ice_" + I);
        }

        for (String Direction : new String[]{"up", "down"}) {
            Same("pointed_dripstone_" + Direction + "_base", "pointed_dripstone_" + Direction + "_frustum", "pointed_dripstone_" + Direction + "_middle", "pointed_dripstone_" + Direction + "_tip");
            Map("pointed_dripstone_" + Direction + "_merge", "pointed_dripstone_" + Direction + "_tip_merge");
        }

        Same("sculk", "sculk_catalyst_bottom", "sculk_catalyst_side", "sculk_catalyst_side_bloom", "sculk_catalyst_top", "sculk_catalyst_top_bloom", "sculk_sensor_bottom", "sculk_sensor_side", "sculk_sensor_tendril_active", "sculk_sensor_tendril_inactive", "sculk_sensor_top", "sculk_shrieker_bottom", "sculk_shrieker_inner_top", "sculk_shrieker_side", "sculk_shrieker_top", "calibrated_sculk_sensor_amethyst", "calibrated_sculk_sensor_input_side", "calibrated_sculk_sensor_top");
    }

    private static void LoadWood() {
        String[] Vanilla = {"acacia", "birch", "jungle", "oak", "spruce"};

        for (String Wood : Vanilla) {
            Map("leaves_" + Wood, Wood + "_leaves");
        }

        Map("leaves_big_oak_carried", "dark_oak_leaves");
        Same("azalea_leaves", "cherry_leaves", "mangrove_leaves", "pale_oak_leaves");
        Map("azalea_leaves_flowers", "flowering_azalea_leaves");

        for (String Wood : Vanilla) {
            Logs("log_" + Wood, "log_" + Wood + "_top", Wood);
        }

        Logs("log_big_oak", "log_big_oak_top", "dark_oak");

        for (String Wood : new String[]{"cherry", "mangrove", "pale_oak"}) {
            Logs(Wood + "_log_side", Wood + "_log_top", Wood);
        }

        for (String Wood : Vanilla) {
            Map("planks_" + Wood, Wood + "_planks");
        }

        Map("planks_big_oak", "dark_oak_planks");
        Same("mangrove_planks", "cherry_planks", "pale_oak_planks", "bamboo_planks", "crimson_planks", "warped_planks");

        for (String Wood : new String[]{"oak", "spruce", "birch", "jungle", "acacia", "dark_oak"}) {
            Stripped("stripped_" + Wood + "_log", Wood);
        }

        for (String Wood : new String[]{"pale_oak", "mangrove", "cherry"}) {
            Stripped("stripped_" + Wood + "_log_side", Wood);
        }

        Same("stripped_bamboo_block_top", "stripped_bamboo_block");

        Map("huge_fungus/crimson_log_side", "crimson_stem");
        Map("huge_fungus/crimson_log_top", "crimson_stem_top");
        Map("huge_fungus/warped_stem_side", "warped_stem");
        Map("huge_fungus/warped_stem_top", "warped_stem_top");
        Map("huge_fungus/strd_crimson_stem_side", "stripped_crimson_stem");
        Map("huge_fungus/stred_crimson_stem_top", "stripped_crimson_stem_top");
        Map("huge_fungus/stred_warped_stem_side", "stripped_warped_stem");
        Map("huge_fungus/strped_warped_stem_top", "stripped_warped_stem_top");

        for (String Wood : new String[]{"acacia", "birch", "dark_oak", "jungle", "spruce"}) {
            Map("door_" + Wood + "_lower", Wood + "_door_bottom");
            Map("door_" + Wood + "_upper", Wood + "_door_top");
        }

        Map("door_wood_lower", "oak_door_bottom");
        Map("door_wood_upper", "oak_door_top");
        Map("door_iron_lower", "iron_door_bottom");
        Map("door_iron_upper", "iron_door_top");
        Map("warped_door_lower", "warped_door_bottom");
        Map("warped_door_top", "warped_door_top");
        Map("huge_fungus/crimson_door_lower", "crimson_door_bottom");
        Map("huge_fungus/crimson_door_top", "crimson_door_top");
        Same("bamboo_door_bottom", "bamboo_door_top", "cherry_door_bottom", "cherry_door_top", "mangrove_door_bottom", "mangrove_door_top", "pale_oak_door_bottom", "pale_oak_door_top");

        for (String Wood : new String[]{"acacia", "bamboo", "birch", "cherry", "dark_oak", "jungle", "mangrove", "pale_oak", "spruce"}) {
            Same(Wood + "_trapdoor");
        }

        Map("trapdoor", "oak_trapdoor");
        Same("iron_trapdoor");
        Map("huge_fungus/crimson_trapdoor", "crimson_trapdoor");
        Map("huge_fungus/warped_trapdoor", "warped_trapdoor");

        for (String Wood : new String[]{"acacia", "bamboo", "birch", "cherry", "crimson", "dark_oak", "jungle", "mangrove", "oak", "pale_oak", "spruce", "warped"}) {
            Same(Wood + "_shelf");
        }
    }

    private static void LoadColored() {
        Colored("concrete_%s", "%s_concrete");
        Colored("concrete_powder_%s", "%s_concrete_powder");
        Colored("glazed_terracotta_%s", "%s_glazed_terracotta");
        Colored("hardened_clay_stained_%s", "%s_terracotta");
        Colored("wool_colored_%s", "%s_wool");
        Colored("glass_%s", "%s_stained_glass");
        Colored("glass_pane_top_%s", "%s_stained_glass_pane_top");

        Map("hardened_clay", "terracotta");
        Same("glass", "glass_pane_top");

        SameIn("candles", "candle", "candle_lit");

        for (String Color : JavaColors) {
            SameIn("candles", Color + "_candle", Color + "_candle_lit");
        }

        String[][] Corals = {{"blue", "tube"}, {"pink", "brain"}, {"purple", "bubble"}, {"red", "fire"}, {"yellow", "horn"}};

        for (String[] Coral : Corals) {
            Map("coral_" + Coral[0], Coral[1] + "_coral_block");
            Map("coral_" + Coral[0] + "_dead", "dead_" + Coral[1] + "_coral_block");
        }
    }

    private static void LoadCopper() {
        for (String Prefix : CopperPrefixes) {
            Same(Prefix + "chiseled_copper", Prefix + "cut_copper", Prefix + "copper_trapdoor", Prefix + "copper_door_bottom", Prefix + "copper_door_top", Prefix + "copper_grate", Prefix + "copper_lantern", Prefix + "copper_bulb", Prefix + "copper_bulb_lit", Prefix + "copper_bulb_lit_powered", Prefix + "copper_bulb_powered");
            Map(Prefix + "copper_chain1", Prefix + "copper_chain");
        }

        Map("copper_block", "copper_block");
        Same("exposed_copper", "weathered_copper", "oxidized_copper");
        Same("exposed_lightning_rod", "weathered_lightning_rod", "oxidized_lightning_rod");
        Same("copper_torch");
        Map("chain1", "iron_chain");
    }

    private static void LoadPlants() {
        Stages("beetroots", "beetroots", 4);
        Stages("carrots", "carrots", 4);
        Stages("potatoes", "potatoes", 4);
        Stages("wheat", "wheat", 8);
        Stages("cocoa", "cocoa", 3);
        Stages("nether_wart", "nether_wart", 3);
        Stages("torchflower_crop", "torchflower_crop", 2);
        Same("sweet_berry_bush_stage0", "sweet_berry_bush_stage1", "sweet_berry_bush_stage2", "sweet_berry_bush_stage3", "pitcher_crop_bottom", "pitcher_crop_side", "pitcher_crop_top", "pitcher_crop_bottom_stage_1", "pitcher_crop_bottom_stage_2", "pitcher_crop_bottom_stage_3", "pitcher_crop_bottom_stage_4", "pitcher_crop_top_stage_3", "pitcher_crop_top_stage_4");

        Map("melon_stem_connected", "attached_melon_stem");
        Map("melon_stem_disconnected", "melon_stem");
        Map("pumpkin_stem_connected", "attached_pumpkin_stem");
        Map("pumpkin_stem_disconnected", "pumpkin_stem");
        Same("melon_side", "melon_top", "pumpkin_side", "pumpkin_top");
        Map("pumpkin_face_off", "carved_pumpkin");
        Map("pumpkin_face_on", "jack_o_lantern");

        Map("double_plant_grass_bottom", "tall_grass_bottom");
        Map("double_plant_grass_top", "tall_grass_top");
        Map("double_plant_fern_bottom", "large_fern_bottom");
        Map("double_plant_rose_bottom", "rose_bush_bottom");
        Map("double_plant_rose_top", "rose_bush_top");
        Map("double_plant_syringa_bottom", "lilac_bottom");
        Map("double_plant_syringa_top", "lilac_top");
        Map("double_plant_paeonia_bottom", "peony_bottom");
        Map("double_plant_paeonia_top", "peony_top");
        Map("double_plant_sunflower_back", "sunflower_back");
        Map("double_plant_sunflower_front", "sunflower_front");
        Map("double_plant_sunflower_bottom", "sunflower_bottom");
        Map("double_plant_sunflower_top", "sunflower_top");
        Map("tallgrass", "short_grass");
        Map("seagrass", "seagrass");
        Map("seagrass_doubletall_bottom_a", "tall_seagrass_bottom");
        Map("seagrass_doubletall_top_a", "tall_seagrass_top");

        Map("cave_vines_head", "cave_vines");
        Map("cave_vines_head_berries", "cave_vines_lit");
        Map("cave_vines_body", "cave_vines_plant");
        Map("cave_vines_body_berries", "cave_vines_plant_lit");
        Map("twisting_vines_bottom", "twisting_vines_plant");
        Map("weeping_vines_plant", "weeping_vines_plant");
        Map("vine", "vine");
        Map("kelp_top", "kelp");
        Map("kelp_a", "kelp_plant");
        Map("dried_kelp_side_a", "dried_kelp_side");
        Same("dried_kelp_top");
        Map("bamboo_sapling", "bamboo_stage0");
        Map("bamboo_leaf", "bamboo_large_leaves");
        Map("bamboo_small_leaf", "bamboo_small_leaves");
        Map("bamboo_stem", "bamboo_stalk");
        Same("bamboo_singleleaf", "bush", "firefly_bush", "pink_petals", "pink_petals_stem", "wildflowers", "wildflowers_stem");
        Map("firefly_bush_firefly", "firefly_bush_emissive");
        Map("waterlily", "lily_pad");
        Map("reeds", "sugar_cane");

        Map("crimson_roots_pot", "potted_crimson_roots");
        Map("warped_roots_pot", "potted_warped_roots");
        Map("eyeblossom_eyes_blooming", "open_eyeblossom_emissive");
        Same("azalea_plant", "azalea_side", "azalea_top", "flowering_azalea_side", "flowering_azalea_top", "potted_azalea_bush_plant", "potted_azalea_bush_side", "potted_azalea_bush_top", "potted_flowering_azalea_bush_plant", "potted_flowering_azalea_bush_side", "potted_flowering_azalea_bush_top");
        Same("flower_pot", "chorus_flower", "chorus_flower_dead", "chorus_plant", "mangrove_propagule", "mangrove_propagule_hanging", "spore_blossom_base");
        Map("mushroom_block_skin_brown", "brown_mushroom_block");
        Map("mushroom_block_skin_red", "red_mushroom_block");
        Map("mushroom_block_skin_stem", "mushroom_stem");
        Same("mushroom_block_inside");

        Same("small_dripleaf_side", "small_dripleaf_stem_bottom", "small_dripleaf_stem_top", "small_dripleaf_top", "big_dripleaf_stem", "big_dripleaf_top");
        Map("big_dripleaf_side1", "big_dripleaf_side");
        Same("cactus_bottom", "cactus_side", "cactus_top");
    }

    /**
     * Textures of PLACED blocks, taken from the normal (non-carried) Bedrock files in textures/blocks.
     * The files in textures/blocks/carried are only used for the item/UI icon (see TextureLoader.ProcessFolderMappings).
     */
    private static void LoadPlacedPlants() {
        Map("flower_dandelion", "dandelion");
        Map("flower_rose", "poppy");
        Map("flower_allium", "allium");
        Map("flower_blue_orchid", "blue_orchid");
        Map("flower_houstonia", "azure_bluet");
        Map("flower_cornflower", "cornflower");
        Map("flower_lily_of_the_valley", "lily_of_the_valley");
        Map("flower_oxeye_daisy", "oxeye_daisy");
        Map("flower_wither_rose", "wither_rose");
        Map("flower_tulip_orange", "orange_tulip");
        Map("flower_tulip_pink", "pink_tulip");
        Map("flower_tulip_red", "red_tulip");
        Map("flower_tulip_white", "white_tulip");
        Same("golden_dandelion", "torchflower", "cactus_flower");

        Map("sapling_oak", "oak_sapling");
        Map("sapling_spruce", "spruce_sapling");
        Map("sapling_birch", "birch_sapling");
        Map("sapling_jungle", "jungle_sapling");
        Map("sapling_acacia", "acacia_sapling");
        Map("sapling_roofed_oak", "dark_oak_sapling");
        Same("cherry_sapling", "pale_oak_sapling", "poplar_sapling");

        Map("torch_on", "torch");
        Map("redstone_torch_on", "redstone_torch");
        Same("soul_torch");

        Map("rail_normal", "rail");
        Map("rail_golden", "powered_rail");
        Map("rail_detector", "detector_rail");
        Map("rail_activator", "activator_rail");

        Same("ladder", "lever", "iron_bars", "copper_bars", "exposed_copper_bars", "weathered_copper_bars", "oxidized_copper_bars", "glow_lichen", "hanging_roots", "frogspawn", "amethyst_cluster", "large_amethyst_bud", "medium_amethyst_bud", "small_amethyst_bud", "sculk_vein", "short_dry_grass", "tall_dry_grass", "spore_blossom", "fern", "crimson_roots", "warped_roots", "weeping_vines");

        Map("copper_bars", "waxed_copper_bars");
        Map("exposed_copper_bars", "waxed_exposed_copper_bars");
        Map("weathered_copper_bars", "waxed_weathered_copper_bars");
        Map("oxidized_copper_bars", "waxed_oxidized_copper_bars");

        Same("crimson_fungus", "warped_fungus", "lightning_rod");
        Map("twisting_vines_base", "twisting_vines");

        Map("web", "cobweb");
        Map("deadbush", "dead_bush");
        Map("mushroom_brown", "brown_mushroom");
        Map("mushroom_red", "red_mushroom");
        Map("eyeblossom_blooming", "open_eyeblossom");
        Map("eyeblossom_dormant", "closed_eyeblossom");

        String[][] Corals = {{"blue", "tube"}, {"pink", "brain"}, {"purple", "bubble"}, {"red", "fire"}, {"yellow", "horn"}};

        for (String[] Coral : Corals) {
            Map("coral_plant_" + Coral[0], Coral[1] + "_coral");
            Map("coral_fan_" + Coral[0], Coral[1] + "_coral_fan");
            Map("coral_plant_" + Coral[0] + "_dead", "dead_" + Coral[1] + "_coral");
            Map("coral_fan_" + Coral[0] + "_dead", "dead_" + Coral[1] + "_coral_fan");
        }
    }

    private static void LoadFunctional() {
        Map("redstone_lamp_off", "redstone_lamp");
        Same("redstone_lamp_on");
        Map("redstone_torch_off", "redstone_torch_off");
        Map("repeater_off", "repeater");
        Same("repeater_on");
        Map("comparator_off", "comparator");
        Same("comparator_on");
        Map("redstone_dust_cross", "redstone_dust_dot");
        Map("redstone_dust_line", "redstone_dust_line0");
        Map("rail_activator_powered", "activator_rail_on");
        Map("rail_detector_powered", "detector_rail_on");
        Map("rail_golden_powered", "powered_rail_on");
        Map("rail_normal_turned", "rail_corner");
        Map("noteblock", "note_block");
        Map("trip_wire", "tripwire");
        Map("trip_wire_source", "tripwire_hook");
        Map("observer_back_lit", "observer_back_on");
        Same("observer_back", "observer_front", "observer_side", "observer_top");
        Map("piston_top_sticky", "piston_top_sticky");
        Same("piston_bottom", "piston_inner", "piston_side");
        Map("dispenser_front_horizontal", "dispenser_front");
        Same("dispenser_front_vertical");
        Map("dropper_front_horizontal", "dropper_front");
        Same("dropper_front_vertical", "hopper_inside", "hopper_outside", "hopper_top");
        Map("daylight_detector_inverted_top", "daylight_detector_inverted_top");
        Same("daylight_detector_side", "daylight_detector_top");
        Map("mob_spawner", "spawner");

        for (String Side : new String[]{"bottom", "east", "east_crafting", "east_triggered", "north", "north_crafting", "south", "south_triggered", "top", "top_crafting", "top_triggered", "west", "west_crafting", "west_triggered"}) {
            Same("crafter_" + Side);
        }
        for (String Command : new String[]{"command_block", "chain_command_block", "repeating_command_block"}) {
            Same(Command + "_back", Command + "_front", Command + "_side");

            if (!Command.startsWith("repeating")) Same(Command + "_conditional");
        }
        Same("jigsaw_back", "jigsaw_front", "jigsaw_lock", "jigsaw_side", "structure_block", "structure_block_corner", "structure_block_data", "structure_block_load", "structure_block_save");

        Map("blast_furnace_front_off", "blast_furnace_front");
        Same("blast_furnace_front_on", "blast_furnace_side", "blast_furnace_top");
        Map("smoker_front_off", "smoker_front");
        Same("smoker_bottom", "smoker_front_on", "smoker_side", "smoker_top");
        Map("furnace_front_off", "furnace_front");
        Same("furnace_front_on", "furnace_side", "furnace_top");
        Same("crafting_table_front", "crafting_table_side", "crafting_table_top");
        Same("cartography_table_side1", "cartography_table_side2", "cartography_table_side3", "cartography_table_top");
        Map("fletcher_table_side1", "fletching_table_front");
        Map("fletcher_table_side2", "fletching_table_side");
        Map("fletcher_table_top", "fletching_table_top");
        Same("smithing_table_bottom", "smithing_table_front", "smithing_table_side", "smithing_table_top", "loom_bottom", "loom_front", "loom_side", "loom_top", "lectern_base", "lectern_front", "lectern_sides", "lectern_top", "grindstone_pivot", "grindstone_round", "grindstone_side", "enchanting_table_bottom", "enchanting_table_side", "enchanting_table_top", "barrel_bottom", "barrel_side", "barrel_top", "barrel_top_open");
        Same("stonecutter_bottom", "stonecutter_side", "stonecutter_top");
        Map("stonecutter2_saw", "stonecutter_saw");
        Same("jukebox_side", "jukebox_top", "brewing_stand", "brewing_stand_base");
        Map("anvil_base", "anvil");
        Map("anvil_top_damaged_0", "anvil_top");
        Map("anvil_top_damaged_1", "chipped_anvil_top");
        Map("anvil_top_damaged_2", "damaged_anvil_top");
        Same("cauldron_bottom", "cauldron_inner", "cauldron_side", "cauldron_top");
        Map("composter_bottom", "composter_bottom");
        Same("composter_side", "composter_top");
        Map("compost", "composter_compost");
        Map("compost_ready", "composter_ready");
        Same("cake_bottom", "cake_inner", "cake_side", "cake_top");
        Same("chiseled_bookshelf_empty", "chiseled_bookshelf_occupied", "chiseled_bookshelf_side", "chiseled_bookshelf_top");
        Same("tnt_bottom", "tnt_side", "tnt_top", "scaffolding_bottom", "scaffolding_side", "scaffolding_top");
        Same("lantern", "soul_lantern");
        Same("campfire_log", "campfire_log_lit", "soul_campfire_log_lit");
        Map("campfire", "campfire_fire");
        Map("soul_campfire", "soul_campfire_fire");
        // Same("fire_0", "soul_fire_0", "fire_1", "soul_fire_1");

        Same("fire_0", "soul_fire_0");

        Map("farmland_dry", "farmland");
        Map("farmland_wet", "farmland_moist");
        Map("endframe_eye", "end_portal_frame_eye");
        Map("endframe_side", "end_portal_frame_side");
        Map("endframe_top", "end_portal_frame_top");
        Same("dragon_egg", "bee_nest_bottom", "bee_nest_front", "bee_nest_front_honey", "bee_nest_side", "bee_nest_top", "beehive_bottom", "beehive_front", "beehive_front_honey", "beehive_side", "beehive_top");
        Map("turtle_egg_not_cracked", "turtle_egg");
        Map("turtle_egg_slightly_cracked", "turtle_egg_slightly_cracked");
        Map("turtle_egg_very_cracked", "turtle_egg_very_cracked");

        for (String State : new String[]{"not_cracked", "slightly_cracked", "very_cracked"}) {
            for (String Side : new String[]{"bottom", "east", "north", "south", "top", "west"}) {
                if (State.equals("slightly_cracked") && Side.equals("bottom")) continue;

                Same("sniffer_egg_" + State + "_" + Side);
            }
        }
        for (int I = 0; I < 4; I++) {
            for (String Side : new String[]{"back", "bottom", "front", "left", "right", "tentacles", "top"}) {
                Map("dried_ghast_state_" + (I + 1) + "_" + Side, "dried_ghast_hydration_" + I + "_" + Side);
            }
        }

        Map("creaking_heart_side_inactive", "creaking_heart");
        Map("creaking_heart_side_active", "creaking_heart_awake");
        Map("creaking_heart_side_dormant", "creaking_heart_dormant");
        Map("creaking_heart_top_inactive", "creaking_heart_top");
        Map("creaking_heart_top_active", "creaking_heart_top_awake");
        Map("creaking_heart_top_dormant", "creaking_heart_top_dormant");

        Same("trial_spawner_bottom", "trial_spawner_side_active", "trial_spawner_side_active_ominous", "trial_spawner_side_inactive", "trial_spawner_top_active", "trial_spawner_top_active_ominous", "trial_spawner_top_ejecting_reward", "trial_spawner_top_inactive", "trial_spawner_top_inactive_ominous");

        for (String Part : new String[]{"bottom", "front_ejecting", "front_off", "front_on", "side_off", "side_on", "top", "top_ejecting"}) {
            Same("vault_" + Part, "vault_" + Part + "_ominous");
        }

        Map("repating_command_block_conditional", "repeating_command_block_conditional");
        Map("snifer_egg_slightly_cracked_bottom", "sniffer_egg_slightly_cracked_bottom");
        Map("scuk_shrieker_can_summon_inner_top", "sculk_shrieker_can_summon_inner_top");
        Map("tril_spawner_side_inactive_ominous", "trial_spawner_side_inactive_ominous");
        Map("triner_top_ejecting_reward_ominous", "trial_spawner_top_ejecting_reward_ominous");

        Same("respawn_anchor_bottom", "respawn_anchor_top", "respawn_anchor_top_off");

        for (int I = 0; I < 5; I++) {
            Same("respawn_anchor_side" + I);
        }
    }

    private static void LoadUnverified() {
        Same("cinnabar", "cinnabar_bricks", "chiseled_cinnabar", "polished_cinnabar", "sulfur", "sulfur_bricks", "chiseled_sulfur", "polished_sulfur", "potent_sulfur");
        Same("poplar_planks", "poplar_sapling", "poplar_door_bottom", "poplar_door_top", "poplar_trapdoor", "poplar_shelf", "orange_poplar_leaves", "red_poplar_leaves", "yellow_poplar_leaves");
        Logs("poplar_log_side", "poplar_log_top", "poplar");
        Stripped("stripped_poplar_log_side", "poplar");
        Same("red_shrub");
    }
}