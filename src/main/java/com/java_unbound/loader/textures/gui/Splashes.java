package com.java_unbound.loader.textures.gui;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.java_unbound.loader.resourcepack.Folder;
import com.java_unbound.loader.resourcepack.FolderResources;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class Splashes {

    public static void CreateSplashes() throws IOException {
        Path resourcePack = Folder.GetResourceFolder();

        FolderResources.CreateFolder(resourcePack, "assets/minecraft/texts");

        Path destinationFolder = resourcePack.resolve("assets").resolve("minecraft").resolve("texts");

        Path source = resourcePack.resolve("splashes.json");
        Path destination = destinationFolder.resolve("splashes.txt");

        String json = Files.readString(source, StandardCharsets.UTF_8);

        JsonObject object = JsonParser.parseString(json).getAsJsonObject();
        JsonArray splashes = object.getAsJsonArray("splashes");

        StringBuilder output = new StringBuilder();

        for (int i = 0; i < splashes.size(); i++) {
            output.append(splashes.get(i).getAsString());

            if (i < splashes.size() - 1) {
                output.append(System.lineSeparator());
            }
        }

        Files.writeString(destination, output.toString(), StandardCharsets.UTF_8);
    }
}