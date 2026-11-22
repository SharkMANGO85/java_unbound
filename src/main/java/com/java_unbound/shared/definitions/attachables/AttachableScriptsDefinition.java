package com.java_unbound.shared.definitions.attachables;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AttachableScriptsDefinition {
    public HashMap<String, String> Animate = new HashMap<>();

    public List<String> Initialize = new ArrayList<>();
    public List<String> PreAnimation = new ArrayList<>();

    public String ParentSetup;

    public String Scale;
    public String ScaleX;
    public String ScaleY;
    public String ScaleZ;

    public String ShouldUpdateBonesAndEffectsOffscreen;
    public String ShouldUpdateEffectsOffscreen;

    public HashMap<String, String> Variables = new HashMap<>();
}