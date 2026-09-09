package com.java_unbound.loader.attachables;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.java_unbound.JavaUnbound;
import com.java_unbound.loader.definitions.Geometries;
import com.java_unbound.loader.definitions.RenderControllers;
import com.java_unbound.loader.resourcepack.Folder;
import com.java_unbound.utils.functions.GetJsonFilesInPath;
import com.java_unbound.utils.functions.GetJsonValue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AttachableFileReader {
    private AttachableFileReader() {

    }

    private static final Path SubpackFolder = Folder.GetResourceFolder().resolve("subpacks").resolve(JavaUnbound.SUBPACK);

    private static final Path BaseAttachablesFolder = Folder.GetResourceFolder().resolve("attachables");
    private static final Path SubpackAttachablesFolder = SubpackFolder.resolve("attachables");

    public static void Read() {
        Map<String, Path> AttachableFiles = new HashMap<>();

        AddAttachableFiles(AttachableFiles, SubpackAttachablesFolder);
        AddAttachableFiles(AttachableFiles, BaseAttachablesFolder);


        for (Path File : AttachableFiles.values()) {
            try {
                ReadAttachableFile(File);
            } catch (IOException Exception) {
                JavaUnbound.LOGGER.error("Failed to read attachable file: {}", File, Exception);
            }
        }
    }


    private static void AddAttachableFiles(Map<String, Path> AttachableFiles, Path AttachableFolder) {
        if (!Files.isDirectory(AttachableFolder)) {
            return;
        }

        List<Path> FilesInFolder = GetJsonFilesInPath.GetDescendant(AttachableFolder);

        for (Path File : FilesInFolder) {
            String RelativePath = AttachableFolder.relativize(File).toString();

            if (AttachableFiles.containsKey(RelativePath)) {
                continue;
            }

            AttachableFiles.put(RelativePath, File);
        }
    }


    private static JsonElement GetAttachableIdentifier(String Content) {
        return GetJsonValue.GetValue(Content, "description.identifier");
    }

    private static JsonElement GetAttachableMaterials(String Content) {
        return GetJsonValue.GetValue(Content, "description.materials");
    }

    private static JsonElement GetAttachableTextures(String Content) {
        return GetJsonValue.GetValue(Content, "description.textures");
    }

    private static JsonElement GetAttachableGeometry(String Content) {
        return GetJsonValue.GetValue(Content, "description.geometry");
    }

    private static JsonElement GetAttachableAnimations(String Content) {
        return GetJsonValue.GetValue(Content, "description.animations");
    }

    private static JsonElement GetAttachableScripts(String Content) {
        return GetJsonValue.GetValue(Content, "description.scripts");
    }

    private static JsonElement GetAttachableRenderControllers(String Content) {
        return GetJsonValue.GetValue(Content, "description.render_controllers");
    }


    public static void ReadAttachableFile(Path File) throws IOException {
        String Content = Files.readString(File);

        JsonElement JsonIdentifier = GetAttachableIdentifier(Content);
        JsonElement JsonMaterials = GetAttachableMaterials(Content);
        JsonElement JsonTextures = GetAttachableTextures(Content);
        JsonElement JsonGeometries = GetAttachableGeometry(Content);
        JsonElement JsonAnimations = GetAttachableAnimations(Content);
        JsonElement JsonScripts = GetAttachableScripts(Content);
        JsonElement JsonRenderControllers = GetAttachableRenderControllers(Content);

        JsonElement TexturePaths = AttachableTextureResolver.ResolveAttachableTexture(JsonTextures);
        List<JsonElement> ResolvedGeometries = Geometries.GetGeometries(JsonGeometries);
        List<JsonElement> ResolvedRenderControllers = RenderControllers.GetRenderControllers(JsonRenderControllers);

        AttachableDefinition Definition = new AttachableDefinition();
        Definition.Identifier = JsonIdentifier.getAsString();
        Definition.Materials = JsonMaterials;
        Definition.Textures = TexturePaths;
        Definition.Geometries = ResolvedGeometries;
        Definition.Animations = JsonAnimations;
        Definition.Scripts = JsonScripts;
        Definition.RenderControllers = ResolvedRenderControllers;
        Definition.Content = Content;

        Definition.Debug();

        AttachableRegistry.Register(Definition);
    }
}