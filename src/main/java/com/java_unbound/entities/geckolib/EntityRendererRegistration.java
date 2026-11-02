package com.java_unbound.entities.geckolib;

import com.java_unbound.loader.gecko_lib.entity.EntityRenderPass;
import com.java_unbound.loader.gecko_lib.entity.GenericReplacedEntityRenderer;

import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class EntityRendererRegistration {
    public EntityRendererRegistration() {}

    private record EntityRendererDefinition(List<EntityRenderPass> Passes, float ScaleWidth, float ScaleHeight) {
        private EntityRendererDefinition {
            Passes = List.copyOf(Passes);
        }
    }

    private static final Map<EntityType<?>, EntityRendererDefinition> RENDERERS = new LinkedHashMap<>();

    public static void RegisterEntityRenderer() {
        Entities.LoadEntities();

        for (Map.Entry<EntityType<?>, EntityRendererDefinition> Entry : RENDERERS.entrySet()) {
            RegisterEntity(Entry.getKey(), Entry.getValue());
        }
    }

    public static void AddEntity(EntityType<?> Type, EntityRenderPass... Passes) {
        AddEntity(Type, 1.0f, 1.0f, Passes);
    }

    public static void AddEntity(EntityType<?> Type, float ScaleWidth, float ScaleHeight, EntityRenderPass... Passes) {
        if (Type == null) {
            throw new IllegalArgumentException("Entity type cannot be null.");
        }

        if (Passes == null || Passes.length == 0) {
            throw new IllegalArgumentException("At least one geometry/texture pair is required.");
        }

        if (RENDERERS.containsKey(Type)) {
            throw new IllegalArgumentException("Entity renderer already registered: " + BuiltInRegistries.ENTITY_TYPE.getKey(Type));
        }

        RENDERERS.put(Type, new EntityRendererDefinition(List.of(Passes), ScaleWidth, ScaleHeight));
    }

    public static List<EntityRenderPass> GetRenderPasses(EntityType<?> Type) {
        EntityRendererDefinition Definition = RENDERERS.get(Type);

        return Definition != null ? Definition.Passes() : List.of();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void RegisterEntity(EntityType<?> Type, EntityRendererDefinition Definition) {
        EntityRenderers.register((EntityType) Type, Context -> new GenericReplacedEntityRenderer(Context, Definition.Passes(), Definition.ScaleWidth(), Definition.ScaleHeight()));
    }
}