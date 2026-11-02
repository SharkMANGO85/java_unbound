package com.java_unbound.entities.geckolib;

import com.java_unbound.entities.geckolib.entities.*;

import net.minecraft.world.entity.EntityTypes;

public class Entities {
    public static void LoadEntities() {
        EntityRendererRegistration.AddEntity(EntityTypes.COW, Cow::RegisterEntity);
        /*EntityRendererRegistration.AddEntity(EntityTypes.SPIDER, Spider::RegisterEntity);
        EntityRendererRegistration.AddEntity(EntityTypes.SKELETON, Skeleton::RegisterEntity);
        EntityRendererRegistration.AddEntity(EntityTypes.CREEPER, Creeper::RegisterEntity);
        EntityRendererRegistration.AddEntity(EntityTypes.ALLAY, Allay::RegisterEntity);
        EntityRendererRegistration.AddEntity(EntityTypes.SNOW_GOLEM, Snow_Golem::RegisterEntity);
        EntityRendererRegistration.AddEntity(EntityTypes.PIG, Pig::RegisterEntity);
        EntityRendererRegistration.AddEntity(EntityTypes.TURTLE, Turtle::RegisterEntity);
        EntityRendererRegistration.AddEntity(EntityTypes.BEE, Bee::RegisterEntity);
        EntityRendererRegistration.AddEntity(EntityTypes.HAPPY_GHAST, Happy_Ghast::RegisterEntity);*/
    }
}