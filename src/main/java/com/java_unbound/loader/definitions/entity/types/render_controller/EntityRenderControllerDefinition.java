package com.java_unbound.loader.definitions.entity.types.render_controller;

import java.util.ArrayList;
import java.util.List;

public class EntityRenderControllerDefinition {
    public String Identifier;

    public EntityRenderControllerArrays Arrays = new EntityRenderControllerArrays();

    public String Geometry;
    public List<String> Textures = new ArrayList<>();
    public List<EntityRenderControllerMaterial> Materials = new ArrayList<>();
    public List<EntityRenderControllerPartVisibility> PartVisibility = new ArrayList<>();

    public boolean IgnoreLighting;
    public boolean FilterLighting;
    public boolean RebuildAnimationMatrices;

    public String LightColorMultiplier;

    public EntityRenderControllerColor Color;
    public EntityRenderControllerColor IsHurtColor;
    public EntityRenderControllerColor OnFireColor;
    public EntityRenderControllerColor OverlayColor;

    public EntityRenderControllerUvAnim UvAnim;
}
