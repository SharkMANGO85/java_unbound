package com.java_unbound.shared.loaders.main_screen;

import com.java_unbound.JavaUnbound;
import com.java_unbound.JavaUnboundUtilities;
import com.java_unbound.shared.resourcepack.Folder;

public class TitleLoader {
    public static boolean TitleAvailable = false;

    public static void LoadTitle() {
        String TitleResource = JavaUnboundUtilities.ResolveResourcePath(JavaUnboundUtilities.GetTextureResource("textures/ui/title.png"));

        if (TitleResource == null) {
            JavaUnbound.LOGGER.error("Title resource not found!");
            return;
        }

        TitleAvailable = true;

        JavaUnbound.LOGGER.debug(TitleResource);

        Folder.SetIdentifier("textures/gui/title/minecraft.png", TitleResource);
    }
}
