package com.java_unbound.molang.types;

import com.java_unbound.molang.MolangParser;
import net.minecraft.world.entity.Entity;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

public final class MolangMathParser {
    private MolangMathParser() {
    }

    public static String Evaluate(Entity Entity, String Expression) {
        if (Expression != null && Expression.equalsIgnoreCase("math.pi")) {
            return String.valueOf(Math.PI);
        }

        if (!IsMathExpression(Expression)) {
            return "0";
        }

        int OpenIndex = Expression.indexOf('(');
        String FunctionName = Expression.substring(5, OpenIndex).trim();
        String Arguments = Expression.substring(OpenIndex + 1, Expression.length() - 1);
        List<String> ArgumentExpressions = MolangParser.SplitArguments(Arguments);
        double[] Values = new double[ArgumentExpressions.size()];

        for (int Index = 0; Index < ArgumentExpressions.size(); Index++) {
            Values[Index] = MolangParser.ParseDouble(
                    MolangParser.Evaluate(Entity, ArgumentExpressions.get(Index))
            );
        }

        return String.valueOf(ApplyMath(FunctionName, Values));
    }

    public static boolean IsMathExpression(String Expression) {
        if (Expression != null && Expression.equalsIgnoreCase("math.pi")) {
            return true;
        }

        if (Expression == null || !Expression.toLowerCase(Locale.ROOT).startsWith("math.") || !Expression.endsWith(")")) {
            return false;
        }

        int OpenIndex = Expression.indexOf('(');

        if (OpenIndex <= 5 || !MolangParser.IsBalanced(Expression)) {
            return false;
        }

        int Depth = 0;
        char Quote = 0;

        for (int Index = OpenIndex; Index < Expression.length(); Index++) {
            char Character = Expression.charAt(Index);

            if (Quote != 0) {
                if (Character == Quote && !IsEscaped(Expression, Index)) {
                    Quote = 0;
                }

                continue;
            }

            if (Character == '\'' || Character == '"') {
                Quote = Character;
            } else if (Character == '(') {
                Depth++;
            } else if (Character == ')') {
                Depth--;

                if (Depth == 0 && Index != Expression.length() - 1) {
                    return false;
                }
            }
        }

        return Depth == 0 && Quote == 0;
    }

