package com.java_unbound.shared.loaders.main_screen;

import com.java_unbound.JavaUnbound;
import com.java_unbound.JavaUnboundUtilities;
import com.java_unbound.shared.resourcepack.Folder;

public class PanoramaLoader {
    public static void LoadPanorama() {
        for (int Index = 0; Index < 6; Index++) {
            String MinecraftPath = "textures/gui/title/background/panorama_" + Index + ".png";
            String PanoramaPath = "textures/ui/panorama_" + Index  + ".png";
            String PanoramaResource = JavaUnboundUtilities.ResolveResourcePath(JavaUnboundUtilities.GetTextureResource(PanoramaPath));

            if (PanoramaResource == null) {
                JavaUnbound.LOGGER.error("Panorama resource not found!");
                continue;
            }

            JavaUnbound.LOGGER.debug(PanoramaResource + " -> " + MinecraftPath);

            Folder.SetIdentifier(MinecraftPath, PanoramaResource);
        }
    }
}
