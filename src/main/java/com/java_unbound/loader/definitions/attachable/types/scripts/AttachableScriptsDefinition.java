package com.java_unbound.loader.definitions.attachable.types.scripts;

import java.util.List;

public class AttachableScriptsDefinition {
    public List<String> Initialize;
    public List<String> PreAnimation;
    public List<AttachableScriptsAnimate> Animate;
    public List<AttachableScriptsVariables> Variables;

    public String Scale;
    public String ScaleX;
    public String ScaleY;
    public String ScaleZ;

    public String ParentSetup;
    public String ShouldUpdateEffectsOffscreen;
    public String ShouldUpdateBonesAndEffectsOffscreen;
}