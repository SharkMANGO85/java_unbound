package com.java_unbound.entities.geckolib.entities;

import com.java_unbound.entities.geckolib.EntityRendererRegistration;
import com.java_unbound.loader.gecko_lib.entity.EntityRenderPass;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;

public class Happy_Ghast {
    public static void RegisterEntity(Entity Entity) {
        EntityRendererRegistration.AddEntity(EntityTypes.HAPPY_GHAST,
                new EntityRenderPass(Identifier.fromNamespaceAndPath("minecraft", "geometry.oreville_ans.gsiepj"), Identifier.fromNamespaceAndPath("minecraft", "textures/oreville/ans/csv.png")));
    }
}