package com.java_unbound.shared.resources.types;

import com.java_unbound.shared.resources.ResourceUtilities;
import net.minecraft.resources.Identifier;

public class UIResourceUpdater {
    public static Identifier TOGGLE_ENABLED;
    public static Identifier TOGGLE_DISABLED;
    public static Identifier TOGGLE_ENABLED_HOVERED;
    public static Identifier TOGGLE_DISABLED_HOVERED;

    public static Identifier SETTINGS_ICON;

    public static Identifier PANORAMA_0;
    public static Identifier PANORAMA_1;
    public static Identifier PANORAMA_2;
    public static Identifier PANORAMA_3;
    public static Identifier PANORAMA_4;
    public static Identifier PANORAMA_5;

    public static void UpdateUIResources() {
        TOGGLE_ENABLED = GetTexture("toggle_on");
        TOGGLE_DISABLED = GetTexture("toggle_off");
        TOGGLE_ENABLED_HOVERED = GetTexture("toggle_on_hover");
        TOGGLE_DISABLED_HOVERED = GetTexture("toggle_off_hover");

        SETTINGS_ICON = GetTexture("icon_setting");

        PANORAMA_0 = GetTexture("panorama_0");
        PANORAMA_1 = GetTexture("panorama_1");
        PANORAMA_2 = GetTexture("panorama_2");
        PANORAMA_3 = GetTexture("panorama_3");
        PANORAMA_4 = GetTexture("panorama_4");
        PANORAMA_5 = GetTexture("panorama_5");
    }

    private static Identifier GetTexture(String Name) {
        return ResourceUtilities.GetTextureIdentifier(null, "textures/ui/" + Name + ".png");
    }
}