package com.java_unbound.fallback;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.java_unbound.shared.converters.TgaConverter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class FallbackUtilities {
    private static final String BundledRoot = "/assets/resource_pack_fallback/";

    public static JsonObject GetFallbackJsonObject(String ResourcePath) {
        byte[] Data = GetFallbackBytes(ResourcePath);
        if (Data == null) {
            return null;
        }

        try (var Reader = new InputStreamReader(new ByteArrayInputStream(Data), StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(Reader).getAsJsonObject();
        } catch (Exception Exception) {
            return null;
        }
    }

    public static BufferedImage GetFallbackImage(String ResourcePath) {
        byte[] Data = GetFallbackBytes(ResourcePath);

        if (Data == null && ResourcePath.toLowerCase().endsWith(".png")) {
            Data = GetFallbackBytes(ResourcePath.substring(0, ResourcePath.length() - 4) + ".tga");
        }

        if (Data == null) {
            return null;
        }

        try {
            BufferedImage Image = ImageIO.read(new ByteArrayInputStream(Data));

            if (Image != null) {
                return Image;
            }

            return TgaConverter.DecodeTga(Data);
        } catch (Exception Exception) {
            return null;
        }
    }

    public static byte[] GetFallbackBytes(String ResourcePath) {
        String Relative = ResourcePath.replace('\\', '/');

        while (Relative.startsWith("/")) Relative = Relative.substring(1);

        try (var InputStream = FallbackUtilities.class.getResourceAsStream(BundledRoot + Relative)) {
            if (InputStream != null) {
                return InputStream.readAllBytes();
            }
        } catch (Exception Exception) {

        }

        Path Remote = RemoteFallbackSource.GetFile(Relative);
        if (Remote == null) {return null;}

        try {
            return Files.readAllBytes(Remote);
        } catch (Exception Exception) {
            return null;
        }
    }
}