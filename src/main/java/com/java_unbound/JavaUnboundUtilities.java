package com.java_unbound;

import com.java_unbound.shared.resourcepack.Folder;

import java.nio.file.Files;
import java.nio.file.Path;

public class JavaUnboundUtilities {
    public static Path GetTextureResource(String ResourcePath) {
        Path ConfigFolder = Folder.GetConfigFolder();

        if (ConfigFolder == null) {
            JavaUnbound.LOGGER.error("Config folder is null");
            return null;
        }

        return ConfigFolder.resolve(ResourcePath).toAbsolutePath().normalize();
    }

    public static String ResolveResourcePath(Path ResourcePath) {
        Path ConfigFolder = Folder.GetConfigFolder();
        Path RelativePath = ConfigFolder.toAbsolutePath().normalize().relativize(ResourcePath.toAbsolutePath().normalize());

        Path SubpackResource = Path.of("subpacks", JavaUnbound.SUB_PACK).resolve(RelativePath);
        Path BaseResource = RelativePath;

        if (Files.exists(ConfigFolder.resolve(SubpackResource))) {return SubpackResource.toString().replace('\\', '/');}
        if (Files.exists(ConfigFolder.resolve(BaseResource))) {return BaseResource.toString().replace('\\', '/');}

        return null;
    }
}