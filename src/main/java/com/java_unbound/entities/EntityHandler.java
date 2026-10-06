package com.java_unbound.entities;

import net.minecraft.world.entity.Entity;

import static com.java_unbound.entities.functions.InitializeEntity.InitializeEntity;

public final class EntityHandler {
    private EntityHandler() {}

    public static void HandleEntityLoad(Entity Entity) {
        InitializeEntity(Entity);
    }

    public static void HandleEntitySpawn(Entity Entity) {
        InitializeEntity(Entity);
    }
}