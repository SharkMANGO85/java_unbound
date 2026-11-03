package com.java_unbound.loader.textures.loaders.types;

import com.java_unbound.loader.textures.loaders.TextureLoader;

import java.io.File;

import static com.java_unbound.loader.textures.loaders.TextureLoader.ManualMappings;

public final class PaintingTextureLoader {
    private PaintingTextureLoader() {}

    public static void LoadPaintingTextures() {
        File[] Paintings = TextureLoader.SubpackPaintingTextureFolder.toFile().listFiles();
        if (Paintings == null) {return;}

        for (File Painting : Paintings) {
            if (!Painting.isFile() || !Painting.getName().endsWith(".png")) {continue;}

            String FileName = Painting.getName().replace(".png", "");
            if (FileName.equals("kz")) {continue;}

            ManualMappings.put("painting/" + FileName, "painting/" + FileName);
        }
    }
}