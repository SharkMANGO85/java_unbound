package com.java_unbound.loader.resourcepack;

//replaces java textures with the bedrock resourcepack textures
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

        Folder.SetIdentifier("textures/colormap/grass.png", "subpacks/SP2/textures/colormap/grass.png");
        Folder.SetIdentifier("textures/colormap/foliage.png", "subpacks/SP2/textures/colormap/foliage.png");
        Folder.SetIdentifier("textures/colormap/dry_foliage.png", "subpacks/SP2/textures/colormap/dry_foliage.png");

        Folder.SetIdentifier("textures/misc/pumpkinblur.png", "subpacks/SP2/textures/misc/pumpkinblur.png");

































































        Folder.SetIdentifier("geckolib/models/entity/creeper.geo.json", "subpacks/creeper.geo.json");
        Folder.SetIdentifier("textures/entity/creeper.png", "subpacks/creeper.png");

        Folder.SetIdentifier("geckolib/models/entity/fox.geo.json", "subpacks/fox.geo.json");
        Folder.SetIdentifier("textures/entity/fox.png", "subpacks/fox.png");

        Folder.SetIdentifier("geckolib/models/entity/snow_golem.geo.json", "subpacks/snow_golem.geo.json");
        Folder.SetIdentifier("textures/entity/snow_golem.png", "subpacks/snow_golem.png");
    }
}