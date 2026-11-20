package com.java_unbound.shared.loaders.main_screen;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.java_unbound.JavaUnbound;
import com.java_unbound.shared.resourcepack.Folder;
import net.minecraft.server.packs.resources.IoSupplier;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class SplashLoader {
    public static boolean SplashesAvailable = false;

    private static Path GetSplashResource() {

        if (SplashResource == null || !Files.isRegularFile(SplashResource)) {
            return null;
        }

        return SplashResource;
    }

    public static IoSupplier<InputStream> LoadSplashes() throws IOException {
        Path SplashResource = GetSplashResource();

        if (SplashResource == null) {
            return null;
        }

        try {
            JsonObject Object = JsonParser.parseString(Files.readString(SplashResource)).getAsJsonObject();
            JsonArray Splashes = GetSplashes(Object);

            if (Splashes == null) {
                return null;
            }

            byte[] Data = GetSplashData(Splashes);

            SplashesAvailable = true;

            return () -> new ByteArrayInputStream(Data);
        } catch (Exception Exception) {
            JavaUnbound.LOGGER.error("Failed to load splashes", Exception);
            return null;
        }
    }

    private static JsonArray GetSplashes(JsonObject Object) {
        return Object.getAsJsonArray("splashes");
    }

    private static byte[] GetSplashData(JsonArray Splashes) {
        StringBuilder Text = new StringBuilder();

        for (JsonElement Splash : Splashes) {
            Text.append(Splash.getAsString()).append('\n');
        }

        return Text.toString().getBytes(StandardCharsets.UTF_8);
    }

    private static Path ResolvePath(Path ResourcePack, Path File) {
        if (Files.exists(File)) {return File;}

        Path RelativePath;

        try {
            RelativePath = ResourcePack.relativize(File);
        } catch (IllegalArgumentException Exception) {
            return null;
        }

        Path Current = ResourcePack;

        for (Path Part : RelativePath) {
            Current = ResolvePart(Current, Part.toString());

            if (Current == null) {
                return null;
            }
        }

        return Current;
    }

    private static Path ResolvePart(Path Current, String Name) {
        Path Exact = Current.resolve(Name);

        if (Files.exists(Exact)) {return Exact;}

        try (Stream<Path> FilesStream = Files.list(Current)) {
            return FilesStream.filter(FilePart -> FilePart.getFileName().toString().equalsIgnoreCase(Name)).findFirst().orElse(null);
        } catch (IOException Exception) {
            return null;
        }
    }
}