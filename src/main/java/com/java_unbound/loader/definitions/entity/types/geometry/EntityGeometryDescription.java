package com.java_unbound.loader.definitions.entity.types.geometry;

import com.google.gson.annotations.SerializedName;

public class EntityGeometryDescription {
    @SerializedName("identifier")
    public String Identifier = "";

    @SerializedName("visible_bounds_width")
    public float VisibleBoundsWidth;

    @SerializedName("visible_bounds_height")
    public float VisibleBoundsHeight;

    @SerializedName("texture_width")
    public int TextureWidth;

    @SerializedName("texture_height")
    public int TextureHeight;

    @SerializedName("visible_bounds_offset")
    public float[] VisibleBoundsOffset;
}