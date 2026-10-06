package com.java_unbound.entities.functions;

import com.google.gson.JsonElement;
import com.java_unbound.JavaUnbound;
import com.java_unbound.entities.loader.EntityRegistry;
import com.java_unbound.global.HashMaps;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;

import java.util.HashMap;

import static com.java_unbound.entities.functions.GetActiveRenderControllers.GetActiveRenderControllers;

public class OnEntityTick {
    public static void OnEntityTick(Entity Entity) {
        String Identifier = BuiltInRegistries.ENTITY_TYPE.getKey(Entity.getType()).toString();

        if (!HashMaps.EntityFiles.containsKey(Identifier)) {return;}

        EntityRegistry EntityRegistry = HashMaps.EntityRegistries.get(Identifier);

        if (EntityRegistry == null) {return;}


        HashMap<String, JsonElement> ActiveRenderControllers = GetActiveRenderControllers(Entity);

        JavaUnbound.LOGGER.error("RENDER CONTROLLERS: " + ActiveRenderControllers);
    }
}
