package com.java_unbound.loader.definitions.entity.types.geometry;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.java_unbound.loader.definitions.entity.types.geometry.adapters.EntityGeometryLocatorDeserializer;

@JsonAdapter(EntityGeometryLocatorDeserializer.class)
public class EntityGeometryLocator {
    @SerializedName("offset")
    public float[] Offset;

    @SerializedName("rotation")
    public float[] Rotation;

    @SerializedName("ignore_inherited_scale")
    public boolean IgnoreInheritedScale;
}
