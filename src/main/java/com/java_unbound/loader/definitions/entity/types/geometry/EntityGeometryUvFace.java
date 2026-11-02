package com.java_unbound.loader.definitions.entity.types.geometry;

import com.google.gson.annotations.SerializedName;

public class EntityGeometryUvFace {
    @SerializedName("uv")
    public float[] Uv;

    @SerializedName("uv_size")
    public float[] UvSize;

    @SerializedName("uv_rotation")
    public int UvRotation;

    @SerializedName("material_instance")
    public String MaterialInstance;
}