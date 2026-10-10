package com.java_unbound.entities.interfaces;

import java.util.HashMap;

public interface EntityVariableInterface {
    HashMap<String, Object> JavaUnbound$getVariables();

    void JavaUnbound$setVariable(String Name, double Value);

    void JavaUnbound$setVariable(String Name, String Value);

    double JavaUnbound$getVariable(String Name);

    Object JavaUnbound$getVariableObject(String Name);
}