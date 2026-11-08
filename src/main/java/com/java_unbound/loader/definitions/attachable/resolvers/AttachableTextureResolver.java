package com.java_unbound.loader.definitions.attachable.resolvers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.java_unbound.JavaUnbound;
import com.java_unbound.loader.definitions.attachable.types.texture.AttachableTextureDefinition;
import com.java_unbound.loader.resourcepack.Folder;
import com.java_unbound.utils.functions.GetJsonValue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class AttachableTextureResolver {
    private static Path OutputFolder = Folder.GetConfigsFolder().resolve("java_unbound_mod").resolve("output").resolve("textures");

    private static Path CopyTexture(Path TexturePath, String AttachableIdentifier, String OriginalTexturePath) {
        if (TexturePath == null || !Files.isRegularFile(TexturePath)) {
            JavaUnbound.LOGGER.error("Cannot copy attachable texture: {}", TexturePath);
            return null;
        }

        try {
            String AttachableFolderName = AttachableIdentifier.replace(":", "_").replace("minecraft_", "");
            String TextureName = Path.of(OriginalTexturePath).getFileName().toString();

            Path AttachableFolder = OutputFolder.resolve(AttachableFolderName);
            Files.createDirectories(AttachableFolder);

            Path TargetPath = AttachableFolder.resolve(TextureName + ".png").normalize();

            if (!TargetPath.startsWith(AttachableFolder.normalize())) {
                JavaUnbound.LOGGER.error("Invalid attachable texture output path: {}", TargetPath);
                return null;
            }

            Files.copy(TexturePath, TargetPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            JavaUnbound.LOGGER.debug("Copied attachable texture: {} -> {}", TexturePath, TargetPath);

            return TargetPath;
        } catch (IOException Exception) {
            JavaUnbound.LOGGER.error("Failed to copy attachable texture: {}", TexturePath, Exception);
            return null;
        }
    }

    private static JsonObject GetTextureElements(Path JsonFile) {
        if (JsonFile == null) {
            JavaUnbound.LOGGER.error("Cannot load attachable textures: JSON file path is null.");
            return new JsonObject();
        }

        if (!Files.isRegularFile(JsonFile)) {
            JavaUnbound.LOGGER.error("Attachable JSON file does not exist: {}", JsonFile);
            return new JsonObject();
        }

        try {
            String Content = Files.readString(JsonFile);
            JsonElement TexturesElement = GetJsonValue.GetValue(Content, "description.textures");

            if (TexturesElement == null) {
                JavaUnbound.LOGGER.warn("No description.textures found in attachable JSON: {}", JsonFile);
                return new JsonObject();
            }

            if (TexturesElement.isJsonPrimitive()) {
                JsonObject Wrapped = new JsonObject();
                Wrapped.add("default", TexturesElement);
                return Wrapped;
            }

            if (!TexturesElement.isJsonObject()) {
                JavaUnbound.LOGGER.error("description.textures is not a JSON object in: {} | Value: {}", JsonFile, TexturesElement);
                return new JsonObject();
            }

            return TexturesElement.getAsJsonObject();
        } catch (IOException Exception) {
            JavaUnbound.LOGGER.error("Failed to read attachable JSON file: {}", JsonFile, Exception);
            return new JsonObject();
        } catch (Exception Exception) {
            JavaUnbound.LOGGER.error("Failed to parse attachable textures from: {}", JsonFile, Exception);
            return new JsonObject();
        }
    }

    private static Path GetTexturePath(String TexturePath) {
        if (TexturePath == null || TexturePath.isBlank()) {
            JavaUnbound.LOGGER.warn("Cannot resolve attachable texture: texture path is null or blank.");
            return null;
        }

        Path BasePath = Folder.GetConfigFolder();

        if (TexturePath.startsWith("textures/oreville/ans/")) {
            BasePath = BasePath.resolve("textures").resolve("oreville").resolve("ans");
            TexturePath = TexturePath.replaceFirst("^textures/oreville/ans/", "");
        } else if (TexturePath.startsWith("textures/entity/")) {
            BasePath = BasePath.resolve("subpacks").resolve(JavaUnbound.SUB_PACK).resolve("textures").resolve("entity");
            TexturePath = TexturePath.replaceFirst("^textures/entity/", "");
        } else if (TexturePath.startsWith("textures/blocks/")) {
            BasePath = BasePath.resolve("subpacks").resolve(JavaUnbound.SUB_PACK).resolve("textures").resolve("blocks");
            TexturePath = TexturePath.replaceFirst("^textures/blocks/", "");
        } else if (TexturePath.startsWith("textures/items/")) {
            BasePath = BasePath.resolve("subpacks").resolve(JavaUnbound.SUB_PACK).resolve("textures").resolve("items");
            TexturePath = TexturePath.replaceFirst("^textures/items/", "");
        } else if (TexturePath.startsWith("textures/misc/")) {
            BasePath = BasePath.resolve("subpacks").resolve(JavaUnbound.SUB_PACK).resolve("textures").resolve("misc");
            TexturePath = TexturePath.replaceFirst("^textures/misc/", "");
        } else {
            JavaUnbound.LOGGER.warn("Unsupported attachable texture path: {} | Supported prefixes: textures/oreville/ans/, textures/entity, textures/blocks/", TexturePath);
            return null;
        }

        if (!TexturePath.endsWith(".png")) {
            TexturePath += ".png";
        }

        Path ResolvedPath = BasePath.resolve(TexturePath).normalize();

        if (!ResolvedPath.startsWith(BasePath.normalize())) {
            JavaUnbound.LOGGER.error("Attachable texture path escapes its base directory: {}", ResolvedPath);
            return null;
        }

        if (!Files.isRegularFile(ResolvedPath)) {
            JavaUnbound.LOGGER.error("Attachable texture file does not exist: {} | Original texture path: {}", ResolvedPath, TexturePath);
            return null;
        }

        JavaUnbound.LOGGER.debug("Resolved attachable texture: {} -> {}", TexturePath, ResolvedPath);
        return ResolvedPath;
    }

    private static String GetResourcePath(Path AbsolutePath) {
        if (AbsolutePath == null) {
            JavaUnbound.LOGGER.warn("Cannot get resource path: absolute path is null.");
            return null;
        }

        Path ConfigPath = Folder.GetConfigFolder().normalize();
        Path NormalizedPath = AbsolutePath.normalize();

        if (!NormalizedPath.startsWith(ConfigPath)) {
            JavaUnbound.LOGGER.debug("Texture path is outside the config folder: {}", AbsolutePath);
            return null;
        }

        String ResourcePath = ConfigPath.relativize(NormalizedPath).toString().replace("\\", "/");

        if (ResourcePath.startsWith("subpacks/" + JavaUnbound.SUB_PACK + "/textures/")) {
            ResourcePath = ResourcePath.replaceFirst("^subpacks/" + JavaUnbound.SUB_PACK + "/textures/", "textures/subpacks/" + JavaUnbound.SUB_PACK + "/");
        } else if (ResourcePath.startsWith("textures/")) {
            // Already in the correct resource path format.
        } else {
            JavaUnbound.LOGGER.warn("Unsupported absolute texture path: {}", AbsolutePath);
            return null;
        }

        return ResourcePath;
    }

    public static HashMap<String, AttachableTextureDefinition> GetAttachableTextures(Path JsonFile) {
        JavaUnbound.LOGGER.debug("Loading attachable textures from: {}", JsonFile);

        JsonObject TexturesElement = GetTextureElements(JsonFile);
        String Content;

        try {
            Content = Files.readString(JsonFile);
        } catch (IOException Exception) {
            JavaUnbound.LOGGER.error("Failed to read attachable JSON: {}", JsonFile, Exception);
            return new HashMap<>();
        }

        JsonElement IdentifierElement = GetJsonValue.GetValue(Content, "description.identifier");

        if (IdentifierElement == null || !IdentifierElement.isJsonPrimitive()) {
            JavaUnbound.LOGGER.error("Missing description.identifier in attachable JSON: {}", JsonFile);
            return new HashMap<>();
        }

        String AttachableIdentifier = IdentifierElement.getAsString();

        if (TexturesElement.size() == 0) {
            JavaUnbound.LOGGER.warn("No attachable textures loaded from: {}", JsonFile);
            return new HashMap<>();
        }

        HashMap<String, AttachableTextureDefinition> Textures = new HashMap<>();

        for (Map.Entry<String, JsonElement> Entry : TexturesElement.entrySet()) {
            String TextureIdentifier = Entry.getKey();
            JsonElement TextureElement = Entry.getValue();

            if (!TextureElement.isJsonPrimitive() || !TextureElement.getAsJsonPrimitive().isString()) {
                JavaUnbound.LOGGER.error("Invalid texture definition: {} | File: {} | Value: {}", TextureIdentifier, JsonFile, TextureElement);
                continue;
            }

            String TexturePath = TextureElement.getAsString();
            AttachableTextureDefinition TextureDefinition = new AttachableTextureDefinition();

            Path OriginalPath = GetTexturePath(TexturePath);
            Path CopiedPath = CopyTexture(OriginalPath, AttachableIdentifier, TexturePath);

            TextureDefinition.Identifier = TextureIdentifier;
            TextureDefinition.AbsolutePath = OriginalPath;
            TextureDefinition.ResourcePath = GetResourcePath(OriginalPath);

            if (TextureDefinition.AbsolutePath == null) {
                JavaUnbound.LOGGER.error("Failed to resolve texture: {} | Identifier: {} | File: {}", TexturePath, TextureIdentifier, JsonFile);
            } else {
                JavaUnbound.LOGGER.debug("Loaded attachable texture: {} | Identifier: {} | Path: {}", JsonFile, TextureIdentifier, TextureDefinition.AbsolutePath);
            }

            TextureDefinition.SetTextureIdentifier();
            Textures.put(TextureDefinition.Identifier, TextureDefinition);
        }

        JavaUnbound.LOGGER.debug("Finished loading attachable textures: {} | Loaded definitions: {}", JsonFile, Textures.size());

        return Textures;
    }
}