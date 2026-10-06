package com.java_unbound.molang.enums;

public final class MolangExpression {
    public enum Type {NUMBER, VALUE, ASSIGNMENT, MATH, UNKNOWN}

    private final Type ExpressionType;
    private final String Expression;

    public MolangExpression(Type Type, String Expression) {
        this.ExpressionType = Type;
        this.Expression = Expression;
    }

    public Type GetType() {
        return ExpressionType;
    }

    public String GetExpression() {
        return Expression;
    }

    @Override
    public String toString() {
        return ExpressionType + ": " + Expression;
    }
}