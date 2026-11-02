package com.java_unbound.molang;

import com.java_unbound.molang.enums.MolangExpression;
import com.java_unbound.molang.types.MolangEntityParser;
import com.java_unbound.molang.types.MolangMathParser;
import net.minecraft.world.entity.Entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

// Evaluates MoLang expressions, including math, conditions, variables, assignments, and render controller expressions.
public final class MolangParser {
    private static final ThreadLocal<Integer> EVALUATION_DEPTH = ThreadLocal.withInitial(() -> 0);

    private MolangParser() {
    }

    public static MolangExpression Classify(String Expression) {
        if (Expression == null) {
            return new MolangExpression(MolangExpression.Type.UNKNOWN, "");
        }

        Expression = Expression.trim();

        if (Expression.matches("(?i)(?:v|variable|t|temp)\\.[A-Za-z0-9_]+\\s*=(?!=).*")) {
            return new MolangExpression(MolangExpression.Type.ASSIGNMENT, Expression);
        }

        if (IsNumber(Expression) || Expression.equalsIgnoreCase("true") || Expression.equalsIgnoreCase("false")) {
            return new MolangExpression(MolangExpression.Type.NUMBER, Expression);
        }

        if (Expression.matches("(?i)(?:q|query|v|variable|t|temp|c|context)\\.[A-Za-z0-9_]+")) {
            return new MolangExpression(MolangExpression.Type.VALUE, Expression);
        }

        if (MolangMathParser.IsMathExpression(Expression) || Expression.equalsIgnoreCase("math.pi")) {
            return new MolangExpression(MolangExpression.Type.MATH, Expression);
        }

        return new MolangExpression(MolangExpression.Type.UNKNOWN, Expression);
    }

    public static String Evaluate(Entity Entity, String Expression) {
        boolean IsRootEvaluation = EVALUATION_DEPTH.get() == 0;

        if (IsRootEvaluation) {
            MolangEntityParser.ClearTemporaries(Entity);
        }

        EVALUATION_DEPTH.set(EVALUATION_DEPTH.get() + 1);

        try {
            return EvaluateInternal(Entity, Expression);
        } catch (RuntimeException Exception) {
            return "0";
        } finally {
            int Depth = EVALUATION_DEPTH.get() - 1;

            if (Depth <= 0) {
                MolangEntityParser.ClearTemporaries(Entity);
                EVALUATION_DEPTH.remove();
            } else {
                EVALUATION_DEPTH.set(Depth);
            }
        }
    }

    public static boolean IsConditionMet(Entity Entity, String Condition) {
        if (Condition == null || Condition.isBlank()) {
            return true;
        }

        return MolangParser.IsTrue(MolangParser.Evaluate(Entity, Condition));
    }

