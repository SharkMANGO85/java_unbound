package com.java_unbound.entities.actions;

import com.java_unbound.entities.EntityHandler;
import net.minecraft.world.entity.Entity;

public final class EntitySpawnHandler {
    private EntitySpawnHandler() {}

    //gets called once a new entity gets spawned
    public static void OnEntitySpawned(Entity Entity) {
        EntityHandler.HandleEntitySpawn(Entity);
    }
}