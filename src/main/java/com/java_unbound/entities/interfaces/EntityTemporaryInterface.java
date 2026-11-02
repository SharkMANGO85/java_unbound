package com.java_unbound.entities.interfaces;

import java.util.HashMap;

public interface EntityTemporaryInterface {
    HashMap<String, Object> JavaUnbound$getTemporaries();

    void JavaUnbound$setTemporary(String Name, double Value);

    void JavaUnbound$setTemporary(String Name, String Value);

    String JavaUnbound$getTemporary(String Name);

    Object JavaUnbound$getTemporaryObject(String Name);
}