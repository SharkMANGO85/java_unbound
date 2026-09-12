package com.java_unbound.loader.entities;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;

import java.nio.file.Path;
import java.util.HashMap;

public class EntityRegistry {
    private String Identifier = "";
    private JsonElement Geometries = JsonNull.INSTANCE;
    private HashMap<String, Path> Textures = null;
    private JsonElement Animations = JsonNull.INSTANCE;
    private JsonElement Scripts = JsonNull.INSTANCE;
    private JsonElement RenderControllers = JsonNull.INSTANCE;
    private JsonElement SoundEffects = JsonNull.INSTANCE;
    private JsonElement ParticleEffects = JsonNull.INSTANCE;
    private boolean EnableAttachables = true;
    private JsonElement SpawnEgg = JsonNull.INSTANCE;

    public void LoadEntity(String Identifier) {
        this.Identifier = Identifier;
        this.Textures = EntityResolver.GetTextures(Identifier);
    }
}