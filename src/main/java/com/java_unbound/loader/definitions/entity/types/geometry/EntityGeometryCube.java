package com.java_unbound.loader.definitions.entity.types.geometry;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class EntityGeometryCube {
    @SerializedName("origin")
    public List<Float> Origin;

    @SerializedName("size")
    public List<Float> Size;

    @SerializedName("pivot")
    public List<Float> Pivot;

    @SerializedName("rotation")
    public List<Float> Rotation;

    @SerializedName("inflate")
    public Float Inflate;

    @SerializedName("mirror")
    public Boolean Mirror;

    @SerializedName("uv")
    public EntityGeometryUvData UV;
}
