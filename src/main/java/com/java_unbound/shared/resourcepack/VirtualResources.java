package com.java_unbound.shared.resourcepack;

import com.java_unbound.JavaUnbound;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class VirtualResources {
    private static final Map<String, byte[]> Resources = new LinkedHashMap<>();

    private VirtualResources() {}

    public static void Set(String MinecraftPath, String Content) {
        String Normalized = Normalize(MinecraftPath);
        byte[] Bytes = Content.getBytes(StandardCharsets.UTF_8);

        Resources.put(Normalized, Bytes);
        WriteDebugFile(Normalized, Bytes);
    }

    public static byte[] Get(String MinecraftPath) {
        return Resources.get(Normalize(MinecraftPath));
    }

    public static boolean Has(String MinecraftPath) {
        return Resources.containsKey(Normalize(MinecraftPath));
    }

    public static Map<String, byte[]> GetResources() {
        return Collections.unmodifiableMap(Resources);
    }

    public static void Clear() {
        Resources.clear();
    }

    private static void WriteDebugFile(String MinecraftPath, byte[] Bytes) {
        Path DesiredPath = Folder.GetDebugFolder().resolve("assets").resolve(MinecraftPath);

        try {
            Files.createDirectories(DesiredPath.getParent());
            Files.write(DesiredPath, Bytes);
        } catch (IOException Exception) {
            JavaUnbound.LOGGER.error("Failed to write debug resource: " + DesiredPath, Exception);
        }
    }

    private static String Normalize(String Path) {
        return Path.replace('\\', '/').replaceFirst("^/+", "");
    }
}