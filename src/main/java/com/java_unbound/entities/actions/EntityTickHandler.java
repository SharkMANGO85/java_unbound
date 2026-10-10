package com.java_unbound.entities.actions;

import com.google.gson.JsonElement;
import com.java_unbound.JavaUnbound;
import com.java_unbound.entities.loaders.EntityRegistry;
import com.java_unbound.entities.render_controller.EntityRenderControllerRegistry;
import com.java_unbound.global.EntityRenderControllerData;
import com.java_unbound.global.HashMaps;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;

import java.util.HashMap;

import static com.java_unbound.entities.render.GetActiveRenderControllers.GetActiveRenderControllers;

public class EntityTickHandler {
    //gets called each entity tick
    public static void OnEntityTick(Entity Entity) {
        String Identifier = BuiltInRegistries.ENTITY_TYPE.getKey(Entity.getType()).toString();

        if (!HashMaps.EntityFiles.containsKey(Identifier)) {return;}

        EntityRegistry EntityRegistry = HashMaps.EntityRegistries.get(Identifier);

        if (EntityRegistry == null) {return;}

        //add later a system where only call when v. changes of the entity
    }

    //gets called each tick to get a new render controller data
    public static void RenderControllerTick(Entity Entity) {
        HashMap<String, JsonElement> ActiveRenderControllers = GetActiveRenderControllers(Entity);

        for (String Key : ActiveRenderControllers.keySet()) {
            EntityRenderControllerData ResolvedData = EntityRenderControllerRegistry.GetRenderControllerData(Entity, Key);

            JavaUnbound.LOGGER.warn("[EntityTickHandler] Identifier: " + ResolvedData.Identifier);
            JavaUnbound.LOGGER.warn("[EntityTickHandler] Geometry: " + ResolvedData.Geometry);
            JavaUnbound.LOGGER.warn("[EntityTickHandler] Textures: " + ResolvedData.Textures);
            JavaUnbound.LOGGER.warn("[EntityTickHandler] Materials: " + ResolvedData.Materials);
        }
    }

    //returns for example for Texture.adwd the correct texture path by getting the main json file and getting the path by the given identifier
    public static void GetDataFromIdentifier(String Type, String Identifier) {

    }
}
