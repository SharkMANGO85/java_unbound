package com.java_unbound.entities.interfaces;

import java.util.HashMap;

public interface EntityVariableInterface {
    HashMap<String, Double> JavaUnbound$getVariables();

    void JavaUnbound$setVariable(String Name, double Value);

    double JavaUnbound$getVariable(String Name);
}