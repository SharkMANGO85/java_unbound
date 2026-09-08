package com.java_unbound.loader.attachables;

import com.google.gson.JsonElement;
import com.java_unbound.JavaUnbound;

import java.util.List;
import java.util.Objects;

public class AttachableDefinition {
    public AttachableDefinition() {}

    public String Identifier;
    public JsonElement Materials;
    public JsonElement Textures;
    public List<JsonElement> Geometries;
    public JsonElement Animations;
    public JsonElement Scripts;
    public List<JsonElement> RenderControllers;
}