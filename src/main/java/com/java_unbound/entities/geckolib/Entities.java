package com.java_unbound.entities.geckolib;

import com.java_unbound.loader.definitions.entity.EntityDefinition;
import com.java_unbound.loader.gecko_lib.entity.EntityRenderPass;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;

import java.util.List;

public class Entities {
    public static void LoadEntities() {
        EntityRendererRegistration.AddEntity(EntityTypes.ALLAY, Entities::RegisterEntity);
        EntityRendererRegistration.AddEntity(EntityTypes.ARMADILLO, Entities::RegisterEntity);
        EntityRendererRegistration.AddEntity(EntityTypes.ARMOR_STAND, Entities::RegisterEntity);
        EntityRendererRegistration.AddEntity(EntityTypes.AXOLOTL, Entities::RegisterEntity);
        EntityRendererRegistration.AddEntity(EntityTypes.BAT, Entities::RegisterEntity);
        EntityRendererRegistration.AddEntity(EntityTypes.BEE, Entities::RegisterEntity);
        EntityRendererRegistration.AddEntity(EntityTypes.BLAZE, Entities::RegisterEntity);
        EntityRendererRegistration.AddEntity(EntityTypes.BOGGED, Entities::RegisterEntity);
        EntityRendererRegistration.AddEntity(EntityTypes.BREEZE, Entities::RegisterEntity);
        EntityRendererRegistration.AddEntity(EntityTypes.CAMEL, Entities::RegisterEntity);
        EntityRendererRegistration.AddEntity(EntityTypes.CAT, Entities::RegisterEntity);
        EntityRendererRegistration.AddEntity(EntityTypes.CAVE_SPIDER, Entities::RegisterEntity);
        EntityRendererRegistration.AddEntity(EntityTypes.CHICKEN, Entities::RegisterEntity);
        EntityRendererRegistration.AddEntity(EntityTypes.COD, Entities::RegisterEntity);
        EntityRendererRegistration.AddEntity(EntityTypes.COW, Entities::RegisterEntity);
        EntityRendererRegistration.AddEntity(EntityTypes.COPPER_GOLEM, Entities::RegisterEntity);
        EntityRendererRegistration.AddEntity(EntityTypes.CREEPER, Entities::RegisterEntity);
        EntityRendererRegistration.AddEntity(EntityTypes.DOLPHIN, Entities::RegisterEntity);
        EntityRendererRegistration.AddEntity(EntityTypes.DONKEY, Entities::RegisterEntity);
        EntityRendererRegistration.AddEntity(EntityTypes.DROWNED, Entities::RegisterEntity);
        EntityRendererRegistration.AddEntity(EntityTypes.ELDER_GUARDIAN, Entities::RegisterEntity);
        EntityRendererRegistration.AddEntity(EntityTypes.ENDERMAN, Entities::RegisterEntity);
        EntityRendererRegistration.AddEntity(EntityTypes.ENDERMITE, Entities::RegisterEntity);
        EntityRendererRegistration.AddEntity(EntityTypes.EVOKER, Entities::RegisterEntity);
        EntityRendererRegistration.AddEntity(EntityTypes.FOX, Entities::RegisterEntity);
        EntityRendererRegistration.AddEntity(EntityTypes.FROG, Entities::RegisterEntity);
        EntityRendererRegistration.AddEntity(EntityTypes.GHAST, Entities::RegisterEntity);
        EntityRendererRegistration.AddEntity(EntityTypes.GIANT, Entities::RegisterEntity);
        EntityRendererRegistration.AddEntity(EntityTypes.GLOW_SQUID, Entities::RegisterEntity);
        EntityRendererRegistration.AddEntity(EntityTypes.GOAT, Entities::RegisterEntity);
        EntityRendererRegistration.AddEntity(EntityTypes.GUARDIAN, Entities::RegisterEntity);
        EntityRendererRegistration.AddEntity(EntityTypes.HAPPY_GHAST, Entities::RegisterEntity);
        EntityRendererRegistration.AddEntity(EntityTypes.HOGLIN, Entities::RegisterEntity);
        EntityRendererRegistration.AddEntity(EntityTypes.HORSE, Entities::RegisterEntity);
        EntityRendererRegistration.AddEntity(EntityTypes.HUSK, Entities::RegisterEntity);
        EntityRendererRegistration.AddEntity(EntityTypes.ILLUSIONER, Entities::RegisterEntity);
        EntityRendererRegistration.AddEntity(EntityTypes.IRON_GOLEM, Entities::RegisterEntity);
        EntityRendererRegistration.AddEntity(EntityTypes.LLAMA, Entities::RegisterEntity);
        EntityRendererRegistration.AddEntity(EntityTypes.MAGMA_CUBE, Entities::RegisterEntity);
        EntityRendererRegistration.AddEntity(EntityTypes.MOOSHROOM, Entities::RegisterEntity);
        EntityRendererRegistration.AddEntity(EntityTypes.MULE, Entities::RegisterEntity);
        EntityRendererRegistration.AddEntity(EntityTypes.OCELOT, Entities::RegisterEntity);
        EntityRendererRegistration.AddEntity(EntityTypes.PANDA, Entities::RegisterEntity);
        EntityRendererRegistration.AddEntity(EntityTypes.PARROT, Entities::RegisterEntity);
        EntityRendererRegistration.AddEntity(EntityTypes.PHANTOM, Entities::RegisterEntity);
        EntityRendererRegistration.AddEntity(EntityTypes.PIG, Entities::RegisterEntity);
        EntityRendererRegistration.AddEntity(EntityTypes.PIGLIN, Entities::RegisterEntity);
        EntityRendererRegistration.AddEntity(EntityTypes.PIGLIN_BRUTE, Entities::RegisterEntity);
        EntityRendererRegistration.AddEntity(EntityTypes.PILLAGER, Entities::RegisterEntity);
        EntityRendererRegistration.AddEntity(EntityTypes.POLAR_BEAR, Entities::RegisterEntity);
        EntityRendererRegistration.AddEntity(EntityTypes.PUFFERFISH, Entities::RegisterEntity);
        EntityRendererRegistration.AddEntity(EntityTypes.RABBIT, Entities::RegisterEntity);
        EntityRendererRegistration.AddEntity(EntityTypes.RAVAGER, Entities::RegisterEntity);
        EntityRendererRegistration.AddEntity(EntityTypes.SALMON, Entities::RegisterEntity);
        EntityRendererRegistration.AddEntity(EntityTypes.SHEEP, Entities::RegisterEntity);
        EntityRendererRegistration.AddEntity(EntityTypes.SHULKER, Entities::RegisterEntity);
        EntityRendererRegistration.AddEntity(EntityTypes.SILVERFISH, Entities::RegisterEntity);
        EntityRendererRegistration.AddEntity(EntityTypes.SKELETON, Entities::RegisterEntity);
        EntityRendererRegistration.AddEntity(EntityTypes.SKELETON_HORSE, Entities::RegisterEntity);
        EntityRendererRegistration.AddEntity(EntityTypes.SLIME, Entities::RegisterEntity);
        EntityRendererRegistration.AddEntity(EntityTypes.SNIFFER, Entities::RegisterEntity);
        EntityRendererRegistration.AddEntity(EntityTypes.SNOW_GOLEM, Entities::RegisterEntity);
        EntityRendererRegistration.AddEntity(EntityTypes.SPIDER, Entities::RegisterEntity);
        EntityRendererRegistration.AddEntity(EntityTypes.SQUID, Entities::RegisterEntity);
        EntityRendererRegistration.AddEntity(EntityTypes.STRAY, Entities::RegisterEntity);
        EntityRendererRegistration.AddEntity(EntityTypes.STRIDER, Entities::RegisterEntity);
        EntityRendererRegistration.AddEntity(EntityTypes.TADPOLE, Entities::RegisterEntity);
        EntityRendererRegistration.AddEntity(EntityTypes.TRADER_LLAMA, Entities::RegisterEntity);
        EntityRendererRegistration.AddEntity(EntityTypes.TROPICAL_FISH, Entities::RegisterEntity);
        EntityRendererRegistration.AddEntity(EntityTypes.TURTLE, Entities::RegisterEntity);
        EntityRendererRegistration.AddEntity(EntityTypes.VEX, Entities::RegisterEntity);
        EntityRendererRegistration.AddEntity(EntityTypes.VILLAGER, Entities::RegisterEntity);
        EntityRendererRegistration.AddEntity(EntityTypes.VINDICATOR, Entities::RegisterEntity);
        EntityRendererRegistration.AddEntity(EntityTypes.WANDERING_TRADER, Entities::RegisterEntity);
        EntityRendererRegistration.AddEntity(EntityTypes.WARDEN, Entities::RegisterEntity);
        EntityRendererRegistration.AddEntity(EntityTypes.WITCH, Entities::RegisterEntity);
        EntityRendererRegistration.AddEntity(EntityTypes.WITHER, Entities::RegisterEntity);
        EntityRendererRegistration.AddEntity(EntityTypes.WITHER_SKELETON, Entities::RegisterEntity);
        EntityRendererRegistration.AddEntity(EntityTypes.WOLF, Entities::RegisterEntity);
        EntityRendererRegistration.AddEntity(EntityTypes.ZOGLIN, Entities::RegisterEntity);
        EntityRendererRegistration.AddEntity(EntityTypes.ZOMBIE, Entities::RegisterEntity);
        EntityRendererRegistration.AddEntity(EntityTypes.ZOMBIE_HORSE, Entities::RegisterEntity);
        EntityRendererRegistration.AddEntity(EntityTypes.ZOMBIE_VILLAGER, Entities::RegisterEntity);
        EntityRendererRegistration.AddEntity(EntityTypes.ZOMBIFIED_PIGLIN, Entities::RegisterEntity);
        EntityRendererRegistration.AddEntity(EntityTypes.CAMEL_HUSK, Entities::RegisterEntity);
        EntityRendererRegistration.AddEntity(EntityTypes.CREAKING, Entities::RegisterEntity);
        EntityRendererRegistration.AddEntity(EntityTypes.ENDER_DRAGON, Entities::RegisterEntity);
        EntityRendererRegistration.AddEntity(EntityTypes.NAUTILUS, Entities::RegisterEntity);
        EntityRendererRegistration.AddEntity(EntityTypes.PARCHED, Entities::RegisterEntity);
        EntityRendererRegistration.AddEntity(EntityTypes.SULFUR_CUBE, Entities::RegisterEntity);
        EntityRendererRegistration.AddEntity(EntityTypes.ZOMBIE_NAUTILUS, Entities::RegisterEntity);
    }

    /*public static void LoadEntities() {
        LoadAllEntities();
    }*/

    /*public static void LoadAllEntities() {
        BuiltInRegistries.ENTITY_TYPE.forEach(Type -> {
            if (LivingEntity.class.isAssignableFrom(Type.getBaseClass())) {
                if (EntityDefinition.GetEntityDefinition(Type) != null) {
                    EntityRendererRegistration.AddEntity(Type, Entities::RegisterEntity);
                }
            }
        });
    }*/

    public static List<EntityRenderPass> RegisterEntity(Entity Entity) {
        return GetRenderingData.GetRenderingData(Entity);
    }
}