    private static String EvaluateInternal(Entity Entity, String Expression) {
        if (Expression == null || Expression.isBlank()) {
            return "0";
        }

        if (Entity == null) {
            return "0";
        }

        Expression = Expression.trim();

        if (!IsBalanced(Expression)) {
            return "0";
        }

        List<String> Statements = SplitStatements(Expression);

        if (Statements.size() > 1 || (Expression.endsWith(";") && Statements.size() == 1)) {
            String ReturnValue = "0";

            for (String Statement : Statements) {
                Statement = Statement.trim();

                if (Statement.isEmpty()) {
                    continue;
                }

                if (Statement.regionMatches(true, 0, "return", 0, 6)
                        && (Statement.length() == 6 || Character.isWhitespace(Statement.charAt(6)))) {
                    return EvaluateInternal(Entity, Statement.length() == 6 ? "0" : Statement.substring(6).trim());
                }

                EvaluateInternal(Entity, Statement);
            }

            return ReturnValue;
        }

        if (Expression.regionMatches(true, 0, "return", 0, 6)
                && (Expression.length() == 6 || Character.isWhitespace(Expression.charAt(6)))) {
            return EvaluateInternal(Entity, Expression.length() == 6 ? "0" : Expression.substring(6).trim());
        }

        while (IsWrapped(Expression)) {
            Expression = Expression.substring(1, Expression.length() - 1).trim();
        }

        if (Expression.isEmpty()) {
            return "0";
        }

        if (IsStringLiteral(Expression)) {
            return Unquote(Expression);
        }

        if (Expression.equalsIgnoreCase("true")) {
            return "1";
        }

        if (Expression.equalsIgnoreCase("false")) {
            return "0";
        }

        // Assignment has lower precedence than all value operators, so split it before evaluating the right side.
        int AssignmentIndex = FindTopLevelAssignment(Expression);

        if (AssignmentIndex >= 0) {
            String Left = Expression.substring(0, AssignmentIndex).trim();
            String Right = Expression.substring(AssignmentIndex + 1).trim();
            String NormalizedLeft = Left.toLowerCase(Locale.ROOT);

            if (IsWritableVariable(NormalizedLeft)) {
                String Value = EvaluateInternal(Entity, Right);
                String VariableName = Left.substring(Left.indexOf('.') + 1).trim().toLowerCase(Locale.ROOT);

                if (NormalizedLeft.startsWith("t.") || NormalizedLeft.startsWith("temp.")) {
                    MolangEntityParser.SetTemporary(Entity, VariableName, Value);
                } else {
                    MolangEntityParser.SetVariable(Entity, VariableName, Value);
                }

                return Value;
            }
        }

        // Null coalescing binds more weakly than ternary and logical operators.
        int Index = FindTopLevel(Expression, "??");

        if (Index >= 0) {
            String LeftExpression = Expression.substring(0, Index).trim();
            String NormalizedLeft = StripOuterParentheses(LeftExpression).toLowerCase(Locale.ROOT);
            boolean IsVariableReference = NormalizedLeft.startsWith("v.")
                    || NormalizedLeft.startsWith("variable.")
                    || NormalizedLeft.startsWith("t.")
                    || NormalizedLeft.startsWith("temp.");
            boolean IsMissing = IsVariableReference && GetMissingVariableValue(Entity, LeftExpression) == null;

            return IsMissing
                    ? EvaluateInternal(Entity, Expression.substring(Index + 2).trim())
                    : EvaluateInternal(Entity, LeftExpression);
        }

        int TernaryIndex = FindTopLevelTernaryQuestion(Expression);

        if (TernaryIndex >= 0) {
            String Condition = Expression.substring(0, TernaryIndex).trim();
            String Remaining = Expression.substring(TernaryIndex + 1).trim();

            if (Remaining.startsWith("{") && Remaining.endsWith("}") && IsWrappedBlock(Remaining)) {
                if (IsTrue(EvaluateInternal(Entity, Condition))) {
                    return EvaluateBlock(Entity, Remaining.substring(1, Remaining.length() - 1));
                }

                return "0";
            }

            int ColonIndex = FindMatchingTernaryColon(Remaining);

            if (ColonIndex >= 0) {
                String TrueExpression = Remaining.substring(0, ColonIndex).trim();
                String FalseExpression = Remaining.substring(ColonIndex + 1).trim();

                return IsTrue(EvaluateInternal(Entity, Condition))
                        ? EvaluateInternal(Entity, TrueExpression)
                        : EvaluateInternal(Entity, FalseExpression);
            }

            // Binary conditional: A ? B executes B only when A is true.
            return IsTrue(EvaluateInternal(Entity, Condition))
                    ? EvaluateInternal(Entity, Remaining)
                    : "0";
        }

        List<String> Parts = SplitTopLevel(Expression, "||");

        if (Parts.size() > 1) {
            for (String Part : Parts) {
                if (IsTrue(EvaluateInternal(Entity, Part))) {
                    return "1";
                }
            }

            return "0";
        }

        Parts = SplitTopLevel(Expression, "&&");

        if (Parts.size() > 1) {
            for (String Part : Parts) {
                if (!IsTrue(EvaluateInternal(Entity, Part))) {
                    return "0";
                }
            }

            return "1";
        }

        if (Expression.startsWith("!") && !Expression.startsWith("!=")) {
            int OperandEnd = FindUnaryOperandEnd(Expression, 1);

            if (OperandEnd <= 1) {
                return "0";
            }

            String Operand = Expression.substring(1, OperandEnd).trim();
            String Remaining = Expression.substring(OperandEnd).trim();
            String Value = IsTrue(EvaluateInternal(Entity, Operand)) ? "0" : "1";

            return Remaining.isEmpty() ? Value : EvaluateInternal(Entity, Value + " " + Remaining);
        }

        // Relational operators have higher precedence than equality operators.
        String[] ComparisonOperators = {"==", "!=", ">=", "<=", ">", "<"};

        for (String Operator : ComparisonOperators) {
            Index = FindTopLevel(Expression, Operator);

            if (Index >= 0) {
                String LeftExpression = Expression.substring(0, Index).trim();
                String RightExpression = Expression.substring(Index + Operator.length()).trim();
                String LeftString = EvaluateInternal(Entity, LeftExpression);
                String RightString = EvaluateInternal(Entity, RightExpression);
                boolean LeftIsString = IsStringLiteral(LeftExpression) || !IsNumber(LeftString);
                boolean RightIsString = IsStringLiteral(RightExpression) || !IsNumber(RightString);

                if (Operator.equals("==") || Operator.equals("!=")) {
                    boolean Equal = LeftIsString || RightIsString
                            ? LeftString.equals(RightString)
                            : Double.compare(ParseDouble(LeftString), ParseDouble(RightString)) == 0;

                    return (Operator.equals("==") == Equal) ? "1" : "0";
                }

                double Left = ParseDouble(LeftString);
                double Right = ParseDouble(RightString);

                return switch (Operator) {
                    case ">=" -> Left >= Right ? "1" : "0";
                    case "<=" -> Left <= Right ? "1" : "0";
                    case ">" -> Left > Right ? "1" : "0";
                    case "<" -> Left < Right ? "1" : "0";
                    default -> "0";
                };
            }
        }

        Index = FindTopLevelRightToLeft(Expression, "+", "-");

        if (Index >= 0) {
            char Operator = Expression.charAt(Index);
            double Left = ParseDouble(EvaluateInternal(Entity, Expression.substring(0, Index)));
            double Right = ParseDouble(EvaluateInternal(Entity, Expression.substring(Index + 1)));

            return String.valueOf(Operator == '+' ? Left + Right : Left - Right);
        }

        Index = FindTopLevelRightToLeft(Expression, "*", "/", "%");

        if (Index >= 0) {
            char Operator = Expression.charAt(Index);
            double Left = ParseDouble(EvaluateInternal(Entity, Expression.substring(0, Index)));
            double Right = ParseDouble(EvaluateInternal(Entity, Expression.substring(Index + 1)));

            return switch (Operator) {
                case '*' -> String.valueOf(Left * Right);
                case '/' -> Right == 0 ? "0" : String.valueOf(Left / Right);
                case '%' -> Right == 0 ? "0" : String.valueOf(Left % Right);
                default -> "0";
            };
        }

        if (Expression.startsWith("-")) {
            return String.valueOf(-ParseDouble(EvaluateInternal(Entity, Expression.substring(1).trim())));
        }

        if (Expression.startsWith("+")) {
            return EvaluateInternal(Entity, Expression.substring(1).trim());
        }

        if (IsNumber(Expression)) {
            return Expression;
        }

        if (Expression.equalsIgnoreCase("math.pi")) {
            return String.valueOf(Math.PI);
        }

        if (MolangMathParser.IsMathExpression(Expression)) {
            return MolangMathParser.Evaluate(Entity, Expression);
        }

        String LowerExpression = Expression.toLowerCase(Locale.ROOT);

        if (LowerExpression.startsWith("v.") || LowerExpression.startsWith("variable.")) {
            return MolangEntityParser.Evaluate(Entity, Expression);
        }

        if (LowerExpression.startsWith("t.") || LowerExpression.startsWith("temp.")) {
            String VariableName = Expression.substring(Expression.indexOf('.') + 1).trim();

            return MolangEntityParser.Evaluate(Entity, Expression);
        }

        if (LowerExpression.startsWith("q.") || LowerExpression.startsWith("query.")) {
            return MolangEntityParser.Evaluate(Entity, Expression);
        }

        if (LowerExpression.startsWith("c.") || LowerExpression.startsWith("context.")) {
            return "0";
        }

        return "0";
    }

