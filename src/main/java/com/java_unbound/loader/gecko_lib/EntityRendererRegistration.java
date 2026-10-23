package com.java_unbound.loader.gecko_lib;

import com.java_unbound.loader.gecko_lib.entity.GenericReplacedEntityRenderer;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;

public class EntityRendererRegistration {
    public static void RegisterEntityRenderer() {
        RegisterEntity(EntityTypes.FOX, "fox", "fox.png");
        RegisterEntity(EntityTypes.CREEPER, "creeper", "creeper.png");
        RegisterEntity(EntityTypes.SNOW_GOLEM, "snow_golem", "snow_golem.png");
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void RegisterEntity(EntityType<?> entityType, String model, String texture) {
        Identifier modelIdentifier = Identifier.fromNamespaceAndPath("minecraft", "entity/" + model);
        Identifier textureIdentifier = Identifier.fromNamespaceAndPath("minecraft", "textures/entity/" + texture);
        Identifier animationIdentifier = Identifier.fromNamespaceAndPath("minecraft", "animations/entity/" + model);

        EntityRenderers.register((EntityType) entityType, context -> new GenericReplacedEntityRenderer(context, modelIdentifier, textureIdentifier, animationIdentifier));
    }
}