package com.java_unbound.entities;

import net.minecraft.world.entity.Entity;

import com.java_unbound.entities.InitializeEntity;

//calls the load and spawn functions
public final class EntityHandler {
    private EntityHandler() {}

    public static void HandleEntityLoad(Entity Entity) {
        InitializeEntity.InitializeEntity(Entity);
    }

    public static void HandleEntitySpawn(Entity Entity) {
        InitializeEntity.InitializeEntity(Entity);
    }
}