    public static boolean IsTrue(String Value) {
        double Number = ParseDouble(Value);

        return Number != 0 && !Double.isNaN(Number);
    }

    public static boolean IsNumber(String Expression) {
        return Expression != null
                && Expression.trim().matches("[+-]?(\\d+(\\.\\d*)?|\\.\\d+)([eE][+-]?\\d+)?");
    }

    public static String GetArguments(String Expression) {
        if (Expression == null) {
            return "";
        }

        int OpenIndex = Expression.indexOf('(');

        if (OpenIndex < 0 || !Expression.endsWith(")") || !IsBalanced(Expression)) {
            return "";
        }

        int ClosingIndex = FindMatchingClosing(Expression, OpenIndex);

        if (ClosingIndex != Expression.length() - 1) {
            return "";
        }

        return Expression.substring(OpenIndex + 1, ClosingIndex);
    }

    public static String Unquote(String Value) {
        if (Value == null) {
            return "";
        }

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

        if (Arguments == null || Arguments.isBlank()) {
            return Result;
        }

        StringBuilder Current = new StringBuilder();
        int Depth = 0;
        char Quote = 0;
        boolean Escaped = false;

        for (int Index = 0; Index < Arguments.length(); Index++) {
            char Character = Arguments.charAt(Index);

            if (Quote != 0) {
                Current.append(Character);

                if (Escaped) {
                    Escaped = false;
                } else if (Character == '\\') {
                    Escaped = true;
                } else if (Character == Quote) {
                    Quote = 0;
                }

                continue;
            }

            if (Character == '\'' || Character == '"') {
                Quote = Character;
                Current.append(Character);
                continue;
            }

            if (Character == '(' || Character == '{' || Character == '[') {
                Depth++;
            } else if (Character == ')' || Character == '}' || Character == ']') {
                Depth--;

                if (Depth < 0) {
                    return List.of();
                }
            }

            if (Character == ',' && Depth == 0) {
                Result.add(Current.toString().trim());
                Current.setLength(0);
                continue;
            }

            Current.append(Character);
        }

        if (Quote != 0 || Depth != 0) {
            return List.of();
        }

        if (!Current.isEmpty()) {
            Result.add(Current.toString().trim());
        }

        return Result;
    }

