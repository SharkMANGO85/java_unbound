package com.java_unbound.loader.resourcepack;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class VirtualResources {
    private static final Map<String, byte[]> Resources = new LinkedHashMap<>();

    private VirtualResources() {
    }

    public static void Set(String MinecraftPath, String Content) {
        Resources.put(Normalize(MinecraftPath), Content.getBytes(StandardCharsets.UTF_8));
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

    private static String Normalize(String Path) {
        return Path.replace('\\', '/').replaceFirst("^/+", "");
    }
}