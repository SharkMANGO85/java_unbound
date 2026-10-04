package com.java_unbound.loader.entities.on_spawn;

import com.java_unbound.JavaUnbound;
import com.java_unbound.loader.entities.EntityBiome;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.Entity;

public final class EntitySpawnHandler {
    private EntitySpawnHandler() {}

    public static void OnEntitySpawned(Entity Entity) {
        EntityHandler.HandleEntity(Entity);
    }
}