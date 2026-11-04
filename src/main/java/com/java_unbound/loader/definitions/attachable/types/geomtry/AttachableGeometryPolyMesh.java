package com.java_unbound.loader.definitions.attachable.types.geomtry;

import com.google.gson.annotations.SerializedName;

public class AttachableGeometryPolyMesh {
    @SerializedName("normalized_uvs")
    public boolean NormalizedUvs;

    @SerializedName("positions")
    public float[][] Positions;

    @SerializedName("normals")
    public float[][] Normals;

    @SerializedName("uvs")
    public float[][] Uvs;

    @SerializedName("polys")
    public AttachableGeometryPolyMeshPolys Polys;
}
