package com.java_unbound.attachables;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.java_unbound.JavaUnbound;
import com.java_unbound.attachables.resolvers.AttachableGeometryResolver;
import com.java_unbound.attachables.resolvers.AttachableRenderControllerResolver;
import com.java_unbound.attachables.resolvers.AttachableTextureResolver;
import com.java_unbound.shared.definitions.attachables.AttachableDefinition;
import com.java_unbound.shared.resourcepack.Folder;
import com.java_unbound.shared.utilities.JsonUtilities;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class AttachableResolver {
    private static void ResolveAttachable(Path AttachablePath) {
        AttachableDefinition Definition = new AttachableDefinition();

        String Identifier = JsonUtilities.GetString("minecraft:attachable.description.identifier", AttachablePath);

        if (Identifier == null) {
            JavaUnbound.LOGGER.error("Could not find attachable description.identifier for " + AttachablePath.toString());
            return;
        }

        Definition.Identifier = Identifier;
        Definition.Textures = AttachableTextureResolver.ResolveTextures(AttachablePath);
        Definition.Geometries = AttachableGeometryResolver.ResolveGeometries(AttachablePath);
        Definition.RenderControllers = AttachableRenderControllerResolver.ResolveRenderControllers(AttachablePath);
    }

    public static void ResolveAttachables() {
        Path ConfigFolder = Folder.GetAddonFolder();

        if (!ConfigFolder.toFile().exists()) {
            JavaUnbound.LOGGER.error("Config folder is null");
            return;
        }

        Path SubpackAttachableFolder = ConfigFolder.resolve("subpacks").resolve(JavaUnbound.SUB_PACK).resolve("attachables");
        Path BaseAttachableFolder = ConfigFolder.resolve("attachables");

        try (Stream<Path> Paths = Files.list(SubpackAttachableFolder)) {
            Paths.filter(Files::isRegularFile)
                    .filter(Path -> Path.toString().endsWith(".json"))
                    .forEach(JsonFile -> {
                        ResolveAttachable(SubpackAttachableFolder.resolve(JsonFile));
                    });
        } catch (IOException Exception) {
            throw new RuntimeException("Failed to preload attachable definition" , Exception);
        }

        try (Stream<Path> Paths = Files.list(BaseAttachableFolder)) {
            Paths.filter(Files::isRegularFile)
                    .filter(Path -> Path.toString().endsWith(".json"))
                    .forEach(JsonFile -> {
                        ResolveAttachable(BaseAttachableFolder.resolve(JsonFile));
                    });
        } catch (IOException Exception) {
            throw new RuntimeException("Failed to preload attachable definition" , Exception);
        }
    }
}