    public static double ApplyMath(String FunctionName, double[] Values) {
        if (FunctionName == null || Values == null) {
            return 0;
        }

        FunctionName = FunctionName.toLowerCase(Locale.ROOT);

        return switch (FunctionName) {
            case "abs" -> HasArguments(Values, 1) ? Math.abs(Values[0]) : 0;
            case "acos" -> HasArguments(Values, 1) ? Math.toDegrees(Math.acos(Values[0])) : 0;
            case "asin" -> HasArguments(Values, 1) ? Math.toDegrees(Math.asin(Values[0])) : 0;
            case "atan" -> HasArguments(Values, 1) ? Math.toDegrees(Math.atan(Values[0])) : 0;
            case "atan2" -> HasArguments(Values, 2) ? Math.toDegrees(Math.atan2(Values[0], Values[1])) : 0;
            case "ceil" -> HasArguments(Values, 1) ? Math.ceil(Values[0]) : 0;
            case "clamp" -> HasArguments(Values, 3) ? Math.max(Math.min(Values[0], Math.max(Values[1], Values[2])), Math.min(Values[1], Values[2])) : 0;
            case "copy_sign" -> HasArguments(Values, 2) ? Math.copySign(Values[0], Values[1]) : 0;
            case "cos" -> HasArguments(Values, 1) ? Math.cos(Math.toRadians(Values[0])) : 0;
            case "exp" -> HasArguments(Values, 1) ? Math.exp(Values[0]) : 0;
            case "floor" -> HasArguments(Values, 1) ? Math.floor(Values[0]) : 0;
            case "hermite_blend" -> HasArguments(Values, 1) ? Values[0] * Values[0] * (3 - 2 * Values[0]) : 0;
            case "inverse_lerp" -> HasArguments(Values, 3) && Values[1] != Values[0] ? (Values[2] - Values[0]) / (Values[1] - Values[0]) : 0;
            case "lerp" -> HasArguments(Values, 3) ? Values[0] + (Values[1] - Values[0]) * Values[2] : 0;
            case "lerprotate" -> HasArguments(Values, 3) ? Values[0] + MinAngle(Values[1] - Values[0]) * Values[2] : 0;
            case "ln", "log" -> HasArguments(Values, 1) ? Math.log(Values[0]) : 0;
            case "max" -> HasArguments(Values, 2) ? Math.max(Values[0], Values[1]) : 0;
            case "min" -> HasArguments(Values, 2) ? Math.min(Values[0], Values[1]) : 0;
            case "min_angle" -> HasArguments(Values, 1) ? MinAngle(Values[0]) : 0;
            case "mod" -> HasArguments(Values, 2) && Values[1] != 0 ? Values[0] % Values[1] : 0;
            case "pow" -> HasArguments(Values, 2) ? Math.pow(Values[0], Values[1]) : 0;
            case "random" -> HasArguments(Values, 2) ? RandomBetween(Values[0], Values[1]) : 0;
            case "random_integer" -> HasArguments(Values, 2) ? RandomInteger(Values[0], Values[1]) : 0;
            case "round" -> HasArguments(Values, 1) ? Math.floor(Values[0] + 0.5) : 0;
            case "sign" -> HasArguments(Values, 1) ? Values[0] > 0 ? 1 : -1 : 0;
            case "sin" -> HasArguments(Values, 1) ? Math.sin(Math.toRadians(Values[0])) : 0;
            case "sqrt" -> HasArguments(Values, 1) ? Math.sqrt(Values[0]) : 0;
            case "tan" -> HasArguments(Values, 1) ? Math.tan(Math.toRadians(Values[0])) : 0;
            case "trunc" -> HasArguments(Values, 1) ? (long) Values[0] : 0;
            case "die_roll" -> DieRoll(Values, false);
            case "die_roll_integer" -> DieRoll(Values, true);

            default -> 0;
        };
    }

    private static boolean HasArguments(double[] Values, int Count) {
        return Values.length >= Count;
    }

    private static double MinAngle(double Angle) {
        double Result = Angle % 360;

        if (Result >= 180) {
            Result -= 360;
        } else if (Result < -180) {
            Result += 360;
        }

        return Result;
    }

    private static double RandomBetween(double First, double Second) {
        double Min = Math.min(First, Second);
        double Max = Math.max(First, Second);

        if (Min == Max) {
            return Min;
        }

        return ThreadLocalRandom.current().nextDouble(Min, Math.nextUp(Max));
    }

    private static int RandomInteger(double First, double Second) {
        int Min = (int) Math.ceil(Math.min(First, Second));
        int Max = (int) Math.floor(Math.max(First, Second));

        if (Max < Min) {
            return 0;
        }

        return (int) ThreadLocalRandom.current().nextLong((long) Min, (long) Max + 1);
    }

    private static double DieRoll(double[] Values, boolean Integer) {
        if (!HasArguments(Values, 3)) {
            return 0;
        }

        int Count = Math.max(0, (int) Values[0]);
        double Low = Math.min(Values[1], Values[2]);
        double High = Math.max(Values[1], Values[2]);
        double Result = 0;

        for (int Index = 0; Index < Count; Index++) {
            Result += Integer ? RandomInteger(Low, High) : RandomBetween(Low, High);
        }

        return Result;
    }

    private static boolean IsEscaped(String Expression, int Index) {
        int BackslashCount = 0;

        for (int Previous = Index - 1; Previous >= 0 && Expression.charAt(Previous) == '\\'; Previous--) {
            BackslashCount++;
        }

        return BackslashCount % 2 != 0;
    }
}
