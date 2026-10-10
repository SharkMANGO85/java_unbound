package com.java_unbound.molang.types;

import com.java_unbound.entities.interfaces.EntityBiomeInterface;
import com.java_unbound.entities.interfaces.EntityVariableInterface;
import com.java_unbound.molang.MolangParser;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Locale;

//called for entites to get their variables and cases
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

            case "is_baby" -> Entity instanceof AgeableMob Mob && Mob.isBaby() ? 1 : 0;
            case "is_alive" -> Entity.isAlive() ? 1 : 0;
            case "life_time" -> Entity.tickCount;

            case "is_on_ground" -> Entity.onGround() ? 1 : 0;
            case "is_in_water" -> Entity.isInWater() ? 1 : 0;
            case "is_in_lava" -> Entity.isInLava() ? 1 : 0;
            case "is_in_water_or_rain" -> Entity.isInWaterOrRain() ? 1 : 0;
            case "is_in_contact_with_water" -> Entity.isInWaterOrRain() ? 1 : 0;

            case "is_on_fire" -> Entity.isOnFire() ? 1 : 0;
            case "is_invisible" -> Entity.isInvisible() ? 1 : 0;
            case "is_sneaking" -> Entity.isCrouching() ? 1 : 0;
            case "is_crouching" -> Entity.isCrouching() ? 1 : 0;
            case "is_sprinting" -> Entity.isSprinting() ? 1 : 0;
            case "is_swimming" -> Entity.isSwimming() ? 1 : 0;
            case "is_sleeping" -> Entity instanceof LivingEntity Living && Living.isSleeping() ? 1 : 0;

            case "is_riding" -> Entity.isPassenger() ? 1 : 0;
            case "is_riding_any_entity" -> Entity.isPassenger() ? 1 : 0;
            case "is_passenger" -> Entity.isPassenger() ? 1 : 0;
            case "is_vehicle" -> Entity.isVehicle() ? 1 : 0;
            case "is_attached" -> Entity.isPassenger() ? 1 : 0;

            case "is_jumping" -> Entity.getDeltaMovement().y > 0 && !Entity.onGround() ? 1 : 0;
            case "is_moving" -> Entity.getDeltaMovement().horizontalDistanceSqr() > 0.0001 ? 1 : 0;

            case "is_spectator" -> Entity instanceof Player Player && Player.isSpectator() ? 1 : 0;
            case "is_local_player" -> Entity instanceof Player Player && Player.level().isClientSide() && Player == net.minecraft.client.Minecraft.getInstance().player ? 1 : 0;

            case "is_tamed" -> Entity instanceof TamableAnimal Animal && Animal.isTame() ? 1 : 0;
            case "is_leashed" -> Entity instanceof Mob Mob && Mob.isLeashed() ? 1 : 0;
            case "is_angry" -> Entity instanceof Mob Mob && Mob.isAggressive() ? 1 : 0;

            case "max_health" -> Entity instanceof LivingEntity Living ? Living.getMaxHealth() : 0;
            case "health" -> Entity instanceof LivingEntity Living ? Living.getHealth() : 0;
            case "hurt_time" -> Entity instanceof LivingEntity Living ? Living.hurtTime : 0;

            case "invulnerable_ticks" -> Entity.invulnerableTime;
            case "fall_distance" -> Entity.fallDistance;
            case "vertical_speed" -> Entity.getDeltaMovement().y;

            case "is_in_ui" -> 0;
            case "graphics_mode_is_any" -> 1;

            case "entity_biome_has_any_identifier" -> EvaluateBiome(Entity, Arguments);

            case "is_pack_setting_selected" -> 1;
            case "is_pack_setting_enabled" -> 1;

            case "in_range" -> EvaluateInRange(Entity, Arguments);

            default -> 0;
        };
    }

    private static double EvaluateInRange(Entity Entity, List<String> Arguments) {
        if (Arguments.size() < 3) return 0;

        double Value = MolangParser.Evaluate(Entity, Arguments.get(0));
        double Min = MolangParser.Evaluate(Entity, Arguments.get(1));
        double Max = MolangParser.Evaluate(Entity, Arguments.get(2));

        return Value >= Min && Value <= Max ? 1 : 0;
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