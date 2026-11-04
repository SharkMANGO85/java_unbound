package com.java_unbound.loader.definitions.entity.types.animation;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class EntityAnimationDefinition {
    public String Identifier;
    public EntityAnimationLoopMode Loop = EntityAnimationLoopMode.ONCE;
    public Float AnimationLength;
    public String AnimTimeUpdate;
    public String BlendWeight;
    public String LoopDelay;
    public String StartDelay;
    public boolean OverridePreviousAnimation;

    public LinkedHashMap<String, EntityAnimationBone> Bones = new LinkedHashMap<>();
    public LinkedHashMap<Float, List<String>> Timeline = new LinkedHashMap<>();
    public LinkedHashMap<Float, List<EntityAnimationEffect>> ParticleEffects = new LinkedHashMap<>();
    public LinkedHashMap<Float, List<EntityAnimationEffect>> SoundEffects = new LinkedHashMap<>();
}