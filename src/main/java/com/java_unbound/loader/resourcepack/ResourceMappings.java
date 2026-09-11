package com.java_unbound.loader.resourcepack;

public final class ResourceMappings {
    private ResourceMappings() {
    }

    public static void Register() {
        RegisterLoadingScreen();
    }

    private static void RegisterLoadingScreen() {
        Folder.SetIdentifier("textures/gui/title/background/panorama_0.png", "subpacks/SP2/textures/ui/panorama_0.png");
        Folder.SetIdentifier("textures/gui/title/background/panorama_1.png", "subpacks/SP2/textures/ui/panorama_1.png");
        Folder.SetIdentifier("textures/gui/title/background/panorama_2.png", "subpacks/SP2/textures/ui/panorama_2.png");
        Folder.SetIdentifier("textures/gui/title/background/panorama_3.png", "subpacks/SP2/textures/ui/panorama_3.png");
        Folder.SetIdentifier("textures/gui/title/background/panorama_4.png", "subpacks/SP2/textures/ui/panorama_4.png");
        Folder.SetIdentifier("textures/gui/title/background/panorama_5.png", "subpacks/SP2/textures/ui/panorama_5.png");
        Folder.SetIdentifier("textures/gui/title/minecraft.png", "subpacks/SP2/textures/ui/title.png");
    }
}