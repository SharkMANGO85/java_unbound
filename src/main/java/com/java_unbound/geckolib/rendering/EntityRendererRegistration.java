package com.java_unbound.geckolib.rendering;

import com.java_unbound.geckolib.entity.EntityRenderPass;
import com.java_unbound.geckolib.entity.GenericReplacedEntityRenderer;

import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class EntityRendererRegistration {
    private record EntityRendererDefinition(Function<Entity, List<EntityRenderPass>> PassProvider, float ScaleWidth, float ScaleHeight) {}

    private static final Map<EntityType<?>, EntityRendererDefinition> RENDERERS = new LinkedHashMap<>();

    public static void RegisterEntityRenderer() {
        Entities.LoadEntities();

        for (Map.Entry<EntityType<?>, EntityRendererDefinition> Entry : RENDERERS.entrySet()) {
            RegisterEntity(Entry.getKey(), Entry.getValue());
        }
    }

    public static void AddEntity(EntityType<?> Type, Function<Entity, List<EntityRenderPass>> PassProvider) {
        AddEntity(Type, 1.0f, 1.0f, PassProvider);
    }

    public static void AddEntity(EntityType<?> Type, float ScaleWidth, float ScaleHeight, Function<Entity, List<EntityRenderPass>> PassProvider) {
        if (Type == null || PassProvider == null) {
            throw new IllegalArgumentException("Entity type and pass provider cannot be null.");
        }

        if (RENDERERS.containsKey(Type)) {
            throw new IllegalArgumentException("Entity renderer already registered: " + BuiltInRegistries.ENTITY_TYPE.getKey(Type));
        }

        RENDERERS.put(Type, new EntityRendererDefinition(PassProvider, ScaleWidth, ScaleHeight));
    }

    public static List<EntityRenderPass> GetRenderPasses(Entity Entity) {
        if (Entity == null) {return List.of();}

        EntityRendererDefinition Definition = RENDERERS.get(Entity.getType());

        return Definition != null ? Definition.PassProvider().apply(Entity) : List.of();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void RegisterEntity(EntityType<?> Type, EntityRendererDefinition Definition) {
        EntityRenderers.register((EntityType) Type, Context -> new GenericReplacedEntityRenderer<>(Context, Definition.PassProvider(), Definition.ScaleWidth(), Definition.ScaleHeight()));
    }
}