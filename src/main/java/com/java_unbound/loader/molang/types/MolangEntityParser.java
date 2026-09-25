package com.java_unbound.loader.molang.types;

import com.java_unbound.loader.molang.MolangParser;
import net.minecraft.world.entity.Entity;

import java.util.List;

public final class MolangEntityParser {
    private MolangEntityParser() {}

    public static double Evaluate(Entity Entity, String Expression) {
        if (Expression == null || Expression.isBlank()) {return 0;}

        Expression = Expression.trim();

        if (!Expression.startsWith("q.") || !Expression.endsWith(")")) {
            return 0;
        }

        int OpenIndex = Expression.indexOf('(');

        if (OpenIndex < 0) {
            return 0;
        }

        String FunctionName = Expression.substring(2, OpenIndex).trim();
        String Arguments = Expression.substring(OpenIndex + 1, Expression.length() - 1);

        return switch (FunctionName) {
            case "is_name_any" -> EvaluateIsNameAny(Entity, Arguments);
            default -> 0;
        };
    }

    private static double EvaluateIsNameAny(Entity Entity, String Arguments) {
        if (Entity == null || Entity.getCustomName() == null) {
            return 0;
        }

        String EntityName = Entity.getCustomName().getString();
        List<String> Values = MolangParser.SplitArguments(Arguments);

        for (String Value : Values) {
            if (EntityName.equals(MolangParser.Unquote(Value))) {
                return 1.0;
            }
        }

        return 0;
    }
}