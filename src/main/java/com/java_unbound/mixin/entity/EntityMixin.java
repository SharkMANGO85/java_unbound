package com.java_unbound.mixin.entity;

import com.java_unbound.loader.entities.EntityBiome;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Entity.class)
public class EntityMixin implements EntityBiome {
    private String JavaUnbound$Biome;

    @Override
    public String JavaUnbound$getBiome() {
        return JavaUnbound$Biome;
    }

    @Override
    public void JavaUnbound$setBiome(String Biome) {
        JavaUnbound$Biome = Biome;
    }
}