    public static boolean IsBalanced(String Expression) {
        if (Expression == null) {
            return false;
        }

        List<Character> Stack = new ArrayList<>();
        char Quote = 0;
        boolean Escaped = false;

        for (int Index = 0; Index < Expression.length(); Index++) {
            char Character = Expression.charAt(Index);

            if (Quote != 0) {
                if (Escaped) {
                    Escaped = false;
                } else if (Character == '\\') {
                    Escaped = true;
                } else if (Character == Quote) {
                    Quote = 0;
                }

                continue;
            }

            if (Character == '\'' || Character == '"') {
                Quote = Character;
            } else if (Character == '(' || Character == '{' || Character == '[') {
                Stack.add(Character);
            } else if (Character == ')' || Character == '}' || Character == ']') {
                if (Stack.isEmpty()) {
                    return false;
                }

                char Open = Stack.removeLast();

                if ((Open == '(' && Character != ')')
                        || (Open == '{' && Character != '}')
                        || (Open == '[' && Character != ']')) {
                    return false;
                }
            }
        }

        return Stack.isEmpty() && Quote == 0;
    }

    private static String StripOuterParentheses(String Expression) {
        while (IsWrapped(Expression)) {
            Expression = Expression.substring(1, Expression.length() - 1).trim();
        }

        return Expression;
    }

    private static String GetMissingVariableValue(Entity Entity, String Expression) {
        Expression = StripOuterParentheses(Expression);

        String LowerExpression = Expression.toLowerCase(Locale.ROOT);

        if (LowerExpression.startsWith("v.") || LowerExpression.startsWith("variable.")) {
            return MolangEntityParser.GetVariable(Entity, Expression.substring(Expression.indexOf('.') + 1));
        }

        if (LowerExpression.startsWith("t.") || LowerExpression.startsWith("temp.")) {
            return MolangEntityParser.GetTemporary(
                    Entity,
                    Expression.substring(Expression.indexOf('.') + 1)
            );
        }

        return null;
    }

