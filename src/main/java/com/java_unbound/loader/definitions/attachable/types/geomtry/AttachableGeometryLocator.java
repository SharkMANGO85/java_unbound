package com.java_unbound.loader.definitions.attachable.types.geomtry;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.java_unbound.loader.definitions.attachable.types.geomtry.adapters.AttachableGeometryLocatorDeserializer;

@JsonAdapter(AttachableGeometryLocatorDeserializer.class)
public class AttachableGeometryLocator {
    @SerializedName("offset")
    public float[] Offset;

    @SerializedName("rotation")
    public float[] Rotation;

    @SerializedName("ignore_inherited_scale")
    public boolean IgnoreInheritedScale;
}
