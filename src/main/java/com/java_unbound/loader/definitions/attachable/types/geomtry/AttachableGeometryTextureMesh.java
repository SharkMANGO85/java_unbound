package com.java_unbound.loader.definitions.attachable.types.geomtry;

import com.google.gson.annotations.SerializedName;

public class AttachableGeometryTextureMesh {
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