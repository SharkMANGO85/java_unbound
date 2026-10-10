package com.java_unbound.entities.initialization;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.java_unbound.entities.actions.EntityTickHandler;
import com.java_unbound.entities.loaders.EntityRegistry;
import com.java_unbound.entities.render_controller.EntityRenderControllerRegistry;
import com.java_unbound.global.EntityRenderControllerData;
import com.java_unbound.global.HashMaps;
import com.java_unbound.molang.MolangParser;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;

import java.util.HashMap;

import static com.java_unbound.entities.functions.SetEntityBiome.SetEntityBiome;
import static com.java_unbound.entities.render.GetActiveRenderControllers.GetActiveRenderControllers;

public class InitializeEntity {
    //initiliaze the enity witht the variabless, etc.
    public static void InitializeEntity(Entity Entity) {
        if (Entity == null) {return;}

        String Identifier = BuiltInRegistries.ENTITY_TYPE.getKey(Entity.getType()).toString();

        InitializeBiome(Entity);
        EntityRegistry EntityRegistry = GetEntityRegistry(Identifier);

        if (EntityRegistry == null) {return;}

        InitializeScript(Entity, EntityRegistry);

        EntityTickHandler.RenderControllerTick(Entity);
    }

    //set the biome if not done yet
    private static void InitializeBiome(Entity Entity) {
        try {
            SetEntityBiome(Entity);
        } catch (Exception Exception) {
            Exception.printStackTrace();
        }
    }

    //gets the entity registry which contains the geometries, textures, etc.
    private static EntityRegistry GetEntityRegistry(String Identifier) {
        if (!HashMaps.EntityFiles.containsKey(Identifier)) {return null;}

        return HashMaps.EntityRegistries.get(Identifier);
    }

    //sets the v. variables for the entity
    private static void InitializeScript(Entity Entity, EntityRegistry EntityRegistry) {
        JsonArray Initialize = EntityRegistry.Scripts.get("initialize");

        if (Initialize == null) {return;}

        for (JsonElement Element : Initialize) {
            if (Element == null || !Element.isJsonPrimitive()) {continue;}

            try {
                MolangParser.Evaluate(Entity, Element.getAsString());
            } catch (Exception Exception) {
                Exception.printStackTrace();
            }
        }
    }
}