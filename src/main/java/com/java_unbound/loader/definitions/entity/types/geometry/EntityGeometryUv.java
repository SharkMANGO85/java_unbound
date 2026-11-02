package com.java_unbound.loader.definitions.entity.types.geometry;

import com.google.gson.annotations.SerializedName;

public class EntityGeometryUv {
    @SerializedName("north")
    public EntityGeometryUvFace North;

    @SerializedName("south")
    public EntityGeometryUvFace South;

    @SerializedName("east")
    public EntityGeometryUvFace East;

    @SerializedName("west")
    public EntityGeometryUvFace West;

    @SerializedName("up")
    public EntityGeometryUvFace Up;

    @SerializedName("down")
    public EntityGeometryUvFace Down;
}
