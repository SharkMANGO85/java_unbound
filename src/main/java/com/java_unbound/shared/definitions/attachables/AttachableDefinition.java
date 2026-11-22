package com.java_unbound.shared.definitions.attachables;

import com.google.gson.JsonObject;
import com.java_unbound.shared.definitions.textures.TextureDefinition;

import java.util.HashMap;

public class AttachableDefinition {
    public String Identifier;

    public HashMap<String, AttachableRenderControllerDefinition> RenderControllers = new HashMap<>();
    public HashMap<String, TextureDefinition> Textures = new HashMap<>();
    public HashMap<String, JsonObject> Geometries = new HashMap<>();
    public HashMap<String, String> Animations = new HashMap<>();
    public HashMap<String, String> Materials = new HashMap<>();
    public HashMap<String, String> Item = new HashMap<>();

    public AttachableScriptsDefinition Scripts = new AttachableScriptsDefinition();

    public HashMap<String, String> ParticleEffects = new HashMap<>();
    public HashMap<String, String> ParticleEmitters = new HashMap<>();
    public HashMap<String, String> SoundEffects = new HashMap<>();

    public AttachableSpawnEggDefinition SpawnEgg = new AttachableSpawnEggDefinition();

    public boolean EnableAttachables;
    public boolean HeldItemIgnoresLighting;
    public boolean HideArmor;

    public String QueryableGeometry;
}