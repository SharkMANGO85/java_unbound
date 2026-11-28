package com.java_unbound.shared.resources.textures.main_screen;

import com.java_unbound.JavaUnbound;
import com.java_unbound.shared.resources.ResourceUtilities;

public class PanoramaLoader {
    public static void LoadPanoramaTextures() {
        int Mapped = 0;

        for (int Index = 0; Index <= 5; Index++) {
            boolean Result = ResourceUtilities.MapTexture("textures/gui/title/background/panorama_" + Index + ".png", "textures/ui/panorama_" + Index + ".png");

            if (Result) {
                Mapped++;
            }
        }

        JavaUnbound.LOGGER.info("[PanoramaLoader] Mapped {}/6 panorama textures from bedrock packs", Mapped);
    }
}