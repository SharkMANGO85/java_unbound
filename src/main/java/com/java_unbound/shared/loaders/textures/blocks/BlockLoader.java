package com.java_unbound.shared.loaders.textures.blocks;

import com.java_unbound.shared.loaders.textures.blocks.types.CoralBlockLoader;
import com.java_unbound.shared.resourcepack.VirtualResources;

public class BlockLoader {
    public static void CreateCarriedRegistry(String ItemName, String Sprite) {
        String ModelPath = "models/item/" + ItemName + "_carried.json";
        String DefinitionPath = "items/" + ItemName + ".json";

        String ModelJson = """
            {
              "parent": "minecraft:item/generated",
              "textures": {
                "layer0": "minecraft:%s"
              }
            }
            """.formatted(Sprite);

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

    public static void LoadBlockTextures() {
        CoralBlockLoader.LoadCoralBlocks();
    }
}