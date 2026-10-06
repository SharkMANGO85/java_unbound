package com.java_unbound.molang;

import com.java_unbound.molang.enums.MolangExpression;
import com.java_unbound.molang.types.MolangEntityParser;
import com.java_unbound.molang.types.MolangMathParser;
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

        if (Expression.matches("[qvt]\\.[A-Za-z0-9_]+\\s*=.*")) {
            return new MolangExpression(MolangExpression.Type.ASSIGNMENT, Expression);
        }

        if (IsNumber(Expression)) {
            return new MolangExpression(MolangExpression.Type.NUMBER, Expression);
        }

        if (Expression.matches("[qvt]\\.[A-Za-z0-9_]+")) {
            return new MolangExpression(MolangExpression.Type.VALUE, Expression);
        }

        if (MolangMathParser.IsMathExpression(Expression)) {
            return new MolangExpression(MolangExpression.Type.MATH, Expression);
        }

        return new MolangExpression(MolangExpression.Type.UNKNOWN, Expression);
    }

    public static double Evaluate(Entity Entity, String Expression) {
        if (Entity == null || Expression == null || Expression.isBlank()) {
            return 0;
        }

        Expression = Expression.trim();

        if (Expression.endsWith(";")) {
            Expression = Expression.substring(0, Expression.length() - 1).trim();
        }

        if (Expression.isEmpty()) {
            return 0;
        }

        while (IsWrapped(Expression)) {
            Expression = Expression.substring(1, Expression.length() - 1).trim();
        }

        int StatementQuestionIndex = FindTopLevelTernaryQuestion(Expression);

        if (StatementQuestionIndex >= 0) {
            String Condition = Expression.substring(0, StatementQuestionIndex).trim();
            String Remaining = Expression.substring(StatementQuestionIndex + 1).trim();

            if (Remaining.startsWith("{") && Remaining.endsWith("}") && IsWrappedBlock(Remaining)) {
                if (IsTrue(Evaluate(Entity, Condition))) {
                    return EvaluateBlock(Entity, Remaining.substring(1, Remaining.length() - 1));
                }

                return 0;
            }
        }

        int AssignmentIndex = FindTopLevelAssignment(Expression);

        if (AssignmentIndex >= 0) {
            String Left = Expression.substring(0, AssignmentIndex).trim();
            String Right = Expression.substring(AssignmentIndex + 1).trim();

            if (Left.startsWith("v.") || Left.startsWith("t.")) {
                double Value = Evaluate(Entity, Right);
                MolangEntityParser.SetVariable(Entity, Left.substring(2).trim(), Value);
                return Value;
            }
        }

        int TernaryIndex = FindTopLevelTernaryQuestion(Expression);

        if (TernaryIndex >= 0) {
            String Condition = Expression.substring(0, TernaryIndex).trim();
            String Remaining = Expression.substring(TernaryIndex + 1);

            int ColonIndex = FindMatchingTernaryColon(Remaining);

            if (ColonIndex >= 0) {
                String TrueExpression = Remaining.substring(0, ColonIndex).trim();
                String FalseExpression = Remaining.substring(ColonIndex + 1).trim();

                return IsTrue(Evaluate(Entity, Condition)) ? Evaluate(Entity, TrueExpression) : Evaluate(Entity, FalseExpression);
            }
        }

        int Index = FindTopLevel(Expression, "??");

        if (Index >= 0) {
            double Left = Evaluate(Entity, Expression.substring(0, Index));
            return Left != 0 ? Left : Evaluate(Entity, Expression.substring(Index + 2));
        }

        List<String> Parts = SplitTopLevel(Expression, "||");

        if (Parts.size() > 1) {
            for (String Part : Parts) {
                if (IsTrue(Evaluate(Entity, Part))) {
                    return 1;
                }
            }

            return 0;
        }

        Parts = SplitTopLevel(Expression, "&&");

        if (Parts.size() > 1) {
            for (String Part : Parts) {
                if (!IsTrue(Evaluate(Entity, Part))) {
                    return 0;
                }
            }

            return 1;
        }

        if (Expression.startsWith("!") && !Expression.startsWith("!=")) {
            return IsTrue(Evaluate(Entity, Expression.substring(1))) ? 0 : 1;
        }

        String[] ComparisonOperators = {"==", "!=", ">=", "<=", ">", "<"};

        for (String Operator : ComparisonOperators) {
            Index = FindTopLevel(Expression, Operator);

            if (Index >= 0) {
                double Left = Evaluate(Entity, Expression.substring(0, Index));
                double Right = Evaluate(Entity, Expression.substring(Index + Operator.length()));

                return switch (Operator) {
                    case "==" -> Left == Right ? 1 : 0;
                    case "!=" -> Left != Right ? 1 : 0;
                    case ">=" -> Left >= Right ? 1 : 0;
                    case "<=" -> Left <= Right ? 1 : 0;
                    case ">" -> Left > Right ? 1 : 0;
                    case "<" -> Left < Right ? 1 : 0;
                    default -> 0;
                };
            }
        }

        Index = FindTopLevelRightToLeft(Expression, "+", "-");

        if (Index >= 0) {
            char Operator = Expression.charAt(Index);
            double Left = Evaluate(Entity, Expression.substring(0, Index));
            double Right = Evaluate(Entity, Expression.substring(Index + 1));
            return Operator == '+' ? Left + Right : Left - Right;
        }

        Index = FindTopLevelRightToLeft(Expression, "*", "/", "%");

        if (Index >= 0) {
            char Operator = Expression.charAt(Index);
            double Left = Evaluate(Entity, Expression.substring(0, Index));
            double Right = Evaluate(Entity, Expression.substring(Index + 1));

            return switch (Operator) {
                case '*' -> Left * Right;
                case '/' -> Right == 0 ? 0 : Left / Right;
                case '%' -> Right == 0 ? 0 : Left % Right;
                default -> 0;
            };
        }

        if (Expression.startsWith("-")) {
            return -Evaluate(Entity, Expression.substring(1));
        }

        if (Expression.startsWith("+")) {
            return Evaluate(Entity, Expression.substring(1));
        }

        if (IsNumber(Expression)) {
            return Double.parseDouble(Expression);
        }

        if (MolangMathParser.IsMathExpression(Expression)) {
            return MolangMathParser.Evaluate(Entity, Expression);
        }

        if (Expression.startsWith("q.")) {
            return MolangEntityParser.Evaluate(Entity, Expression);
        }

        if (Expression.startsWith("v.")) {
            return MolangEntityParser.Evaluate(Entity, Expression);
        }

        if (Expression.startsWith("t.")) {
            return MolangEntityParser.Evaluate(Entity, Expression);
        }

        return 0;
    }

    public static boolean IsTrue(double Value) {
        return Value != 0 && !Double.isNaN(Value);
    }

    public static boolean IsNumber(String Expression) {
        return Expression != null && Expression.trim().matches("[+-]?(\\d+(\\.\\d*)?|\\.\\d+)([eE][+-]?\\d+)?");
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

            if (Character == '(' || Character == '{') {
                Depth++;
            } else if (Character == ')' || Character == '}') {
                Depth--;
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
            } else if (Character == '(' || Character == '{') {
                Depth++;
            } else if (Character == ')' || Character == '}') {
                Depth--;

                if (Depth < 0) {
                    return false;
                }
            }
        }

        return Depth == 0 && Quote == 0;
    }

    private static List<String> SplitTopLevel(String Expression, String Operator) {
        List<String> Result = new ArrayList<>();
        int Depth = 0;
        char Quote = 0;
        int Start = 0;

        for (int Index = 0; Index <= Expression.length() - Operator.length(); Index++) {
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

            if (Character == '(' || Character == '{') {
                Depth++;
                continue;
            }

            if (Character == ')' || Character == '}') {
                Depth--;
                continue;
            }

            if (Depth == 0 && Expression.startsWith(Operator, Index)) {
                Result.add(Expression.substring(Start, Index).trim());
                Start = Index + Operator.length();
                Index += Operator.length() - 1;
            }
        }

        if (!Result.isEmpty()) {
            Result.add(Expression.substring(Start).trim());
        }

        return Result;
    }

    private static double EvaluateBlock(Entity Entity, String Block) {
        List<String> Statements = SplitStatements(Block);
        double Result = 0;

        for (String Statement : Statements) {
            Statement = Statement.trim();

            if (Statement.isEmpty()) {
                continue;
            }

            Result = Evaluate(Entity, Statement);
        }

        return Result;
    }

    private static List<String> SplitStatements(String Expression) {
        List<String> Result = new ArrayList<>();
        StringBuilder Current = new StringBuilder();

        int ParenthesisDepth = 0;
        int BlockDepth = 0;
        char Quote = 0;

        for (int Index = 0; Index < Expression.length(); Index++) {
            char Character = Expression.charAt(Index);

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
                ParenthesisDepth++;
            } else if (Character == ')') {
                ParenthesisDepth--;
            } else if (Character == '{') {
                BlockDepth++;
            } else if (Character == '}') {
                BlockDepth--;
            }

            if (Character == ';' && ParenthesisDepth == 0 && BlockDepth == 0) {
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

    private static int FindTopLevelAssignment(String Expression) {
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

            if (Character == '(' || Character == '{') {
                Depth++;
                continue;
            }

            if (Character == ')' || Character == '}') {
                Depth--;
                continue;
            }

            if (Depth != 0 || Character != '=') {
                continue;
            }

            char Previous = Index > 0 ? Expression.charAt(Index - 1) : 0;
            char Next = Index + 1 < Expression.length() ? Expression.charAt(Index + 1) : 0;

            if (Previous == '=' || Previous == '!' || Previous == '<' || Previous == '>' || Next == '=') {
                continue;
            }

            return Index;
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
            } else if (Character == '(') {
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

    private static boolean IsWrappedBlock(String Expression) {
        if (!Expression.startsWith("{") || !Expression.endsWith("}")) {
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
            } else if (Character == '{') {
                Depth++;
            } else if (Character == '}') {
                Depth--;

                if (Depth == 0 && Index != Expression.length() - 1) {
                    return false;
                }
            }
        }

        return Depth == 0;
    }

    private static int FindTopLevel(String Expression, String Operator) {
        int Depth = 0;
        char Quote = 0;

        for (int Index = 0; Index <= Expression.length() - Operator.length(); Index++) {
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

            if (Character == '(' || Character == '{') {
                Depth++;
                continue;
            }

            if (Character == ')' || Character == '}') {
                Depth--;
                continue;
            }

            if (Depth == 0 && Expression.startsWith(Operator, Index)) {
                if ((Operator.equals("+") || Operator.equals("-")) && (Index == 0 || "+-*/%(<>=!&|?".indexOf(Expression.charAt(Index - 1)) >= 0)) {
                    continue;
                }

                return Index;
            }
        }

        return -1;
    }

    private static int FindTopLevelRightToLeft(String Expression, String... Operators) {
        int Depth = 0;
        char Quote = 0;

        for (int Index = Expression.length() - 1; Index >= 0; Index--) {
            char Character = Expression.charAt(Index);

            if (Character == '\'' || Character == '"') {
                if (Quote == 0) {
                    Quote = Character;
                } else if (Character == Quote) {
                    Quote = 0;
                }

                continue;
            }

            if (Quote != 0) {
                continue;
            }

            if (Character == ')' || Character == '}') {
                Depth++;
                continue;
            }

            if (Character == '(' || Character == '{') {
                Depth--;
                continue;
            }

            if (Depth != 0) {
                continue;
            }

            for (String Operator : Operators) {
                if (Character == Operator.charAt(0)) {
                    if ((Character == '+' || Character == '-') && (Index == 0 || "+-*/%(<>=!&|?".indexOf(Expression.charAt(Index - 1)) >= 0)) {
                        continue;
                    }

                    if ((Character == '*' || Character == '/' || Character == '%') && Index == 0) {
                        continue;
                    }

                    return Index;
                }
            }
        }

        return -1;
    }

    private static int FindTopLevelTernaryQuestion(String Expression) {
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

            if (Character == '(' || Character == '{') {
                Depth++;
                continue;
            }

            if (Character == ')' || Character == '}') {
                Depth--;
                continue;
            }

            if (Depth == 0 && Character == '?') {
                if (Index + 1 < Expression.length() && Expression.charAt(Index + 1) == '?') {
                    Index++;
                    continue;
                }

                if (Index > 0 && Expression.charAt(Index - 1) == '?') {
                    continue;
                }

                return Index;
            }
        }

        return -1;
    }

    private static int FindMatchingTernaryColon(String Expression) {
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

            if (Character == '(' || Character == '{') {
                Depth++;
                continue;
            }

            if (Character == ')' || Character == '}') {
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

                if (Index > 0 && Expression.charAt(Index - 1) == '?') {
                    continue;
                }

                TernaryDepth++;
            } else if (Character == ':') {
                TernaryDepth--;

                if (TernaryDepth == 0) {
                    return Index;
                }
            }
        }

        return -1;
    }
}