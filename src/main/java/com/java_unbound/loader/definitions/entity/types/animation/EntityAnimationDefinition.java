package com.java_unbound.loader.definitions.entity.types.animation;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class EntityAnimationDefinition {
    public String Identifier;

    public boolean Loop = false;
    public boolean HoldOnLastFrame = false;
    public Float AnimationLength;
    public boolean OverridePreviousAnimation = false;

    public String StartDelay;
    public String LoopDelay;
    public String AnimTimeUpdate;
    public String BlendWeight;

    public Map<String, EntityAnimationBone> Bones = new LinkedHashMap<>();

    public TreeMap<Float, List<EntityAnimationParticleEffect>> ParticleEffects = new TreeMap<>();
    public TreeMap<Float, List<EntityAnimationSoundEffect>> SoundEffects = new TreeMap<>();
    public TreeMap<Float, List<String>> Timeline = new TreeMap<>();
}