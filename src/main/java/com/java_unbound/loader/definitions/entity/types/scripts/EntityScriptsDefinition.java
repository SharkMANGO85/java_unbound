package com.java_unbound.loader.definitions.entity.types.scripts;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class EntityScriptsDefinition {
    public List<String> Initialize;
    public List<String> PreAnimation;
    public List<EntityScriptsAnimate> Animate;
    public List<EntityScriptsVariables> Variables;

    public String Scale;
    public String ScaleX;
    public String ScaleY;
    public String ScaleZ;

    public String ParentSetup;
    public String ShouldUpdateEffectsOffscreen;
    public String ShouldUpdateBonesAndEffectsOffscreen;
}