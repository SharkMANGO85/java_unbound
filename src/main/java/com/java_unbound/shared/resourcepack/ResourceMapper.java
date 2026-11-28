package com.java_unbound.shared.resourcepack;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class ResourceMapper {
    private final Map<String, String> Mappings = new HashMap<>();

    public void SetIdentifier(String MinecraftPath, String ResourcePath) {
        Mappings.put(Normalize(MinecraftPath), Normalize(ResourcePath));
    }

    public void RemoveIdentifier(String MinecraftPath) {
        Mappings.remove(Normalize(MinecraftPath));
    }

    public String GetIdentifier(String MinecraftPath) {
        return Mappings.get(Normalize(MinecraftPath));
    }

    public boolean HasIdentifier(String MinecraftPath) {
        return Mappings.containsKey(Normalize(MinecraftPath));
    }

    public void Clear() {
        Mappings.clear();
    }

    public Map<String, String> GetMappings() {
        return Collections.unmodifiableMap(Mappings);
    }

    private static String Normalize(String Path) {
        return Path.replace('\\', '/').replaceFirst("^/+", "");
    }
}