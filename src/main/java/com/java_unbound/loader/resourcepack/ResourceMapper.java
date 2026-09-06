package com.java_unbound.loader.resourcepack;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class ResourceMapper {
    private static final Map<String, String> Mappings = new HashMap<>();

    private ResourceMapper() {
    }

    public static void SetIdentifier(String MinecraftPath, String ResourcePath) {
        Mappings.put(Normalize(MinecraftPath), Normalize(ResourcePath));
    }

    public static String GetIdentifier(String MinecraftPath) {
        return Mappings.get(Normalize(MinecraftPath));
    }

    public static boolean HasIdentifier(String MinecraftPath) {
        return Mappings.containsKey(Normalize(MinecraftPath));
    }

    public static void Clear() {
        Mappings.clear();
    }

    public static Map<String, String> GetMappings() {
        return Collections.unmodifiableMap(Mappings);
    }

    private static String Normalize(String Path) {
        return Path.replace('\\', '/').replaceFirst("^/+", "");
    }
}