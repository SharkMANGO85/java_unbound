package com.java_unbound.entities.functions;

import com.java_unbound.entities.interfaces.EntityBiomeInterface;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.Entity;

public class SetEntityBiome {
    public static void SetEntityBiome(Entity Entity) {
        EntityBiomeInterface Data = (EntityBiomeInterface) Entity;

        if (Data.JavaUnbound$getBiome() == null) {
            var Biome = Entity.level().registryAccess().lookupOrThrow(Registries.BIOME).getKey(Entity.level().getBiome(Entity.blockPosition()).value());

            if (Biome != null) {
                Data.JavaUnbound$setBiome(Biome.toString());
            }
        }
    }
}
