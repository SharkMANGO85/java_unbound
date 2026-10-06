package com.java_unbound.entities.functions;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.java_unbound.JavaUnbound;
import com.java_unbound.entities.loader.EntityRegistry;
import com.java_unbound.global.HashMaps;
import com.java_unbound.molang.MolangParser;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;

import java.util.HashMap;

import static com.java_unbound.entities.functions.SetEntityBiome.SetEntityBiome;

public class InitializeEntity {
    public static void InitializeEntity(Entity Entity) {
        SetEntityBiome(Entity);

        String Identifier = BuiltInRegistries.ENTITY_TYPE.getKey(Entity.getType()).toString();

        if (!HashMaps.EntityFiles.containsKey(Identifier)) {return;}

        EntityRegistry EntityRegistry = HashMaps.EntityRegistries.get(Identifier);

        if (EntityRegistry == null) {return;}

        JsonArray Initialize = EntityRegistry.Scripts.get("initialize");

        if (Initialize == null) {return;}

        for (JsonElement Element : Initialize) {
            MolangParser.Evaluate(Entity, Element.getAsString());
        }

        HashMap<String, JsonElement> ActiveRenderControllers = GetActiveRenderControllers.GetActiveRenderControllers(Entity);

        //From the a
    }
}
