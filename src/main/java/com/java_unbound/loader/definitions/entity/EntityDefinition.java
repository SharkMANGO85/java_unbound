package com.java_unbound.loader.definitions.entity;

import com.java_unbound.JavaUnbound;
import com.java_unbound.loader.definitions.entity.resolvers.*;
import com.java_unbound.loader.definitions.entity.types.animation.EntityAnimationDefinition;
import com.java_unbound.loader.definitions.entity.types.geometry.EntityGeometryDefinition;
import com.java_unbound.loader.definitions.entity.types.render_controller.EntityRenderControllerReference;
import com.java_unbound.loader.definitions.entity.types.scripts.EntityScriptsDefinition;
import com.java_unbound.loader.definitions.entity.types.spawn_egg.EntitySpawnEggDefinition;
import com.java_unbound.loader.definitions.entity.types.texture.EntityTextureDefinition;
import com.java_unbound.loader.resourcepack.Folder;
import com.java_unbound.utils.variables.HashMaps;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

public class EntityDefinition {
    public String Identifier;

    public boolean EnableAttachables;
    public boolean HeldItemIgnoresLighting;
    public boolean HideArmor;

    public HashMap<String, EntityGeometryDefinition> Geometries;
    public HashMap<String, EntityTextureDefinition> Textures;
    public List<EntityRenderControllerReference> RenderControllers;
    public EntityScriptsDefinition Scripts;
    public HashMap<String, String> Materials;

    public List<String> AnimationControllers;
    public HashMap<String, EntityAnimationDefinition> Animations;

    public HashMap<String, String> SoundEffects;
    public HashMap<String, String> ParticleEffects;

    public EntitySpawnEggDefinition SpawnEgg;

    public static EntityDefinition ResolveEntity(Path JsonFile) {
        EntityDefinition Definition = new EntityDefinition();
        String EntityIdentifer = EntityStringResolver.GetEntityString("identifier", JsonFile);

        if (EntityIdentifer.equals("minecraft:villager_v2")) {
            EntityIdentifer = "minecraft:villager";
        }

        try {
            Definition.Identifier = EntityIdentifer;
            Definition.Geometries = EntityGeometryResolver.GetEntityGeometries(JsonFile);
            Definition.Textures = EntityTextureResolver.GetEntityTextures(JsonFile);
            Definition.RenderControllers = EntityRenderControllerResolver.GetEntityRenderControllers(JsonFile);
            Definition.Scripts = EntityScriptsResolver.GetEntityScripts(JsonFile);
            Definition.SpawnEgg = EntitySpawnEggResolver.GetEntitySpawnEgg(JsonFile);
            Definition.EnableAttachables = EntityBooleanResolver.GetEntityBoolean("enable_attachables", JsonFile);
            Definition.HeldItemIgnoresLighting = EntityBooleanResolver.GetEntityBoolean("held_item_ignores_lighting", JsonFile);
            Definition.HideArmor = EntityBooleanResolver.GetEntityBoolean("hide_armor", JsonFile);
            Definition.Animations = EntityAnimationResolver.GetEntityAnimations(JsonFile);

            return Definition;
        } catch (Exception Exception) {
            JavaUnbound.LOGGER.error("Failed to resolve entity definition: {}", JsonFile, Exception);
            return null;
        }
    }

    public static void PreloadEntityFiles() {
        Path EntityFolder = Folder.GetConfigFolder().resolve("entity");
        if (!Files.isDirectory(EntityFolder)) {return;}

        try (Stream<Path> Paths = Files.list(EntityFolder)) {
            Paths.filter(Files::isRegularFile)
                    .filter(Path -> Path.toString().endsWith(".json"))
                    .forEach(JsonFile -> {
                        EntityDefinition Definition = ResolveEntity(JsonFile);

                        if (Definition != null && Definition.Identifier != null) {
                            HashMaps.Entities.put(Definition.Identifier, Definition);
                        }
                    });

        } catch (IOException Exception) {
            throw new RuntimeException("Failed to preload entity definitions from: " + EntityFolder, Exception);
        }
    }

    public static EntityDefinition GetEntityDefinition(Entity Entity) {
        return GetEntityDefinition(Entity.getType());
    }

    public static EntityDefinition GetEntityDefinition(EntityType<?> Type) {
        var Key = BuiltInRegistries.ENTITY_TYPE.getKey(Type);

        return Key != null ? HashMaps.Entities.get(Key.toString()) : null;
    }
}
