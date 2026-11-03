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
        EntityRendererRegistration.AddEntity(EntityTypes.COW, Entities::RegisterEntity);
        EntityRendererRegistration.AddEntity(EntityTypes.SPIDER, Entities::RegisterEntity);
        EntityRendererRegistration.AddEntity(EntityTypes.SKELETON, Entities::RegisterEntity);
        EntityRendererRegistration.AddEntity(EntityTypes.CREEPER, Entities::RegisterEntity);
        EntityRendererRegistration.AddEntity(EntityTypes.ALLAY, Entities::RegisterEntity);
        EntityRendererRegistration.AddEntity(EntityTypes.SNOW_GOLEM, Entities::RegisterEntity);
        EntityRendererRegistration.AddEntity(EntityTypes.PIG, Entities::RegisterEntity);
        EntityRendererRegistration.AddEntity(EntityTypes.TURTLE, Entities::RegisterEntity);
        EntityRendererRegistration.AddEntity(EntityTypes.BEE, Entities::RegisterEntity);
        EntityRendererRegistration.AddEntity(EntityTypes.HAPPY_GHAST, Entities::RegisterEntity);
        EntityRendererRegistration.AddEntity(EntityTypes.AXOLOTL, Entities::RegisterEntity);
        EntityRendererRegistration.AddEntity(EntityTypes.CAMEL, Entities::RegisterEntity);
        EntityRendererRegistration.AddEntity(EntityTypes.CHICKEN, Entities::RegisterEntity);
        EntityRendererRegistration.AddEntity(EntityTypes.BLAZE, Entities::RegisterEntity);
        EntityRendererRegistration.AddEntity(EntityTypes.ARMOR_STAND, Entities::RegisterEntity);
        EntityRendererRegistration.AddEntity(EntityTypes.WARDEN, Entities::RegisterEntity);
    }

    /*public static void LoadEntities() {
        LoadAllEntities();
    }*/

    public static void LoadAllEntities() {
        BuiltInRegistries.ENTITY_TYPE.forEach(Type -> {
            if (Type.getCategory() != MobCategory.MISC || Type == EntityTypes.ARMOR_STAND) {
                if (EntityDefinition.GetEntityDefinition(Type) != null) {
                    EntityRendererRegistration.AddEntity(Type, Entities::RegisterEntity);
                }
            }
        });
    }

    public static List<EntityRenderPass> RegisterEntity(Entity Entity) {
        return GetRenderingData.GetRenderingData(Entity);
    }
}