package com.java_unbound.loader.ui;

import net.minecraft.server.packs.resources.IoSupplier;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public final class Splashes {
    private Splashes() {
    }

    public static IoSupplier<InputStream> GetSplashResource(Path ResourcePack) {
        Path File = ResourcePack.resolve("splashes.json");
        File = ResolvePath(ResourcePack, File);

        if (File == null || !Files.isRegularFile(File)) {
            return null;
        }

        try {
            String Json = Files.readString(File);
            com.google.gson.JsonObject Object = com.google.gson.JsonParser.parseString(Json).getAsJsonObject();
            com.google.gson.JsonArray Splashes = Object.getAsJsonArray("splashes");

            StringBuilder Text = new StringBuilder();

            for (com.google.gson.JsonElement Splash : Splashes) {
                Text.append(Splash.getAsString()).append('\n');
            }

            byte[] Data = Text.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);

            return () -> new java.io.ByteArrayInputStream(Data);
        } catch (Exception Exception) {
            Exception.printStackTrace();
            return null;
        }
    }

    private static Path ResolvePath(Path ResourcePack, Path File) {
        if (Files.exists(File)) {
            return File;
        }

        Path RelativePath;

        try {
            RelativePath = ResourcePack.relativize(File);
        } catch (IllegalArgumentException Exception) {
            return null;
        }

        Path Current = ResourcePack;

        for (Path Part : RelativePath) {
            String Name = Part.toString();
            Path Exact = Current.resolve(Name);

            if (Files.exists(Exact)) {
                Current = Exact;
                continue;
            }

            Path Match = null;

            try (java.util.stream.Stream<Path> FilesStream = Files.list(Current)) {
                Match = FilesStream.filter(FilePart -> FilePart.getFileName().toString().equalsIgnoreCase(Name)).findFirst().orElse(null);
            } catch (java.io.IOException Exception) {
                return null;
            }

            if (Match == null) {
                return null;
            }

            Current = Match;
        }

        return Current;
    }
}