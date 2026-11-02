package com.java_unbound.loader.definitions.entity.types.geometry;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class EntityGeometryBone {
    @SerializedName("name")
    public String Name = "";

    @SerializedName("parent")
    public String Parent;

    @SerializedName("pivot")
    public float[] Pivot;

    @SerializedName("rotation")
    public float[] Rotation;

    @SerializedName("mirror")
    public boolean Mirror;

    @SerializedName("debug")
    public boolean Debug;

    @SerializedName("binding")
    public String Binding;

    @SerializedName("render_group_id")
    public int RenderGroupId;

    @SerializedName("reset")
    public boolean Reset;

    @SerializedName("neverRender")
    public boolean NeverRender;

    @SerializedName("bind_pose_rotation")
    public float[] BindPoseRotation;

    @SerializedName("cubes")
    public List<EntityGeometryCube> Cubes = new ArrayList<>();

    @SerializedName("locators")
    public HashMap<String, EntityGeometryLocator> Locators = new HashMap<>();

    @SerializedName("poly_mesh")
    public EntityGeometryPolyMesh PolyMesh;

    @SerializedName("texture_meshes")
    public List<EntityGeometryTextureMesh> TextureMeshes = new ArrayList<>();
}
