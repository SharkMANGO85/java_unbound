package com.java_unbound.loader.ui.textures;

import com.java_unbound.JavaUnbound;
import com.java_unbound.loader.resourcepack.Folder;
import com.java_unbound.loader.ui.textures.loaders.TextureLoader;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static com.java_unbound.loader.ui.textures.loaders.TextureLoader.ManualMappings;

public final class PaintingAtlasExtractor {
    private static final List<Painting> Paintings = List.of(
            new Painting("kebab", 0, 0, 16, 16),
            new Painting("aztec", 16, 0, 16, 16),
            new Painting("alban", 32, 0, 16, 16),
            new Painting("aztec2", 48, 0, 16, 16),
            new Painting("bomb", 64, 0, 16, 16),
            new Painting("plant", 80, 0, 16, 16),
            new Painting("wasteland", 96, 0, 16, 16),

            new Painting("pool", 0, 32, 32, 16),
            new Painting("courbet", 32, 32, 32, 16),
            new Painting("sea", 64, 32, 32, 16),
            new Painting("sunset", 96, 32, 32, 16),
            new Painting("creebet", 128, 32, 32, 16),

            new Painting("wanderer", 0, 64, 16, 32),
            new Painting("graham", 16, 64, 16, 32),

            new Painting("match", 0, 128, 32, 32),
            new Painting("bust", 32, 128, 32, 32),
            new Painting("stage", 64, 128, 32, 32),
            new Painting("void", 96, 128, 32, 32),
            new Painting("skull_and_roses", 128, 128, 32, 32),
            new Painting("wither", 160, 128, 32, 32),

            new Painting("fighters", 0, 96, 64, 32),

            new Painting("pointer", 0, 192, 64, 64),
            new Painting("pigscene", 64, 192, 64, 64),
            new Painting("burning_skull", 128, 192, 64, 64),

            new Painting("skeleton", 192, 64, 64, 48),
            new Painting("donkey_kong", 192, 112, 64, 48)
    );

    private PaintingAtlasExtractor() {}

    public static void ExtractPaintingAtlas() throws IOException {
        Path PaintingFolder = TextureLoader.SubpackPaintingTextureFolder;
        Path AtlasPath = PaintingFolder.resolve("kz.png");

        if (!Files.exists(AtlasPath)) {
            throw new IOException("Painting atlas not found: " + AtlasPath);
        }

        BufferedImage Atlas = ImageIO.read(AtlasPath.toFile());

        if (Atlas == null) {
            throw new IOException("Could not read painting atlas: " + AtlasPath);
        }

        if (Atlas.getWidth() != 256 || Atlas.getHeight() != 256) {
            throw new IOException("Expected kz.png to be 256x256, got " + Atlas.getWidth() + "x" + Atlas.getHeight());
        }

        int Extracted = 0;

        for (Painting Painting : Paintings) {
            BufferedImage Image = Atlas.getSubimage(Painting.X, Painting.Y, Painting.Width, Painting.Height);
            Path OutputPath = PaintingFolder.resolve(Painting.Name + ".png");

            ImageIO.write(Image, "png", OutputPath.toFile());

            ManualMappings.put("painting/" + Painting.Name, "painting/" + Painting.Name);

            Extracted++;
        }
    }

    private static final class Painting {
        private final String Name;
        private final int X;
        private final int Y;
        private final int Width;
        private final int Height;

        private Painting(String Name, int X, int Y, int Width, int Height) {
            this.Name = Name;
            this.X = X;
            this.Y = Y;
            this.Width = Width;
            this.Height = Height;
        }
    }
}