package com.java_unbound.loader.resourcepack;

public final class ResourceMappings {
    private ResourceMappings() {
    }

    public static void Register() {
        RegisterUI();
        RegisterItems();
        RegisterBlocks();
    }

    private static void RegisterUI() {
        Folder.SetIdentifier("textures/gui/title/background/panorama_0.png", "subpacks/SP2/textures/ui/panorama_0.png");
        Folder.SetIdentifier("textures/gui/title/background/panorama_1.png", "subpacks/SP2/textures/ui/panorama_1.png");
        Folder.SetIdentifier("textures/gui/title/background/panorama_2.png", "subpacks/SP2/textures/ui/panorama_2.png");
        Folder.SetIdentifier("textures/gui/title/background/panorama_3.png", "subpacks/SP2/textures/ui/panorama_3.png");
        Folder.SetIdentifier("textures/gui/title/background/panorama_4.png", "subpacks/SP2/textures/ui/panorama_4.png");
        Folder.SetIdentifier("textures/gui/title/background/panorama_5.png", "subpacks/SP2/textures/ui/panorama_5.png");
        Folder.SetIdentifier("textures/gui/title/minecraft.png", "subpacks/SP2/textures/ui/title.png");


        Folder.SetIdentifier("textures/misc/pumpkinblur.png", "subpacks/SP2/textures/misc/pumpkinblur.png");
        Folder.SetIdentifier("textures/misc/missing_texture.png", "subpacks/SP2/textures/misc/missing_texture.png");
    }

    private static void RegisterItems() {
        Folder.SetIdentifier("textures/item/totem_of_undying.png", "subpacks/SP2/textures/items/totem.png");
        Folder.SetIdentifier("textures/item/painting.png", "subpacks/SP2/textures/items/painting.png");
        Folder.SetIdentifier("textures/item/blue_egg.png", "subpacks/SP2/textures/items/blue_egg.png");

        Folder.SetIdentifier("textures/item/cow_spawn_egg.png", "subpacks/SP2/textures/items/spawn_eggs/spawn_egg_cow.png");
        Folder.SetIdentifier("textures/item/allay_spawn_egg.png", "subpacks/SP2/textures/items/spawn_eggs/spawn_egg_allay.png");
    }

    private static void RegisterBlocks() {
        Folder.SetIdentifier("textures/block/cobblestone.png", "subpacks/SP2/textures/blocks/cobblestone.png");
        Folder.SetIdentifier("textures/block/sand.png", "subpacks/SP2/textures/blocks/sand.png");
        Folder.SetIdentifier("textures/block/stone.png", "subpacks/SP2/textures/blocks/stone.png");

        Folder.SetIdentifier("textures/item/spruce_leaves.png", "subpacks/SP2/textures/blocks/stone.png");
        Folder.SetIdentifier("textures/block/spruce_leaves.png", "subpacks/SP2/textures/blocks/leaves_spruce.png");

        Folder.SetIdentifier("textures/block/golden_dandelion.png", "subpacks/SP2/textures/blocks/carried/golden_dandelion_carried.png");
    }
}