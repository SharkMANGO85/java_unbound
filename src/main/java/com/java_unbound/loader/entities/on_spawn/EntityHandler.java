package com.java_unbound.loader.entities.on_spawn;

import com.java_unbound.JavaUnbound;
import com.java_unbound.global.HashMaps;
import com.java_unbound.loader.entities.EntityBiome;
import com.java_unbound.loader.entities.EntityRegistry;
import com.java_unbound.loader.molang.MolangParser;
import com.java_unbound.loader.molang.types.MolangEntityParser;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.Entity;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.util.Map;

public final class EntityHandler {
    private EntityHandler() {}

    public static void HandleEntity(Entity Entity) {
        InitializeEntity(Entity);
    }

    private static void SetEntityBiome(Entity Entity) {
        EntityBiome Data = (EntityBiome) Entity;

        if (Data.JavaUnbound$getBiome() == null) {
            var Biome = Entity.level().registryAccess().lookupOrThrow(Registries.BIOME).getKey(Entity.level().getBiome(Entity.blockPosition()).value());

            if (Biome != null) {
                Data.JavaUnbound$setBiome(Biome.toString());
            }
        }
    }

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


    }
}