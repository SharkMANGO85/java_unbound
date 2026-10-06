package com.java_unbound.mixin.entity;

import com.java_unbound.entities.interfaces.EntityBiomeInterface;
import com.java_unbound.entities.interfaces.EntityVariableInterface;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;

import java.util.HashMap;

@Mixin(Entity.class)
public class EntityMixin implements EntityBiomeInterface, EntityVariableInterface {
    private String JavaUnbound$Biome;
    private final HashMap<String, Double> JavaUnbound$Variables = new HashMap<>();

    @Override
    public String JavaUnbound$getBiome() {
        return JavaUnbound$Biome;
    }

    @Override
    public void JavaUnbound$setBiome(String Biome) {
        JavaUnbound$Biome = Biome;
    }

    @Override
    public HashMap<String, Double> JavaUnbound$getVariables() {
        return JavaUnbound$Variables;
    }

    @Override
    public void JavaUnbound$setVariable(String Name, double Value) {
        JavaUnbound$Variables.put(Name, Value);
    }

    @Override
    public double JavaUnbound$getVariable(String Name) {
        return JavaUnbound$Variables.getOrDefault(Name, 0.0);
    }
}