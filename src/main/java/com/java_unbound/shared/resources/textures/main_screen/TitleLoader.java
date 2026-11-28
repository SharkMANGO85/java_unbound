package com.java_unbound.shared.resources.textures.main_screen;

import com.java_unbound.JavaUnbound;
import com.java_unbound.shared.resources.ResourceUtilities;

public class TitleLoader {
    public static void LoadTitleTexture() {
        boolean Result = ResourceUtilities.MapTexture("textures/gui/title/minecraft.png", "textures/ui/title.png");

        JavaUnbound.LOGGER.info("[TitleLoader] Title texture mapped from bedrock pack: {}", Result);
    }
}