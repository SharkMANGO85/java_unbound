package com.java_unbound.molang.types;

import com.java_unbound.entities.interfaces.EntityVariableInterface;
import com.java_unbound.entities.render_controller.EntityRenderControllerRegistry;
import com.java_unbound.molang.MolangParser;
import net.minecraft.world.entity.Entity;

import java.util.List;
import java.util.Map;

//for render controllers especially for Array.Textures[v.ssss] to resolve it correctly
public final class MolangRenderControllerParser {
    private MolangRenderControllerParser() {}

    public static String Evaluate(Entity Entity, String Expression, EntityRenderControllerRegistry Registry) {
        if (Entity == null || Expression == null || Registry == null || Expression.isBlank()) {
            return "";
        }

        Expression = Expression.trim();

        while (IsWrapped(Expression)) {
            Expression = Expression.substring(1, Expression.length() - 1).trim();
        }

        int QuestionIndex = FindTernaryQuestion(Expression);

        if (QuestionIndex >= 0) {
            String Condition = Expression.substring(0, QuestionIndex).trim();
            String Remaining = Expression.substring(QuestionIndex + 1).trim();

            int ColonIndex = FindTernaryColon(Remaining);

            if (ColonIndex >= 0) {
                String TrueExpression = Remaining.substring(0, ColonIndex).trim();
                String FalseExpression = Remaining.substring(ColonIndex + 1).trim();

                return Evaluate(Entity, MolangParser.IsTrue(MolangParser.Evaluate(Entity, Condition)) ? TrueExpression : FalseExpression, Registry);
            }
        }

        if (Expression.startsWith("Array.")) {
            int OpenBracketIndex = Expression.indexOf('[');

            if (OpenBracketIndex < 0 || !Expression.endsWith("]")) {
                return Expression;
            }

            String ArrayIdentifier = Expression.substring(0, OpenBracketIndex).trim();
            String IndexExpression = Expression.substring(OpenBracketIndex + 1, Expression.length() - 1).trim();

            if (ArrayIdentifier.isBlank() || IndexExpression.isBlank()) {
                return "";
            }

            String StringValue = EvaluateString(Entity, IndexExpression);

            if (StringValue != null) {
                return FindArrayValue(Registry, ArrayIdentifier, StringValue);
            }

            return FindArrayValue(Registry, ArrayIdentifier, (int) MolangParser.Evaluate(Entity, IndexExpression));
        }

        return MolangParser.Unquote(Expression);
    }

    private static String EvaluateString(Entity Entity, String Expression) {
        Expression = Expression.trim();

        if ((Expression.startsWith("\"") && Expression.endsWith("\"")) || (Expression.startsWith("'") && Expression.endsWith("'"))) {
            return MolangParser.Unquote(Expression);
        }

        if (Expression.startsWith("v.") || Expression.startsWith("t.")) {
            Object Value = ((EntityVariableInterface) Entity).JavaUnbound$getVariableObject(Expression.substring(2).trim());

            if (Value instanceof String StringValue) {
                return StringValue;
            }
        }

        return null;
    }

    private static String FindArrayValue(EntityRenderControllerRegistry Registry, String ArrayIdentifier, int Index) {
        if (Index < 0) {
            return "";
        }

        for (Map<String, List<String>> Category : Registry.Arrays.values()) {
            List<String> Values = Category.get(ArrayIdentifier);

            if (Values != null && Index < Values.size()) {
                return Values.get(Index);
            }
        }

        return "";
    }

    private static String FindArrayValue(EntityRenderControllerRegistry Registry, String ArrayIdentifier, String Value) {
        for (Map<String, List<String>> Category : Registry.Arrays.values()) {
            List<String> Values = Category.get(ArrayIdentifier);

            if (Values != null && Values.contains(Value)) {
                return Value;
            }
        }

        return "";
    }

    private static int FindTernaryQuestion(String Expression) {
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

            if (Character == '(' || Character == '[' || Character == '{') {
                Depth++;
                continue;
            }

            if (Character == ')' || Character == ']' || Character == '}') {
                Depth--;
                continue;
            }

            if (Depth == 0 && Character == '?') {
                if (Index + 1 < Expression.length() && Expression.charAt(Index + 1) == '?') {
                    Index++;
                    continue;
                }

                return Index;
            }
        }

        return -1;
    }

    private static int FindTernaryColon(String Expression) {
        int Depth = 0;
        int TernaryDepth = 1;
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

            if (Character == '(' || Character == '[' || Character == '{') {
                Depth++;
                continue;
            }

            if (Character == ')' || Character == ']' || Character == '}') {
                Depth--;
                continue;
            }

            if (Depth != 0) {
                continue;
            }

            if (Character == '?') {
                if (Index + 1 < Expression.length() && Expression.charAt(Index + 1) == '?') {
                    Index++;
                    continue;
                }

                TernaryDepth++;
            } else if (Character == ':' && --TernaryDepth == 0) {
                return Index;
            }
        }

        return -1;
    }

    private static boolean IsWrapped(String Expression) {
        if (!Expression.startsWith("(") || !Expression.endsWith(")")) {
            return false;
        }

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
            } else if (Character == ')') {
                Depth--;

                if (Depth == 0 && Index != Expression.length() - 1) {
                    return false;
                }
            }
        }

        return Depth == 0;
    }
}