    private static boolean IsWritableVariable(String Expression) {
        return Expression.matches("(?:v|variable|t|temp)\\.[a-z0-9_]+");
    }

    private static boolean IsStringLiteral(String Expression) {
        if (Expression == null || Expression.length() < 2) {
            return false;
        }

        char Quote = Expression.charAt(0);

        if ((Quote != '\'' && Quote != '"') || Expression.charAt(Expression.length() - 1) != Quote) {
            return false;
        }

        for (int Index = 1; Index < Expression.length() - 1; Index++) {
            if (Expression.charAt(Index) == Quote && !IsEscaped(Expression, Index)) {
                return false;
            }
        }

        return !IsEscaped(Expression, Expression.length() - 1);
    }

    private static List<String> SplitTopLevel(String Expression, String Operator) {
        List<String> Result = new ArrayList<>();
        int Depth = 0;
        char Quote = 0;
        boolean Escaped = false;
        int Start = 0;

        for (int Index = 0; Index <= Expression.length() - Operator.length(); Index++) {
            char Character = Expression.charAt(Index);

            if (Quote != 0) {
                if (Escaped) {
                    Escaped = false;
                } else if (Character == '\\') {
                    Escaped = true;
                } else if (Character == Quote) {
                    Quote = 0;
                }

                continue;
            }

            if (Character == '\'' || Character == '"') {
                Quote = Character;
                continue;
            }

            if (Character == '(' || Character == '{' || Character == '[') {
                Depth++;
                continue;
            }

            if (Character == ')' || Character == '}' || Character == ']') {
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

    private static String EvaluateBlock(Entity Entity, String Block) {
        List<String> Statements = SplitStatements(Block);
        String Result = "0";
        boolean HasMultipleStatements = Statements.size() > 1;

        for (String Statement : Statements) {
            Statement = Statement.trim();

            if (Statement.isEmpty()) {
                continue;
            }

            if (Statement.regionMatches(true, 0, "return", 0, 6)
                    && (Statement.length() == 6 || Character.isWhitespace(Statement.charAt(6)))) {
                return EvaluateInternal(Entity, Statement.length() == 6 ? "0" : Statement.substring(6).trim());
            }

            Result = EvaluateInternal(Entity, Statement);
        }

        return HasMultipleStatements ? "0" : Result;
    }

    private static List<String> SplitStatements(String Expression) {
        List<String> Result = new ArrayList<>();
        StringBuilder Current = new StringBuilder();
        int ParenthesisDepth = 0;
        int BlockDepth = 0;
        int ArrayDepth = 0;
        char Quote = 0;
        boolean Escaped = false;

        for (int Index = 0; Index < Expression.length(); Index++) {
            char Character = Expression.charAt(Index);

            if (Quote != 0) {
                Current.append(Character);

                if (Escaped) {
                    Escaped = false;
                } else if (Character == '\\') {
                    Escaped = true;
                } else if (Character == Quote) {
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
            } else if (Character == '[') {
                ArrayDepth++;
            } else if (Character == ']') {
                ArrayDepth--;
            }

            if (Character == ';' && ParenthesisDepth == 0 && BlockDepth == 0 && ArrayDepth == 0) {
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
        boolean Escaped = false;

        for (int Index = 0; Index < Expression.length(); Index++) {
            char Character = Expression.charAt(Index);

            if (Quote != 0) {
                if (Escaped) {
                    Escaped = false;
                } else if (Character == '\\') {
                    Escaped = true;
                } else if (Character == Quote) {
                    Quote = 0;
                }

                continue;
            }

            if (Character == '\'' || Character == '"') {
                Quote = Character;
                continue;
            }

            if (Character == '(' || Character == '{' || Character == '[') {
                Depth++;
                continue;
            }

            if (Character == ')' || Character == '}' || Character == ']') {
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
        if (Expression.length() < 2 || !Expression.startsWith("(") || !Expression.endsWith(")")) {
            return false;
        }

        int Depth = 0;
        char Quote = 0;
        boolean Escaped = false;

        for (int Index = 0; Index < Expression.length(); Index++) {
            char Character = Expression.charAt(Index);

            if (Quote != 0) {
                if (Escaped) {
                    Escaped = false;
                } else if (Character == '\\') {
                    Escaped = true;
                } else if (Character == Quote) {
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

    private static boolean IsWrappedBlock(String Expression) {
        if (Expression.length() < 2 || !Expression.startsWith("{") || !Expression.endsWith("}")) {
            return false;
        }

        int Depth = 0;
        char Quote = 0;
        boolean Escaped = false;

        for (int Index = 0; Index < Expression.length(); Index++) {
            char Character = Expression.charAt(Index);

            if (Quote != 0) {
                if (Escaped) {
                    Escaped = false;
                } else if (Character == '\\') {
                    Escaped = true;
                } else if (Character == Quote) {
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

        return Depth == 0 && Quote == 0;
    }

    private static int FindTopLevel(String Expression, String Operator) {
        int Depth = 0;
        char Quote = 0;
        boolean Escaped = false;

        for (int Index = 0; Index <= Expression.length() - Operator.length(); Index++) {
            char Character = Expression.charAt(Index);

            if (Quote != 0) {
                if (Escaped) {
                    Escaped = false;
                } else if (Character == '\\') {
                    Escaped = true;
                } else if (Character == Quote) {
                    Quote = 0;
                }

                continue;
            }

            if (Character == '\'' || Character == '"') {
                Quote = Character;
                continue;
            }

            if (Character == '(' || Character == '{' || Character == '[') {
                Depth++;
                continue;
            }

            if (Character == ')' || Character == '}' || Character == ']') {
                Depth--;
                continue;
            }

            if (Depth == 0 && Expression.startsWith(Operator, Index)) {
                if (Operator.equals("+") || Operator.equals("-")) {
                    int PreviousIndex = PreviousNonWhitespaceIndex(Expression, Index);

                    if (PreviousIndex < 0 || "+-*/%(<>=!&|?".indexOf(Expression.charAt(PreviousIndex)) >= 0) {
                        continue;
                    }
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

            if (Quote != 0) {
                if (Character == Quote && !IsEscaped(Expression, Index)) {
                    Quote = 0;
                }

                continue;
            }

            if ((Character == '\'' || Character == '"') && !IsEscaped(Expression, Index)) {
                Quote = Character;
                continue;
            }

            if (Character == ')' || Character == '}' || Character == ']') {
                Depth++;
                continue;
            }

            if (Character == '(' || Character == '{' || Character == '[') {
                Depth--;
                continue;
            }

            if (Depth != 0) {
                continue;
            }

            for (String Operator : Operators) {
                if (Character != Operator.charAt(0)) {
                    continue;
                }

                if ((Character == '+' || Character == '-')
                        && (PreviousNonWhitespaceIndex(Expression, Index) < 0
                        || "+-*/%(<>=!&|?".indexOf(Expression.charAt(PreviousNonWhitespaceIndex(Expression, Index))) >= 0)) {
                    continue;
                }

                if ((Character == '*' || Character == '/' || Character == '%')
                        && PreviousNonWhitespaceIndex(Expression, Index) < 0) {
                    continue;
                }

                return Index;
            }
        }

        return -1;
    }

    private static int FindTopLevelTernaryQuestion(String Expression) {
        int Depth = 0;
        char Quote = 0;
        boolean Escaped = false;

        for (int Index = 0; Index < Expression.length(); Index++) {
            char Character = Expression.charAt(Index);

            if (Quote != 0) {
                if (Escaped) {
                    Escaped = false;
                } else if (Character == '\\') {
                    Escaped = true;
                } else if (Character == Quote) {
                    Quote = 0;
                }

                continue;
            }

            if (Character == '\'' || Character == '"') {
                Quote = Character;
                continue;
            }

            if (Character == '(' || Character == '{' || Character == '[') {
                Depth++;
                continue;
            }

            if (Character == ')' || Character == '}' || Character == ']') {
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
        boolean Escaped = false;

        for (int Index = 0; Index < Expression.length(); Index++) {
            char Character = Expression.charAt(Index);

            if (Quote != 0) {
                if (Escaped) {
                    Escaped = false;
                } else if (Character == '\\') {
                    Escaped = true;
                } else if (Character == Quote) {
                    Quote = 0;
                }

                continue;
            }

            if (Character == '\'' || Character == '"') {
                Quote = Character;
                continue;
            }

            if (Character == '(' || Character == '{' || Character == '[') {
                Depth++;
                continue;
            }

            if (Character == ')' || Character == '}' || Character == ']') {
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

                if (Index == 0 || Expression.charAt(Index - 1) != '?') {
                    TernaryDepth++;
                }
            } else if (Character == ':') {
                TernaryDepth--;

                if (TernaryDepth == 0) {
                    return Index;
                }
            }
        }

        return -1;
    }

    private static int FindUnaryOperandEnd(String Expression, int Start) {
        int Index = Start;

        while (Index < Expression.length() && Character.isWhitespace(Expression.charAt(Index))) {
            Index++;
        }

        if (Index >= Expression.length()) {
            return Index;
        }

        char Character = Expression.charAt(Index);

        if (Character == '!' || Character == '+' || Character == '-') {
            return FindUnaryOperandEnd(Expression, Index + 1);
        }

        if (java.lang.Character.isDigit(Character) || Character == '.') {
            int NumberEnd = Index;

            while (NumberEnd < Expression.length()
                    && (java.lang.Character.isDigit(Expression.charAt(NumberEnd)) || Expression.charAt(NumberEnd) == '.')) {
                NumberEnd++;
            }

            if (NumberEnd < Expression.length()
                    && (Expression.charAt(NumberEnd) == 'e' || Expression.charAt(NumberEnd) == 'E')) {
                NumberEnd++;

                if (NumberEnd < Expression.length()
                        && (Expression.charAt(NumberEnd) == '+' || Expression.charAt(NumberEnd) == '-')) {
                    NumberEnd++;
                }

                while (NumberEnd < Expression.length() && java.lang.Character.isDigit(Expression.charAt(NumberEnd))) {
                    NumberEnd++;
                }
            }

            return NumberEnd;
        }

        if (Character == '(') {
            int ClosingIndex = FindMatchingClosing(Expression, Index);

            return ClosingIndex >= 0 ? ClosingIndex + 1 : Index;
        }

        if (Character == '\'' || Character == '"') {
            for (int QuoteIndex = Index + 1; QuoteIndex < Expression.length(); QuoteIndex++) {
                if (Expression.charAt(QuoteIndex) == Character && !IsEscaped(Expression, QuoteIndex)) {
                    return QuoteIndex + 1;
                }
            }

            return Index;
        }

        while (Index < Expression.length()) {
            char Current = Expression.charAt(Index);

            if (java.lang.Character.isWhitespace(Current) || "+-*/%<>=!&|?:,;{}[]()".indexOf(Current) >= 0) {
                break;
            }

            Index++;
        }

        if (Index < Expression.length() && Expression.charAt(Index) == '(') {
            int ClosingIndex = FindMatchingClosing(Expression, Index);

            if (ClosingIndex >= 0) {
                Index = ClosingIndex + 1;
            }
        }

        return Index;
    }

    private static int FindMatchingClosing(String Expression, int OpenIndex) {
        char Open = Expression.charAt(OpenIndex);
        char Closing = Open == '(' ? ')' : Open == '{' ? '}' : ']';
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
            } else if (Character == Open) {
                Depth++;
            } else if (Character == Closing) {
                Depth--;

                if (Depth == 0) {
                    return Index;
                }
            }
        }

        return -1;
    }

    private static int PreviousNonWhitespaceIndex(String Expression, int Index) {
        for (int Previous = Index - 1; Previous >= 0; Previous--) {
            if (!Character.isWhitespace(Expression.charAt(Previous))) {
                return Previous;
            }
        }

        return -1;
    }

    private static boolean IsEscaped(String Expression, int Index) {
        int BackslashCount = 0;

        for (int Previous = Index - 1; Previous >= 0 && Expression.charAt(Previous) == '\\'; Previous--) {
            BackslashCount++;
        }

        return BackslashCount % 2 != 0;
    }

    public static double ParseDouble(String Value) {
        if (Value == null) {
            return 0;
        }

        try {
            return Double.parseDouble(Value.trim());
        } catch (NumberFormatException Exception) {
            return 0;
        }
    }
}
