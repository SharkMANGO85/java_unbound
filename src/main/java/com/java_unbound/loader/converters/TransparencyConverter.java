package com.java_unbound.loader.converters;

import com.java_unbound.JavaUnbound;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class TransparencyConverter {
    public static final HashMap<String, Double> Transparency = new HashMap<>();

    static {
        Transparency.put("blocks/redstone_block.png", 255.0);
        Transparency.put("blocks/water_flow_grey.png", 255.0 / 2);
        Transparency.put("blocks/water_still_grey.png", 255.0 / 2);
    }
    
    public static Runnable LoadTextureTransparencies(File TexturesRoot) {
        return () -> {
            for (Map.Entry<String, Double> Entry : Transparency.entrySet()) {
                File File = new File(TexturesRoot, Entry.getKey());
                if (!File.isFile()) {
                    JavaUnbound.LOGGER.error("[TransparencyConverter] Texture not found: " + File);
                    continue;
                }

                try {
                    BufferedImage Source = ImageIO.read(File);
                    if (Source == null) {
                        JavaUnbound.LOGGER.error("[TransparencyConverter] Not a readable image: " + File);
                        continue;
                    }

                    BufferedImage Fixed = applyAlpha(Source, Entry.getValue());
                    ImageIO.write(Fixed, "png", File);
                } catch (IOException e) {
                    JavaUnbound.LOGGER.error("[TransparencyConverter] Failed to convert " + File + ": " + e.getMessage());
                }
            }
        };
    }
    
    public static BufferedImage applyAlpha(BufferedImage Source, double AlphaValue) {
        int Alpha = (int) Math.max(0, Math.min(255, Math.round(AlphaValue)));

        BufferedImage Out = new BufferedImage(Source.getWidth(), Source.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int Y = 0; Y < Source.getHeight(); Y++) {
            for (int X = 0; X < Source.getWidth(); X++) {
                int rgb = Source.getRGB(X, Y) & 0x00FFFFFF;
                Out.setRGB(X, Y, (Alpha << 24) | rgb);
            }
        }
        return Out;
    }
}