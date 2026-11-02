package com.java_unbound.entities.geckolib;

import com.java_unbound.entities.geckolib.entities.chicken.ReplacedChickenRenderer;
import com.java_unbound.entities.geckolib.entities.copper_golem.ReplacedCopperGolemRenderer;
import com.java_unbound.entities.geckolib.entities.cow.ReplacedCowRenderer;
import com.java_unbound.entities.geckolib.entities.creeper.ReplacedCreeperRenderer;

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

public final class EntityRendererRegistration {
    private record EntityRendererDefinition(
            List<EntityRenderPass> Passes,
            float ScaleWidth,
            float ScaleHeight
    ) {
        private EntityRendererDefinition {
            Passes = List.copyOf(Passes);
        }
    }

    private static final Map<EntityType<?>, EntityRendererDefinition> RENDERERS = new LinkedHashMap<>();

    private EntityRendererRegistration() {
    }

    static {
        AddEntity(
                EntityTypes.SLIME,
                2.0f,
                2.0f,
                new EntityRenderPass(
                        Identifier.fromNamespaceAndPath("minecraft", "geometry.oreville_ans.igrcna"),
                        Identifier.fromNamespaceAndPath("minecraft", "textures/oreville/ans/cmh.png")
                )
        );
    }

    public static void RegisterEntityRenderer() {
        RegisterExistingRenderers();

        for (Map.Entry<EntityType<?>, EntityRendererDefinition> Entry : RENDERERS.entrySet()) {
            RegisterEntity(Entry.getKey(), Entry.getValue());
        }
    }

    public static void AddEntity(
            EntityType<?> Type,
            EntityRenderPass... Passes
    ) {
        AddEntity(Type, 1.0f, 1.0f, Passes);
    }

    public static void AddEntity(
            EntityType<?> Type,
            float ScaleWidth,
            float ScaleHeight,
            EntityRenderPass... Passes
    ) {
        if (Type == null) {
            throw new IllegalArgumentException("Entity type cannot be null.");
        }

        if (Passes == null || Passes.length == 0) {
            throw new IllegalArgumentException("At least one geometry/texture pair is required.");
        }

        if (RENDERERS.containsKey(Type)) {
            throw new IllegalArgumentException(
                    "Entity renderer already registered: " + BuiltInRegistries.ENTITY_TYPE.getKey(Type)
            );
        }

        RENDERERS.put(
                Type,
                new EntityRendererDefinition(List.of(Passes), ScaleWidth, ScaleHeight)
        );
    }

    public static List<EntityRenderPass> GetRenderPasses(EntityType<?> Type) {
        EntityRendererDefinition Definition = RENDERERS.get(Type);

        return Definition != null ? Definition.Passes() : List.of();
    }

    private static void RegisterExistingRenderers() {
        EntityRenderers.register(EntityTypes.CREEPER, ReplacedCreeperRenderer::new);
        EntityRenderers.register(EntityTypes.COW, ReplacedCowRenderer::new);
        EntityRenderers.register(EntityTypes.CHICKEN, ReplacedChickenRenderer::new);
        EntityRenderers.register(EntityTypes.COPPER_GOLEM, ReplacedCopperGolemRenderer::new);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void RegisterEntity(
            EntityType<?> Type,
            EntityRendererDefinition Definition
    ) {
        EntityRenderers.register(
                (EntityType) Type,
                Context -> new GenericReplacedEntityRenderer(
                        Context,
                        Definition.Passes(),
                        Definition.ScaleWidth(),
                        Definition.ScaleHeight()
                )
        );
    }
}
