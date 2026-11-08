package com.java_unbound.loader.definitions.attachable;

import com.java_unbound.JavaUnbound;
import com.java_unbound.loader.definitions.attachable.resolvers.*;
import com.java_unbound.loader.definitions.attachable.types.geomtry.*;
import com.java_unbound.loader.definitions.attachable.types.render_controller.AttachableRenderControllerReference;
import com.java_unbound.loader.definitions.attachable.types.scripts.AttachableScriptsDefinition;
import com.java_unbound.loader.definitions.attachable.types.texture.*;
import com.java_unbound.loader.definitions.entity.EntityDefinition;
import com.java_unbound.loader.definitions.entity.types.geometry.EntityGeometryDefinition;
import com.java_unbound.loader.definitions.entity.types.render_controller.EntityRenderControllerReference;
import com.java_unbound.loader.resourcepack.Folder;
import com.java_unbound.molang.types.MolangAttachableParser;
import com.java_unbound.utils.variables.HashMaps;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

public class AttachableDefinition {
    public String Identifier;

    public HashMap<String, AttachableTextureDefinition> Textures;
    public HashMap<String, EntityGeometryDefinition> Geomtries;
    public List<EntityRenderControllerReference> RenderControllers;
    public AttachableScriptsDefinition Scripts;

    public HashMap<String, String> Materials;
    public HashMap<String, String> Animations;

    public static AttachableDefinition ResolveAttachable(Path JsonFile) {
        AttachableDefinition Definition = new AttachableDefinition();
        String AttachableIdentifer = AttachableStringResolver.GetAttachableString("identifier", JsonFile);

        try {
            Definition.Identifier = AttachableIdentifer;
            Definition.Textures = AttachableTextureResolver.GetAttachableTextures(JsonFile);
            Definition.Geomtries = AttachableGeometryResolver.GetAttachableGeometries(JsonFile);
            Definition.RenderControllers = AttachableRenderControllerResolver.GetAttachableRenderControllers(JsonFile);
            Definition.Scripts = AttachableScriptResolver.GetAttableScripts(JsonFile);

            return Definition;
        } catch (Exception Exception) {
            JavaUnbound.LOGGER.error("Failed to resolve attachable definition: {}", JsonFile, Exception);
            return null;
        }
    }

    public static void PreloadAttachableFiles() {
        Path AttachableFolder = Folder.GetConfigFolder().resolve("attachables");
        if (!Files.isDirectory(AttachableFolder)) {return;}

        try (Stream<Path> Paths = Files.list(AttachableFolder)) {
            Paths.filter(Files::isRegularFile)
                    .filter(Path -> Path.toString().endsWith(".json"))
                    .forEach(JsonFile -> {
                        AttachableDefinition Definition = ResolveAttachable(JsonFile);

                        if (Definition != null && Definition.Identifier != null) {
                            HashMaps.Attachables.put(Definition.Identifier, Definition);
                        }
                    });

        } catch (IOException Exception) {
            throw new RuntimeException("Failed to preload attachable definitions from: " + AttachableFolder, Exception);
        }
    }

    public static AttachableDefinition GetAttachableDefinition(ItemStack Stack) {
        return GetAttachableDefinition(Stack.getItem());
    }

    public static AttachableDefinition GetAttachableDefinition(Item Item) {
        var Key = BuiltInRegistries.ITEM.getKey(Item);

        return Key != null ? HashMaps.Attachables.get(Key.toString()) : null;
    }

    public static AttachableDefinition GetAttachableDefinition(MolangAttachableParser.Context Context) {
        return HashMaps.Attachables.get(Context.ItemId);
    }
}
