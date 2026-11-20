package com.java_unbound;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.java_unbound.shared.resourcepack.Folder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class JavaUnboundUtilities {
    public static Path GetTextureResource(String ResourcePath) {

            JavaUnbound.LOGGER.error("Config folder is null");
            return null;
        }

        return ConfigFolder.resolve(ResourcePath).toAbsolutePath().normalize();
    }

    public static String ResolveResourcePath(Path ResourcePath) {
        Path RelativePath = ConfigFolder.toAbsolutePath().normalize().relativize(ResourcePath.toAbsolutePath().normalize());

        Path SubpackResource = Path.of("subpacks", JavaUnbound.SUB_PACK).resolve(RelativePath);
        Path BaseResource = RelativePath;

        if (Files.exists(ConfigFolder.resolve(SubpackResource))) {return SubpackResource.toString().replace('\\', '/');}
        if (Files.exists(ConfigFolder.resolve(BaseResource))) {return BaseResource.toString().replace('\\', '/');}

        return null;
    }

    public static JsonObject GetFileAsJsonObject(Path File) {
        try {
            String FileContent = Files.readString(File);
            JsonObject FileJson = JsonParser.parseString(FileContent).getAsJsonObject();

            return  FileJson;
        } catch (IOException Exception) {
            Exception.printStackTrace();
        }

        return null;
    }
}