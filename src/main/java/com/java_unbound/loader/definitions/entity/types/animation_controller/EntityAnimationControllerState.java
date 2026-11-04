package com.java_unbound.loader.definitions.entity.types.animation_controller;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class EntityAnimationControllerState {
    public String Name;

    public List<EntityAnimationControllerAnimation> Animations = new ArrayList<>();
    public List<EntityAnimationControllerTransition> Transitions = new ArrayList<>();

    public List<String> OnEntry = new ArrayList<>();
    public List<String> OnExit = new ArrayList<>();

    public List<EntityAnimationControllerEffect> ParticleEffects = new ArrayList<>();
    public List<EntityAnimationControllerEffect> SoundEffects = new ArrayList<>();

    public float BlendTransition;
    public TreeMap<Float, Float> BlendTransitionCurve;

    public boolean BlendViaShortestPath;
}