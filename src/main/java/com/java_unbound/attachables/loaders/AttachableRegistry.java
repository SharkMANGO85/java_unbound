package com.java_unbound.attachables.loaders;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.java_unbound.JavaUnbound;
import com.java_unbound.entities.loaders.EntityResolver;
import com.java_unbound.loader.definitions.entities.EntitiesDefinition;
import com.java_unbound.utils.variables.HashMaps;

import java.nio.file.Path;
import java.util.HashMap;

public class AttachableRegistry {
    public String Identifier = "";
    public HashMap<String, Path> Textures = null;
    public HashMap<String, JsonElement> Geometries = null;
    public HashMap<String, JsonElement> RenderControllers = null;
    public JsonElement Animations = JsonNull.INSTANCE;
    public HashMap<String, JsonArray> Scripts = null;
    public JsonElement SoundEffects = JsonNull.INSTANCE;
    public JsonElement ParticleEffects = JsonNull.INSTANCE;
    public boolean EnableAttachables = true;
    public JsonElement SpawnEgg = JsonNull.INSTANCE;
}
