package com.java_unbound.loader.definitions.attachable.types.geomtry;

import com.google.gson.annotations.SerializedName;

public class AttachableGeometryUv {
    @SerializedName("north")
    public AttachableGeometryUvFace North;

    @SerializedName("south")
    public AttachableGeometryUvFace South;

    @SerializedName("east")
    public AttachableGeometryUvFace East;

    @SerializedName("west")
    public AttachableGeometryUvFace West;

    @SerializedName("up")
    public AttachableGeometryUvFace Up;

    @SerializedName("down")
    public AttachableGeometryUvFace Down;
}
