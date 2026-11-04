package com.java_unbound.loader.definitions.attachable.types.geomtry;

import com.google.gson.annotations.SerializedName;

public class AttachableGeometryDisplayTransform {
    @SerializedName("rotation")
    public float[] Rotation;

    @SerializedName("rotation_pivot")
    public float[] RotationPivot;

    @SerializedName("scale")
    public float[] Scale;

    @SerializedName("scale_pivot")
    public float[] ScalePivot;

    @SerializedName("translation")
    public float[] Translation;

    @SerializedName("fit_to_frame")
    public boolean FitToFrame;
}