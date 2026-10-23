package com.java_unbound.loader.resourcepack;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

//can set a texture located in the bedrock resourcepack folder to a one which should be replaced
public final class ResourceMapper {
    private static final Map<String, String> Mappings = new HashMap<>();

    private ResourceMapper() {
    }

    public static void SetIdentifier(String MinecraftPath, String ResourcePath) {
        String target = Normalize(MinecraftPath);
        String source = Normalize(ResourcePath);

        Mappings.put(target, source);
    }

    public static String GetIdentifier(String MinecraftPath) {
        return Mappings.get(Normalize(MinecraftPath));
    }

    public static boolean HasIdentifier(String MinecraftPath) {
        return Mappings.containsKey(Normalize(MinecraftPath));
    }

    public static void Clear() {
        Mappings.clear();
        VirtualResources.Clear();
    }

    public static Map<String, String> GetMappings() {
        return Collections.unmodifiableMap(Mappings);
    }

    private static String Normalize(String Path) {
        return Path.replace('\\', '/').replaceFirst("^/+", "");
    }
}