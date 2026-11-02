package com.java_unbound.entities.geckolib;

import com.java_unbound.entities.geckolib.entities.chicken.ReplacedChickenRenderer;
import com.java_unbound.entities.geckolib.entities.copper_golem.ReplacedCopperGolemRenderer;
import com.java_unbound.entities.geckolib.entities.cow.ReplacedCowRenderer;
import com.java_unbound.entities.geckolib.entities.creeper.ReplacedCreeperRenderer;
import com.java_unbound.entities.geckolib.entities.slime.ReplacedSlimeRenderer;

import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.world.entity.EntityTypes;

public class EntityRendererRegistration {
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void RegisterEntityRenderer() {
        EntityRenderers.register(EntityTypes.CREEPER, ReplacedCreeperRenderer::new);
        EntityRenderers.register(EntityTypes.COW, ReplacedCowRenderer::new);
        EntityRenderers.register(EntityTypes.CHICKEN, ReplacedChickenRenderer::new);
        EntityRenderers.register(EntityTypes.COPPER_GOLEM, ReplacedCopperGolemRenderer::new);
        EntityRenderers.register(EntityTypes.SLIME, ReplacedSlimeRenderer::new);
    }
}