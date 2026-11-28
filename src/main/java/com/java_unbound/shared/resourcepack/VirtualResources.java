package com.java_unbound.shared.resourcepack;

import com.java_unbound.JavaUnbound;
import com.java_unbound.shared.definitions.resource_pack.ResourcePackDefinition;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class VirtualResources {
    private final ResourcePackDefinition Definition;
    private final Map<String, byte[]> Resources = new LinkedHashMap<>();

    public VirtualResources(ResourcePackDefinition Definition) {
        this.Definition = Definition;
    }

    public void Set(String MinecraftPath, String Content) {
        String Normalized = Normalize(MinecraftPath);
        byte[] Bytes = Content.getBytes(StandardCharsets.UTF_8);

        Resources.put(Normalized, Bytes);

        WriteDebugFile(Normalized, Bytes);
    }

    public byte[] Get(String MinecraftPath) {
        return Resources.get(Normalize(MinecraftPath));
    }

    public boolean Has(String MinecraftPath) {
        return Resources.containsKey(Normalize(MinecraftPath));
    }

    public Map<String, byte[]> GetResources() {
        return Collections.unmodifiableMap(Resources);
    }

    public void Clear() {
        Resources.clear();
    }

    private void WriteDebugFile(String MinecraftPath, byte[] Bytes) {
        Path DesiredPath = PackFolders.GetDebugFolder(Definition).resolve("assets").resolve(MinecraftPath);

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