package com.java_unbound.loader.textures.gui;

import com.java_unbound.JavaUnbound;
import com.java_unbound.loader.resourcepack.Folder;
import com.java_unbound.loader.resourcepack.FolderResources;

import java.nio.file.StandardCopyOption;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Panorama {
    private Panorama() {
    }

    public static void CreatePanorama() throws IOException {
        Path ResourcePack = Folder.GetResourceFolder();

        FolderResources.CreateFolder(ResourcePack, "assets/minecraft/textures/gui/title/background");

        Path SourceFolder = ResourcePack.resolve("subpacks").resolve(JavaUnbound.SUBPACK).resolve("textures").resolve("ui");
        Path DestinationFolder = ResourcePack.resolve("assets").resolve("minecraft").resolve("textures").resolve("gui").resolve("title").resolve("background");

        for (int Index = 0; Index <= 5; Index++) {
            Path Source = SourceFolder.resolve("panorama_" + Index + ".png");
            Path Destination = DestinationFolder.resolve("panorama_" + Index + ".png");

            Files.copy(Source, Destination, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}