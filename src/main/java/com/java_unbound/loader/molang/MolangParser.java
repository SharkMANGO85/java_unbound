package com.java_unbound.loader.molang;

import com.java_unbound.loader.molang.types.MolangEntityParser;
import com.java_unbound.loader.molang.types.MolangMathParser;
import net.minecraft.world.entity.Entity;

import java.util.ArrayList;
import java.util.List;

public final class MolangParser {
    private MolangParser() {}

    public static MolangExpression Classify(String Expression) {
        if (Expression == null) {
            return new MolangExpression(MolangExpression.Type.UNKNOWN, "");
        }

        Expression = Expression.trim();

        if (Expression.matches("[qv]\\.[A-Za-z0-9_]+\\s*=.*")) {
            return new MolangExpression(MolangExpression.Type.ASSIGNMENT, Expression);
        }

        if (IsNumber(Expression)) {
            return new MolangExpression(MolangExpression.Type.NUMBER, Expression);
        }

        if (Expression.matches("[qv]\\.[A-Za-z0-9_]+")) {
            return new MolangExpression(MolangExpression.Type.VALUE, Expression);
        }

        if (MolangMathParser.IsMathExpression(Expression)) {
            return new MolangExpression(MolangExpression.Type.MATH, Expression);
        }

        return new MolangExpression(MolangExpression.Type.UNKNOWN, Expression);
    }

    public static double Evaluate(Entity Entity, String Expression) {
        if (Expression == null || Expression.isBlank()) {return 0;}

        Expression = Expression.trim();

        if (IsNumber(Expression)) {
            return Double.parseDouble(Expression);
        }

        if (Expression.startsWith("q.")) {
            return MolangEntityParser.Evaluate(Entity, Expression);
        }

        if (MolangMathParser.IsMathExpression(Expression)) {
            return MolangMathParser.Evaluate(Entity, Expression);
        }

        return 0;
    }

    public static boolean IsNumber(String Expression) {
        return Expression.matches("[+-]?\\d+(\\.\\d+)?");
    }

    public static String GetArguments(String Expression) {
        return Expression.substring(Expression.indexOf('(') + 1, Expression.length() - 1);
    }

    public static String Unquote(String Value) {
        Value = Value.trim();

        if (Value.length() >= 2) {
            char First = Value.charAt(0);
            char Last = Value.charAt(Value.length() - 1);

            if ((First == '\'' && Last == '\'') || (First == '"' && Last == '"')) {
                return Value.substring(1, Value.length() - 1);
            }
        }

        return Value;
    }

    public static List<String> SplitArguments(String Arguments) {
        List<String> Result = new ArrayList<>();
        StringBuilder Current = new StringBuilder();

        int Depth = 0;
        char Quote = 0;

        for (int Index = 0; Index < Arguments.length(); Index++) {
            char Character = Arguments.charAt(Index);

            if (Quote != 0) {
                Current.append(Character);

                if (Character == Quote) {
                    Quote = 0;
                }

                continue;
            }

            if (Character == '\'' || Character == '"') {
                Quote = Character;
                Current.append(Character);
                continue;
            }

            if (Character == '(') {
                Depth++;
                Current.append(Character);
                continue;
            }

            if (Character == ')') {
                Depth--;
                Current.append(Character);
                continue;
            }

            if (Character == ',' && Depth == 0) {
                Result.add(Current.toString().trim());
                Current.setLength(0);
                continue;
            }

            Current.append(Character);
        }

        if (!Current.isEmpty()) {
            Result.add(Current.toString().trim());
        }

        return Result;
    }

    public static boolean IsBalanced(String Expression) {
        int Depth = 0;
        char Quote = 0;

        for (int Index = 0; Index < Expression.length(); Index++) {
            char Character = Expression.charAt(Index);

            if (Quote != 0) {
                if (Character == Quote) {
                    Quote = 0;
                }

                continue;
            }

            if (Character == '\'' || Character == '"') {
                Quote = Character;
                continue;
            }

            if (Character == '(') {
                Depth++;
            }

            if (Character == ')') {
                Depth--;
            }

            if (Depth < 0) {
                return false;
            }
        }

        return Depth == 0 && Quote == 0;
    }
}