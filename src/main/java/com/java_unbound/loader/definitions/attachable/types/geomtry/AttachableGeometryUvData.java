package com.java_unbound.loader.definitions.attachable.types.geomtry;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.java_unbound.loader.definitions.attachable.types.geomtry.adapters.AttachableGeometryUvDataDeserializer;

@JsonAdapter(AttachableGeometryUvDataDeserializer.class)
public class AttachableGeometryUvData extends AttachableGeometryUv {
    @SerializedName("uv")
    public float[] Uv;
}
