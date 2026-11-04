package com.java_unbound.loader.definitions.attachable.types.geomtry;

import com.google.gson.annotations.SerializedName;

public class AttachableGeometryUvFace {
    @SerializedName("uv")
    public float[] Uv;

    @SerializedName("uv_size")
    public float[] UvSize;

    @SerializedName("uv_rotation")
    public int UvRotation;

    @SerializedName("material_instance")
    public String MaterialInstance;
}