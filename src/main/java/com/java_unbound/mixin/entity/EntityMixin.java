package com.java_unbound.mixin.entity;

import com.java_unbound.entities.interfaces.EntityBiomeInterface;
import com.java_unbound.entities.interfaces.EntityVariableInterface;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;

import java.util.HashMap;

//set variables and the biome to the entity
@Mixin(Entity.class)
public class EntityMixin implements EntityBiomeInterface, EntityVariableInterface {
    private String JavaUnbound$Biome;
    private final HashMap<String, Object> JavaUnbound$Variables = new HashMap<>();

    @Override
    public String JavaUnbound$getBiome() {
        return JavaUnbound$Biome;
    }

    @Override
    public void JavaUnbound$setBiome(String Biome) {
        JavaUnbound$Biome = Biome;
    }

    @Override
    public HashMap<String, Object> JavaUnbound$getVariables() {
        return JavaUnbound$Variables;
    }

    @Override
    public void JavaUnbound$setVariable(String Name, double Value) {
        JavaUnbound$Variables.put(Name, Value);
    }

    @Override
    public void JavaUnbound$setVariable(String Name, String Value) {
        JavaUnbound$Variables.put(Name, Value);
    }

    @Override
    public double JavaUnbound$getVariable(String Name) {
        Object Value = JavaUnbound$Variables.get(Name);

        if (Value instanceof Number Number) {
            return Number.doubleValue();
        }

        if (Value instanceof Boolean Boolean) {
            return Boolean ? 1.0 : 0.0;
        }

        return 0.0;
    }

    @Override
    public Object JavaUnbound$getVariableObject(String Name) {
        return JavaUnbound$Variables.get(Name);
    }
}