package com.java_unbound.loader.definitions.entity.types.geometry;

import com.google.gson.annotations.SerializedName;

public class EntityGeometryPolyMesh {
    @SerializedName("normalized_uvs")
    public boolean NormalizedUvs;

    @SerializedName("positions")
    public float[][] Positions;

    @SerializedName("normals")
    public float[][] Normals;

    @SerializedName("uvs")
    public float[][] Uvs;

    @SerializedName("polys")
    public EntityGeometryPolyMeshPolys Polys;
}
