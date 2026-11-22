package com.java_unbound.attachables.resolvers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.java_unbound.JavaUnbound;
import com.java_unbound.shared.definitions.textures.TextureDefinition;
import com.java_unbound.shared.resourcepack.Folder;
import com.java_unbound.shared.utilities.JsonUtilities;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AttachableTextureResolver {
    private static final Map<String, TextureDefinition> TextureCache = new HashMap<>();
    private static List<String> MissingTextureCache = new ArrayList<>();

    public static HashMap<String, TextureDefinition> ResolveTextures(Path AttachablePath) {
        String Identifier = JsonUtilities.GetString("minecraft:attachable.description.identifier", AttachablePath);

        if (Identifier == null) {
            JavaUnbound.LOGGER.error("Could not find minecraft:attachable.description.identifier for " + AttachablePath);
            return new HashMap<>();
        }

        JsonElement TexturesElement = JsonUtilities.GetElement("minecraft:attachable.description.textures", AttachablePath);

        if (TexturesElement == null) {
            JavaUnbound.LOGGER.error("Could not find attachable.description.textures for " + AttachablePath);
            return new HashMap<>();
        }

        HashMap<String, TextureDefinition> ResolvedTextures = new HashMap<>();

        if (TexturesElement.isJsonPrimitive() && TexturesElement.getAsJsonPrimitive().isString()) {
            String TexturePath = TexturesElement.getAsString();
            TextureDefinition Definition = GetTexturePath(Identifier, TexturePath);

            if (Definition != null) {
                ResolvedTextures.put(Identifier, Definition);
            } else {
                if (!MissingTextureCache.contains(TexturePath)) {
                    MissingTextureCache.add(TexturePath);

                    JavaUnbound.LOGGER.error("Could not resolve texture: " + TexturePath);
                }
            }
        } else if (TexturesElement.isJsonObject()) {
            for (Map.Entry<String, JsonElement> Entry : TexturesElement.getAsJsonObject().entrySet()) {
                String TextureIdentifier = Entry.getKey();
                String TexturePath = Entry.getValue().getAsString();

                TextureDefinition Definition = GetTexturePath(Identifier, TexturePath);

                if (Definition == null) {
                    if (!MissingTextureCache.contains(TexturePath)) {
                        MissingTextureCache.add(TexturePath);

                        JavaUnbound.LOGGER.error("Could not resolve texture: " + TexturePath);
                    }

                    continue;
                }

                ResolvedTextures.put(TextureIdentifier, Definition);
            }
        } else {
            JavaUnbound.LOGGER.error("Invalid minecraft:attachable.description.textures format for " + AttachablePath);
        }

        return ResolvedTextures;
    }

    public static void ClearCache() {
        TextureCache.clear();
        MissingTextureCache.clear();
    }

    private static TextureDefinition GetTexturePath(String AttachableIdentifier, String TexturePath) {
        String CacheKey = JavaUnbound.SUB_PACK + ":" + TexturePath;

        if (TextureCache.containsKey(CacheKey)) {
            return TextureCache.get(CacheKey);
        }

        Path ConfigFolder = Folder.GetAddonFolder();

        if (ConfigFolder == null || !ConfigFolder.toFile().exists()) {
            JavaUnbound.LOGGER.error("Config folder does not exist");
            return null;
        }

        Path SubpackTexturePath = ConfigFolder.resolve("subpacks").resolve(JavaUnbound.SUB_PACK);
        Path BaseTexturePath = ConfigFolder;

        Path TextureFile = SubpackTexturePath.resolve(TexturePath.contains(".png") ? TexturePath : TexturePath + ".png");

        if (!TextureFile.toFile().isFile()) {
            TextureFile = BaseTexturePath.resolve(TexturePath.contains(".png") ? TexturePath : TexturePath + ".png");
        }

        if (!TextureFile.toFile().isFile()) {
            return null;
        }

        String AbsolutePath = TextureFile.toAbsolutePath().toString();

        CreateDebugTextureFolder(AttachableIdentifier, TexturePath, AbsolutePath);

        TextureDefinition Definition = GetTextureDefinition(AttachableIdentifier, TexturePath, AbsolutePath);

        TextureCache.put(CacheKey, Definition);

        return Definition;
    }

    private static TextureDefinition GetTextureDefinition(String AttachableIdentifier, String TexturePath, String AbsolutePath) {
        TextureDefinition Definition = new TextureDefinition();

        Definition.TexturePath = TexturePath;
        Definition.AbsolutePath = AbsolutePath;

        return  Definition;
    }

    private static void CreateDebugTextureFolder(String AttachableIdentifier, String TexturePath, String AbsolutePath) {
        Path DevelopmentFolder = Folder.GetDevelopmentDebugFolder();

        if (DevelopmentFolder == null) {
            JavaUnbound.LOGGER.debug("Development folder does not exist");
            return;
        }

        String SafeAttachableIdentifier = AttachableIdentifier.replace("minecraft:", "").replace(':', '_');
        Path DesiredPath = DevelopmentFolder.resolve("attachables").resolve(SafeAttachableIdentifier).resolve(TexturePath.contains(".png") ? TexturePath : TexturePath + ".png");

        try {
            Files.createDirectories(DesiredPath.getParent());
            Files.copy(Path.of(AbsolutePath), DesiredPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException Exception) {
            JavaUnbound.LOGGER.error("Failed to copy texture: {}", AbsolutePath, Exception);
        }
    }
}
