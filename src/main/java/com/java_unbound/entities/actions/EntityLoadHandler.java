package com.java_unbound.entities.actions;

import com.java_unbound.entities.EntityHandler;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;

public final class EntityLoadHandler {
    private EntityLoadHandler() {}

    //Gets Called once an entity gets loaded to the game
    public static void Register() {
        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> EntityHandler.HandleEntityLoad(entity));
    }
}