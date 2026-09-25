package com.java_unbound.loader.molang;

import net.minecraft.world.entity.Entity;

import java.util.Arrays;

public final class EntityMolangParser {
    private EntityMolangParser() {}

    public static boolean Read(Entity PareserEntity, String FunctionName, String[] Values) {
        return switch (FunctionName) {
            case "is_name_any" -> {
                if (PareserEntity.getCustomName() == null) {yield false;}

                yield Arrays.stream(Values).anyMatch(Value -> PareserEntity.getCustomName().getString().equals(Value));
            }

            default -> false;
        };
    }
}