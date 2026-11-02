package com.java_unbound.entities.geckolib;

import com.java_unbound.entities.geckolib.entities.*;
import net.minecraft.world.entity.Entity;

public class Entities {
    public static void LoadEntities(Entity Entity) {
        Cow.RegisterEntity(Entity);
        Spider.RegisterEntity(Entity);
        Skeleton.RegisterEntity(Entity);
        Creeper.RegisterEntity(Entity);
        Allay.RegisterEntity(Entity);
        Snow_Golem.RegisterEntity(Entity);
        Pig.RegisterEntity(Entity);
        Turtle.RegisterEntity(Entity);
        Bee.RegisterEntity(Entity);
        Happy_Ghast.RegisterEntity(Entity);
    }
}
