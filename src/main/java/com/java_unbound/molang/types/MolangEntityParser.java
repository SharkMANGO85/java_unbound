package com.java_unbound.molang.types;

import com.java_unbound.entities.interfaces.EntityBiomeInterface;
import com.java_unbound.entities.interfaces.EntityTemporaryInterface;
import com.java_unbound.entities.interfaces.EntityVariableInterface;
import com.java_unbound.molang.MolangParser;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Locale;

public final class MolangEntityParser {
    private static volatile PackSettingProvider PackSettings;

    private MolangEntityParser() {}

    private static final Logger Logger = LoggerFactory.getLogger("JavaUnbound/Molang");
    private static boolean Debug = true;

    public static void SetDebug(boolean Enabled) {
        Debug = Enabled;
    }

    private static void LogDebug(String Message) {
        if (Debug) Logger.info("[Molang] {}", Message);
    }

    private static void LogError(String Message, Exception Exception) {
        Logger.error("[Molang] {}", Message, Exception);
    }

    public interface PackSettingProvider {
        boolean IsEnabled(String SettingName);

        boolean IsSelected(String SettingName, String SelectedValue);
    }

    public static void SetPackSettingProvider(PackSettingProvider Provider) {
        PackSettings = Provider;
    }

    public static String Evaluate(Entity Entity, String Expression) {
        if (Expression == null || Expression.isBlank()) {
            LogDebug("Received empty expression.");
            return "0";
        }

        try {
            Expression = Expression.trim();
            String LowerExpression = Expression.toLowerCase(Locale.ROOT);

            LogDebug("Evaluating: " + Expression + " | Entity: " + (Entity != null ? Entity.getName().getString() : "null"));

            if (LowerExpression.startsWith("v.") || LowerExpression.startsWith("variable.")) {
                String Result = EvaluateVariable(Entity, Expression.substring(Expression.indexOf('.') + 1).trim());
                LogDebug("Variable result: " + Result);
                return Result;
            }

            if (LowerExpression.startsWith("t.") || LowerExpression.startsWith("temp.")) {
                String Result = EvaluateTemporary(Entity, Expression.substring(Expression.indexOf('.') + 1).trim());
                LogDebug("Temporary result: " + Result);
                return Result;
            }

            if (LowerExpression.startsWith("q.") || LowerExpression.startsWith("query.")) {
                int OpenIndex = Expression.indexOf('(');
                int PrefixLength = LowerExpression.startsWith("query.") ? 6 : 2;

                if (OpenIndex < 0) {
                    String Result = EvaluateQuery(Entity, Expression.substring(PrefixLength).trim(), List.of());
                    LogDebug("Query result: " + Result);
                    return Result;
                }

                if (!Expression.endsWith(")") || !MolangParser.IsBalanced(Expression)) {
                    LogError("Invalid query syntax: " + Expression, new IllegalArgumentException("Unbalanced query expression."));
                    return "0";
                }

                String FunctionName = Expression.substring(PrefixLength, OpenIndex).trim();
                String Arguments = Expression.substring(OpenIndex + 1, Expression.length() - 1);

                String Result = EvaluateQuery(Entity, FunctionName, MolangParser.SplitArguments(Arguments));
                LogDebug("Query '" + FunctionName + "' returned: " + Result);
                return Result;
            }

            LogError("Unknown expression: " + Expression, new IllegalArgumentException("Unsupported Molang expression."));
            return "0";

        } catch (Exception Exception) {
            LogError("Failed to evaluate expression: " + Expression, Exception);
            return "0";
        }
    }

    public static String GetVariable(Entity Entity, String VariableName) {
        if (Entity == null || VariableName == null || VariableName.isBlank()) {
            LogDebug("GetVariable received invalid arguments.");
            return null;
        }

        VariableName = VariableName.trim().toLowerCase(Locale.ROOT);

        LogDebug("Looking up variable: " + VariableName);

        if (!(Entity instanceof EntityVariableInterface Variables)) {
            LogDebug("FAILED: Entity does not implement EntityVariableInterface!");
            LogDebug("Entity class: " + Entity.getClass().getName());
            LogDebug("Entity type: " + Entity.getType());
            return null;
        }

        Object Value = Variables.JavaUnbound$getVariableObject(VariableName);

        if (Value == null) {
            LogDebug("Variable does not exist: " + VariableName);
            LogDebug("Available variables: " + Variables.JavaUnbound$getVariables());
            return null;
        }

        LogDebug("Variable found: " + VariableName + " = " + Value);

        return String.valueOf(Value);
    }

    private static String EvaluateVariable(Entity Entity, String VariableName) {
        String Value = GetVariable(Entity, VariableName);

        return Value != null ? Value : "0";
    }

    public static void SetVariable(Entity Entity, String VariableName, String Value) {
        if (Entity == null || VariableName == null || VariableName.isBlank()) {
            return;
        }

        if (!(Entity instanceof EntityVariableInterface Variables)) {
            return;
        }

        Variables.JavaUnbound$setVariable(
                VariableName.trim().toLowerCase(Locale.ROOT),
                Value == null ? "0" : Value
        );
    }

