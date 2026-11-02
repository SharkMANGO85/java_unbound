package com.java_unbound.loader.definitions.entity.types.geometry;

import com.google.gson.annotations.SerializedName;

public class EntityGeometryTextureMesh {
    @SerializedName("texture")
    public String Texture = "";

    @SerializedName("position")
    public float[] Position;

    @SerializedName("rotation")
    public float[] Rotation;

    @SerializedName("scale")
    public float[] Scale;

    @SerializedName("local_pivot")
    public float[] LocalPivot;

    @SerializedName("use_pixel_depth")
    public boolean UsePixelDepth;
}