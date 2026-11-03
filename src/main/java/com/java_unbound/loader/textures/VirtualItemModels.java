package com.java_unbound.loader.textures;

import com.java_unbound.loader.resourcepack.VirtualResources;

public final class VirtualItemModels {
    private VirtualItemModels() {}

    public static void RegisterBed(String BedName) {
        String ModelPath = "models/item/" + BedName + "_gui.json";
        String DefinitionPath = "items/" + BedName + ".json";

        String ModelJson = """
                {
                  "parent": "minecraft:item/generated",
                  "textures": {
                    "layer0": "minecraft:item/%s"
                  }
                }
                """.formatted(BedName);

        String DefinitionJson = """
                {
                  "model": {
                    "type": "minecraft:model",
                    "model": "minecraft:item/%s_gui"
                  }
                }
                """.formatted(BedName);

        VirtualResources.Set(ModelPath, ModelJson);
        VirtualResources.Set(DefinitionPath, DefinitionJson);
    }

    /**
     * Makes the inventory/UI icon of an item use a "carried" texture, while the placed block keeps its own texture.
     *
     * @param ItemName    Java item name, e.g. "poppy"
     * @param TexturePath texture path without namespace/extension, e.g. "item/carried/poppy"
     */
    public static void RegisterCarriedItem(String ItemName, String TexturePath) {
        String ModelPath = "models/item/" + ItemName + "_carried.json";
        String DefinitionPath = "items/" + ItemName + ".json";

        String ModelJson = """
                {
                  "parent": "minecraft:item/generated",
                  "textures": {
                    "layer0": "minecraft:%s"
                  }
                }
                """.formatted(TexturePath);

        String DefinitionJson = """
                {
                  "model": {
                    "type": "minecraft:model",
                    "model": "minecraft:item/%s_carried"
                  }
                }
                """.formatted(ItemName);

        VirtualResources.Set(ModelPath, ModelJson);
        VirtualResources.Set(DefinitionPath, DefinitionJson);
    }
}