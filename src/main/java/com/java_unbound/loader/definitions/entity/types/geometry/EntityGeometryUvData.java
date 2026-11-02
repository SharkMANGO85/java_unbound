package com.java_unbound.loader.definitions.entity.types.geometry;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.java_unbound.loader.definitions.entity.types.geometry.adapters.EntityGeometryUvDataDeserializer;

@JsonAdapter(EntityGeometryUvDataDeserializer.class)
public class EntityGeometryUvData extends EntityGeometryUv {
    @SerializedName("uv")
    public float[] Uv;
}