    public static String GetTemporary(Entity Entity, String TemporaryName) {
        if (Entity == null || TemporaryName == null || TemporaryName.isBlank()) {
            return null;
        }

        if (Entity instanceof EntityTemporaryInterface Temporaries) {
            Object Value = Temporaries.JavaUnbound$getTemporaryObject(
                    TemporaryName.trim().toLowerCase(Locale.ROOT)
            );

            return Value != null ? String.valueOf(Value) : null;
        }

        LogDebug("Entity does not implement EntityTemporaryInterface: " + Entity.getType());
        return null;
    }

    private static String EvaluateTemporary(Entity Entity, String TemporaryName) {
        String Value = GetTemporary(Entity, TemporaryName);

        return Value != null ? Value : "0";
    }

    public static void SetTemporary(Entity Entity, String TemporaryName, String Value) {
        if (Entity == null || TemporaryName == null || TemporaryName.isBlank()) {
            return;
        }

        if (!(Entity instanceof EntityTemporaryInterface Temporaries)) {
            return;
        }

        Temporaries.JavaUnbound$setTemporary(
                TemporaryName.trim().toLowerCase(Locale.ROOT),
                Value == null ? "0" : Value
        );
    }

    public static void ClearTemporaries(Entity Entity) {
        if (Entity instanceof EntityTemporaryInterface Temporaries) {
            Temporaries.JavaUnbound$getTemporaries().clear();
        }
    }

    public static String EvaluateQuery(Entity Entity, String FunctionName, List<String> Arguments) {
        if (Entity == null || FunctionName == null) {
            return "0";
        }

        if (Arguments == null) {
            Arguments = List.of();
        }

        return switch (FunctionName.toLowerCase(Locale.ROOT)) {
            case "is_name_any" -> EvaluateIsNameAny(Entity, Arguments);

            case "is_baby" -> Entity instanceof AgeableMob Mob && Mob.isBaby() ? "1" : "0";
            case "is_alive" -> Entity.isAlive() ? "1" : "0";
            case "life_time" -> String.valueOf(Entity.tickCount / 20.0);

            case "is_on_ground" -> Entity.onGround() ? "1" : "0";
            case "is_in_water" -> Entity.isInWater() ? "1" : "0";
            case "is_in_lava" -> Entity.isInLava() ? "1" : "0";
            case "is_in_water_or_rain", "is_in_contact_with_water" -> Entity.isInWaterOrRain() ? "1" : "0";

            case "is_on_fire", "is_onfire" -> Entity.isOnFire() ? "1" : "0";
            case "is_invisible" -> Entity.isInvisible() ? "1" : "0";
            case "is_sneaking", "is_crouching" -> Entity.isCrouching() ? "1" : "0";
            case "is_sprinting" -> Entity.isSprinting() ? "1" : "0";
            case "is_swimming" -> Entity.isSwimming() ? "1" : "0";
            case "is_sleeping" -> Entity instanceof LivingEntity Living && Living.isSleeping() ? "1" : "0";

            case "is_riding", "is_riding_any_entity", "is_passenger", "is_attached" -> Entity.isPassenger() ? "1" : "0";
            case "is_vehicle" -> Entity.isVehicle() ? "1" : "0";

            case "is_jumping" -> Entity.getDeltaMovement().y > 0 && !Entity.onGround() ? "1" : "0";
            case "is_moving" -> Entity.getDeltaMovement().horizontalDistanceSqr() > 0.0001 ? "1" : "0";

            case "is_spectator" -> Entity instanceof Player Player && Player.isSpectator() ? "1" : "0";
            case "is_local_player" -> IsLocalPlayer(Entity);

            case "is_tamed" -> Entity instanceof TamableAnimal Animal && Animal.isTame() ? "1" : "0";
            case "is_leashed" -> Entity instanceof Mob Mob && Mob.isLeashed() ? "1" : "0";
            case "is_angry" -> Entity instanceof Mob Mob && Mob.isAggressive() ? "1" : "0";

            case "max_health" -> Entity instanceof LivingEntity Living ? String.valueOf(Living.getMaxHealth()) : "0";
            case "health" -> Entity instanceof LivingEntity Living ? String.valueOf(Living.getHealth()) : "0";
            case "hurt_time" -> Entity instanceof LivingEntity Living ? String.valueOf(Living.hurtTime) : "0";

            case "invulnerable_ticks" -> String.valueOf(Entity.invulnerableTime);
            case "fall_distance" -> String.valueOf(Entity.fallDistance);
            case "vertical_speed" -> String.valueOf(Entity.getDeltaMovement().y);

            case "is_in_ui" -> IsInUI();
            case "graphics_mode_is_any" -> GraphicsModeIsAny(Arguments);

            case "entity_biome_has_any_identifier" -> EvaluateBiome(Entity, Arguments);

            case "is_pack_setting_selected" -> IsPackSettingSelected(Arguments);
            case "is_pack_setting_enabled" -> IsPackSettingEnabled(Arguments);

            case "in_range" -> EvaluateInRange(Entity, Arguments);
            case "any" -> EvaluateAny(Entity, Arguments);
            case "all" -> EvaluateAll(Entity, Arguments);

            case "has_rider" -> !Entity.getPassengers().isEmpty() ? "1" : "0";
            case "has_target" -> Entity instanceof Mob Mob && Mob.getTarget() != null ? "1" : "0";

            default -> {
                LogDebug("Unknown query: " + FunctionName + " | Arguments: " + Arguments);
                yield "0";
            }
        };
    }

