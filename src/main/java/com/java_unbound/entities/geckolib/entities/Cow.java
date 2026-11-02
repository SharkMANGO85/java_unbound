package com.java_unbound.entities.geckolib.entities;

import com.java_unbound.JavaUnbound;
import com.java_unbound.loader.gecko_lib.entity.EntityRenderPass;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;

import java.util.List;

public class Cow {
    public static List<EntityRenderPass> RegisterEntity(Entity Entity) {
        JavaUnbound.LOGGER.info("Registering Cow renderer for: {}", Entity.getUUID());

        return List.of(
                new EntityRenderPass(Identifier.fromNamespaceAndPath("minecraft", "geometry.oreville_ans.syguii"), Identifier.fromNamespaceAndPath("minecraft", "textures/oreville/ans/day.png")),
                new EntityRenderPass(Identifier.fromNamespaceAndPath("minecraft", "geometry.oreville_ans.dnwuwm"), Identifier.fromNamespaceAndPath("minecraft", "textures/oreville/ans/dam.png"))
        );
    }
}