package com.java_unbound.loader.entities.on_spawn;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;

public final class EntityLoadHandler {

    private EntityLoadHandler() {}

    public static void Register() {
        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> EntityHandler.HandleEntity(entity));
    }
}