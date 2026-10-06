package com.java_unbound.molang.types;

import com.java_unbound.entities.interfaces.EntityBiomeInterface;
import com.java_unbound.entities.interfaces.EntityVariableInterface;
import com.java_unbound.molang.MolangParser;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.lang.reflect.Field;
import java.util.*;

public final class MolangEntityParser {
    private MolangEntityParser() {}

    public static double Evaluate(Entity Entity, String Expression) {
        if (Entity == null || Expression == null || Expression.isBlank()) {
            return 0;
        }

        Expression = Expression.trim();

        if (Expression.startsWith("v.")) {
            return EvaluateVariable(Entity, Expression.substring(2).trim());
        }

        if (Expression.startsWith("t.")) {
            return EvaluateVariable(Entity, Expression.substring(2).trim());
        }

        if (Expression.startsWith("q.")) {
            int OpenIndex = Expression.indexOf('(');

            if (OpenIndex < 0) {
                return EvaluateQuery(Entity, Expression.substring(2).trim(), List.of());
            }

            if (!Expression.endsWith(")")) {
                return 0;
            }

            String FunctionName = Expression.substring(2, OpenIndex).trim();
            String Arguments = Expression.substring(OpenIndex + 1, Expression.length() - 1);

            return EvaluateQuery(Entity, FunctionName, MolangParser.SplitArguments(Arguments));
        }

        return 0;
    }

    private static double EvaluateVariable(Entity Entity, String VariableName) {
        if (VariableName.isBlank()) {
            return 0;
        }

        return ((EntityVariableInterface) Entity).JavaUnbound$getVariable(VariableName);
    }

    public static void SetVariable(Entity Entity, String VariableName, double Value) {
        if (Entity == null || VariableName == null || VariableName.isBlank()) {
            return;
        }

        ((EntityVariableInterface) Entity).JavaUnbound$setVariable(VariableName, Value);

        Class<?> EntityClass = Entity.getClass();

        while (EntityClass != null) {
            try {
                Field Field = EntityClass.getDeclaredField(VariableName);
                Field.setAccessible(true);

                if (Field.getType() == double.class || Field.getType() == Double.class) {
                    Field.set(Entity, Value);
                    return;
                }

                if (Field.getType() == float.class || Field.getType() == Float.class) {
                    Field.set(Entity, (float) Value);
                    return;
                }

                if (Field.getType() == int.class || Field.getType() == Integer.class) {
                    Field.set(Entity, (int) Value);
                    return;
                }

                if (Field.getType() == long.class || Field.getType() == Long.class) {
                    Field.set(Entity, (long) Value);
                    return;
                }

                if (Field.getType() == boolean.class || Field.getType() == Boolean.class) {
                    Field.set(Entity, Value != 0);
                    return;
                }

                return;
            } catch (NoSuchFieldException Exception) {
                EntityClass = EntityClass.getSuperclass();
            } catch (IllegalAccessException Exception) {
                return;
            }
        }
    }

    private static double EvaluateQuery(Entity Entity, String FunctionName, List<String> Arguments) {
        return switch (FunctionName.toLowerCase(Locale.ROOT)) {
            case "is_name_any" -> EvaluateIsNameAny(Entity, Arguments);
            case "is_baby" -> Entity instanceof net.minecraft.world.entity.AgeableMob Mob && Mob.isBaby() ? 1 : 0;
            case "is_alive" -> Entity.isAlive() ? 1 : 0;
            case "is_on_ground" -> Entity.onGround() ? 1 : 0;
            case "is_in_water" -> Entity.isInWater() ? 1 : 0;
            case "is_in_lava" -> Entity.isInLava() ? 1 : 0;
            case "is_on_fire" -> Entity.isOnFire() ? 1 : 0;
            case "is_invisible" -> Entity.isInvisible() ? 1 : 0;
            case "is_sneaking" -> Entity.isCrouching() ? 1 : 0;
            case "is_sprinting" -> Entity.isSprinting() ? 1 : 0;
            case "is_swimming" -> Entity.isSwimming() ? 1 : 0;
            case "is_sleeping" -> Entity instanceof LivingEntity Living && Living.isSleeping() ? 1 : 0;
            case "is_riding" -> Entity.isPassenger() ? 1 : 0;
            case "is_riding_any_entity" -> Entity.isPassenger() ? 1 : 0;
            case "is_attached" -> Entity.isPassenger() ? 1 : 0;
            case "is_in_ui" -> 0;
            case "graphics_mode_is_any" -> 1;
            case "entity_biome_has_any_identifier" -> EvaluateBiome(Entity, Arguments);
            case "is_pack_setting_selected" -> 1;
            default -> 0;
        };
    }

    private static double EvaluateIsNameAny(Entity Entity, List<String> Values) {
        if (Entity.getCustomName() == null) {
            return 0;
        }

        String EntityName = Entity.getCustomName().getString();

        for (String Value : Values) {
            if (EntityName.equals(MolangParser.Unquote(Value).trim())) {
                return 1;
            }
        }

        return 0;
    }

    private static double EvaluateBiome(Entity Entity, List<String> Values) {
        String BiomeId = ((EntityBiomeInterface) Entity).JavaUnbound$getBiome();

        if (BiomeId == null) {
            return 0;
        }

        for (String Value : Values) {
            if (BiomeId.equals(MolangParser.Unquote(Value).trim())) {
                return 1;
            }
        }

        return 0;
    }
}