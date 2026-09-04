package com.java_unbound.loader.gui;

import com.java_unbound.JavaUnbound;
import com.java_unbound.loader.resourcepack.Folder;
import com.java_unbound.loader.resourcepack.FolderResources;
import net.minecraft.client.gui.components.LogoRenderer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class Title {
    public static void CreateTitle() throws IOException {
        Path ResourcePack = Folder.GetResourceFolder();

        FolderResources.CreateFolder(ResourcePack, "assets/minecraft/textures/gui/title");

        Path SourceFolder = ResourcePack.resolve("subpacks").resolve(JavaUnbound.SUBPACK).resolve("textures").resolve("ui");
        Path DestinationFolder = ResourcePack.resolve("assets").resolve("minecraft").resolve("textures").resolve("gui").resolve("title");

        Path Source = SourceFolder.resolve("title.png");
        Path Destination = DestinationFolder.resolve("minecraft.png");

        Files.copy(Source, Destination, StandardCopyOption.REPLACE_EXISTING);
    }
}
