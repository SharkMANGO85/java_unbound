package com.java_unbound.loader.definitions.attachable.types.geomtry;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class AttachableGeometryCube {
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
    public AttachableGeometryUvData UV;
}
