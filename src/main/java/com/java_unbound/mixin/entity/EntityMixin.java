package com.java_unbound.mixin.entity;

import com.java_unbound.entities.interfaces.EntityBiomeInterface;
import com.java_unbound.entities.interfaces.EntityTemporaryInterface;
import com.java_unbound.entities.interfaces.EntityVariableInterface;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;

import java.util.HashMap;

@Mixin(Entity.class)
public class EntityMixin implements EntityBiomeInterface, EntityVariableInterface, EntityTemporaryInterface {
    private String JavaUnbound$Biome;

    private final HashMap<String, Object> JavaUnbound$Variables = new HashMap<>();
    private final HashMap<String, Object> JavaUnbound$Temporaries = new HashMap<>();

    private boolean JavaUnbound$Initialized;

    @Override
    public boolean JavaUnbound$isInitialized() {
        return JavaUnbound$Initialized;
    }

    @Override
    public void JavaUnbound$setInitialized(boolean Initialized) {
        JavaUnbound$Initialized = Initialized;
    }

    @Override
    public String JavaUnbound$getBiome() {
        return JavaUnbound$Biome;
    }

    @Override
    public void JavaUnbound$setBiome(String Biome) {
        JavaUnbound$Biome = Biome;
    }

    // Variables

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
    public String JavaUnbound$getVariable(String Name) {
        Object Value = JavaUnbound$Variables.get(Name);

        if (Value instanceof Number || Value instanceof Boolean || Value instanceof String) {
            return String.valueOf(Value);
        }

        return "0.0";
    }

    @Override
    public Object JavaUnbound$getVariableObject(String Name) {
        return JavaUnbound$Variables.get(Name);
    }

    // Temporaries

    @Override
    public HashMap<String, Object> JavaUnbound$getTemporaries() {
        return JavaUnbound$Temporaries;
    }

    @Override
    public void JavaUnbound$setTemporary(String Name, double Value) {
        JavaUnbound$Temporaries.put(Name, Value);
    }

    @Override
    public void JavaUnbound$setTemporary(String Name, String Value) {
        JavaUnbound$Temporaries.put(Name, Value);
    }

    @Override
    public String JavaUnbound$getTemporary(String Name) {
        Object Value = JavaUnbound$Temporaries.get(Name);

        if (Value instanceof Number || Value instanceof Boolean || Value instanceof String) {
            return String.valueOf(Value);
        }

        return "0.0";
    }

    @Override
    public Object JavaUnbound$getTemporaryObject(String Name) {
        return JavaUnbound$Temporaries.get(Name);
    }
}