    private static String IsLocalPlayer(Entity Entity) {
        Minecraft Client = Minecraft.getInstance();

        return Entity instanceof Player Player
                && Player.level().isClientSide()
                && Client != null
                && Player == Client.player ? "1" : "0";
    }

    private static String EvaluateAny(Entity Entity, List<String> Arguments) {
        if (Arguments.size() < 2) {
            return "0";
        }

        double Value = MolangParser.ParseDouble(MolangParser.Evaluate(Entity, Arguments.getFirst()));

        for (int Index = 1; Index < Arguments.size(); Index++) {
            double ArgumentValue = MolangParser.ParseDouble(MolangParser.Evaluate(Entity, Arguments.get(Index)));

            if (Double.compare(ArgumentValue, Value) == 0) {
                return "1";
            }
        }

        return "0";
    }

    private static String EvaluateAll(Entity Entity, List<String> Arguments) {
        if (Arguments.size() < 3) {
            return "0";
        }

        double Value = MolangParser.ParseDouble(MolangParser.Evaluate(Entity, Arguments.getFirst()));

        for (int Index = 1; Index < Arguments.size(); Index++) {
            double ArgumentValue = MolangParser.ParseDouble(MolangParser.Evaluate(Entity, Arguments.get(Index)));

            if (Double.compare(ArgumentValue, Value) != 0) {
                return "0";
            }
        }

        return "1";
    }

    private static String EvaluateInRange(Entity Entity, List<String> Arguments) {
        if (Arguments.size() < 3) {
            return "0";
        }

        double Value = MolangParser.ParseDouble(MolangParser.Evaluate(Entity, Arguments.get(0)));
        double Min = MolangParser.ParseDouble(MolangParser.Evaluate(Entity, Arguments.get(1)));
        double Max = MolangParser.ParseDouble(MolangParser.Evaluate(Entity, Arguments.get(2)));

        return Value >= Math.min(Min, Max) && Value <= Math.max(Min, Max) ? "1" : "0";
    }

    private static String IsPackSettingSelected(List<String> Arguments) {
        if (PackSettings == null || Arguments.size() < 2) {
            return "0";
        }

        String SettingName = MolangParser.Unquote(Arguments.get(0)).trim();
        String SelectedValue = MolangParser.Unquote(Arguments.get(1)).trim();

        return PackSettings.IsSelected(SettingName, SelectedValue) ? "1" : "0";
    }

    private static String IsPackSettingEnabled(List<String> Arguments) {
        if (PackSettings == null || Arguments.isEmpty()) {
            return "0";
        }

        String SettingName = MolangParser.Unquote(Arguments.getFirst()).trim();

        return PackSettings.IsEnabled(SettingName) ? "1" : "0";
    }

    private static String IsInUI() {
        Minecraft Client = Minecraft.getInstance();

        return Client != null && Client.gui.screen() != null ? "1" : "0";
    }

    private static String GraphicsModeIsAny(List<String> Arguments) {
        Minecraft Client = Minecraft.getInstance();

        if (Client == null || Arguments.isEmpty()) {
            return "0";
        }

        String Graphics = NormalizeGraphicsMode(Client.options.graphicsPreset().get().toString());

        for (String Argument : Arguments) {
            String Mode = NormalizeGraphicsMode(MolangParser.Unquote(Argument).trim());

            if (Mode.equals(Graphics)) {
                return "1";
            }
        }

        return "0";
    }

    private static String NormalizeGraphicsMode(String Mode) {
        String Normalized = Mode.toLowerCase(Locale.ROOT).replace('-', '_').replace(' ', '_');

        return switch (Normalized) {
            case "simple", "fast" -> "fast";
            case "fancy" -> "fancy";
            case "deferred", "ray_traced", "raytraced", "fabulous" -> "fabulous";
            case "custom" -> "custom";
            default -> Normalized;
        };
    }

    private static String EvaluateIsNameAny(Entity Entity, List<String> Values) {
        if (Entity.getCustomName() == null) {
            return "0";
        }

        String EntityName = Entity.getCustomName().getString();

        for (String Value : Values) {
            if (EntityName.equals(MolangParser.Unquote(Value).trim())) {
                return "1";
            }
        }

        return "0";
    }

    private static String EvaluateBiome(Entity Entity, List<String> Values) {
        if (!(Entity instanceof EntityBiomeInterface BiomeInterface)) {
            LogDebug("Entity does not implement EntityBiomeInterface: " + Entity.getType());
            return "0";
        }

        String BiomeId = BiomeInterface.JavaUnbound$getBiome();

        if (BiomeId == null) {
            return "0";
        }

        for (String Value : Values) {
            if (BiomeId.equals(MolangParser.Unquote(Value).trim())) {
                return "1";
            }
        }

        return "0";
    }
}
