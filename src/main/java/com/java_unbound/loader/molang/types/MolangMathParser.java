package com.java_unbound.loader.molang.types;

import com.java_unbound.loader.molang.MolangParser;
import net.minecraft.world.entity.Entity;

import java.util.List;

public final class MolangMathParser {
    private MolangMathParser() {}

    public static double Evaluate(Entity Entity, String Expression) {
        if (!IsMathExpression(Expression)) {return 0;}

        int OpenIndex = Expression.indexOf('(');
        String FunctionName = Expression.substring(5, OpenIndex).trim();
        String Arguments = Expression.substring(OpenIndex + 1, Expression.length() - 1);

        List<String> ArgumentExpressions = MolangParser.SplitArguments(Arguments);
        double[] Values = new double[ArgumentExpressions.size()];

        for (int Index = 0; Index < ArgumentExpressions.size(); Index++) {
            Values[Index] = MolangParser.Evaluate(Entity, ArgumentExpressions.get(Index));
        }

        return ApplyMath(FunctionName, Values);
    }

    public static boolean IsMathExpression(String Expression) {
        if (Expression == null || !Expression.startsWith("math.") || !Expression.endsWith(")")) {
            return false;
        }

        int OpenIndex = Expression.indexOf('(');
        return OpenIndex > 5 && MolangParser.IsBalanced(Expression);
    }

    private static double ApplyMath(String FunctionName, double[] Values) {
        if (Values.length == 0) {return 0;}

        return switch (FunctionName) {
            case "abs" -> Math.abs(Values[0]);
            case "sin" -> Math.sin(Values[0]);
            case "cos" -> Math.cos(Values[0]);
            case "tan" -> Math.tan(Values[0]);
            case "sqrt" -> Math.sqrt(Values[0]);
            case "floor" -> Math.floor(Values[0]);
            case "ceil" -> Math.ceil(Values[0]);
            case "round" -> Math.round(Values[0]);
            case "exp" -> Math.exp(Values[0]);
            case "log" -> Math.log(Values[0]);
            case "min" -> Math.min(Values[0], Values.length > 1 ? Values[1] : 0);
            case "max" -> Math.max(Values[0], Values.length > 1 ? Values[1] : 0);
            default -> 0;
        };
    }
}