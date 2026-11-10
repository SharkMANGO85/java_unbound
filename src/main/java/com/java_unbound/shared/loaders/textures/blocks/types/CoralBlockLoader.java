package com.java_unbound.shared.loaders.textures.blocks.types;

import com.java_unbound.JavaUnbound;
import com.java_unbound.JavaUnboundUtilities;
import com.java_unbound.shared.loaders.textures.blocks.BlockLoader;
import com.java_unbound.shared.resourcepack.Folder;

import java.util.Map;

public class CoralBlockLoader {
    private static final Map<String, String> CoralTypes = Map.of("blue", "tube", "pink", "brain", "purple", "bubble", "red", "fire", "yellow", "horn");

    private static String Resolve(String ResourcePath) {
        var Resource = JavaUnboundUtilities.GetTextureResource(ResourcePath);

        if (Resource == null) {
            JavaUnbound.LOGGER.warn("Texture resource not found: {}", ResourcePath);
            return null;
        }

        return JavaUnboundUtilities.ResolveResourcePath(Resource);
    }

    private static void LoadCoral(String PlacedPath, String CarriedPath, String ItemName) {
        String Placed = Resolve(PlacedPath);

        if (Placed != null) {
            Folder.SetIdentifier("textures/block/" + ItemName + ".png", Placed);
        }

        String Carried = Resolve(CarriedPath);

        if (Carried != null) {
            String CarriedSprite = "item/carried/" + ItemName;

            Folder.SetIdentifier("textures/" + CarriedSprite + ".png", Carried);
            BlockLoader.CreateCarriedRegistry(ItemName, CarriedSprite);
        }
    }

    public static void LoadCoralBlocks() {
        for (Map.Entry<String, String> Coral : CoralTypes.entrySet()) {
            String Color = Coral.getKey();
            String Type = Coral.getValue();

            for (String Variant : new String[]{"fan", "plant"}) {
                String MinecraftType = Variant.equals("fan") ? Type + "_coral_fan" : Type + "_coral";

                for (String Dead : new String[]{"", "dead_"}) {
                    String Suffix = Dead.isEmpty() ? "" : "_dead";
                    String Base = "coral_" + Variant + "_" + Color + Suffix;

                    String PlacedPath = "textures/blocks/" + Base + ".png";
                    String CarriedPath = "textures/blocks/carried/" + Base + "_carried.png";

                    LoadCoral(PlacedPath, CarriedPath, Dead + MinecraftType);
                }
            }
        }
    }
}