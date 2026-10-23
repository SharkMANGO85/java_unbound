package com.java_unbound.loader.ui.textures;

import com.java_unbound.JavaUnbound;
import com.java_unbound.loader.resourcepack.VirtualResources;

import java.util.Set;

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
}