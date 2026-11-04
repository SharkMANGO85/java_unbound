package com.java_unbound.loader.definitions.attachable.types.render_controller;

import java.util.ArrayList;
import java.util.List;

public class AttachableRenderControllerDefinition {
    public String Identifier;

    public AttachableRenderControllerArrays Arrays = new AttachableRenderControllerArrays();

    public String Geometry;
    public List<String> Textures = new ArrayList<>();
    public List<AttachableRenderControllerMaterial> Materials = new ArrayList<>();
    public List<AttachableRenderControllerPartVisibility> PartVisibility = new ArrayList<>();

    public boolean IgnoreLighting;
    public boolean FilterLighting;
    public boolean RebuildAnimationMatrices;

    public String LightColorMultiplier;

    public AttachableRenderControllerColor Color;
    public AttachableRenderControllerColor IsHurtColor;
    public AttachableRenderControllerColor OnFireColor;
    public AttachableRenderControllerColor OverlayColor;

    public AttachableRenderControllerUvAnim UvAnim;
}
