package com.java_unbound.loader.definitions.entity.types.animation_controller;

import java.util.LinkedHashMap;

public class EntityAnimationControllerDefinition {
    public String Identifier;
    public String InitialState = "default";

    public LinkedHashMap<String, EntityAnimationControllerState> States = new LinkedHashMap<>();
}