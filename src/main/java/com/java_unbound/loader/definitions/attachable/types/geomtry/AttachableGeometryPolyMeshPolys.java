package com.java_unbound.loader.definitions.attachable.types.geomtry;

import com.google.gson.annotations.JsonAdapter;
import com.java_unbound.loader.definitions.attachable.types.geomtry.adapters.AttachableGeometryPolyMeshPolysDeserializer;

@JsonAdapter(AttachableGeometryPolyMeshPolysDeserializer.class)
public class AttachableGeometryPolyMeshPolys {
    public String PolyType;

    public int[][][] Polygons;

    public boolean IsIndexed() {
        return Polygons != null;
    }

    public boolean IsTriangleList() {
        return "tri_list".equals(PolyType);
    }

    public boolean IsQuadList() {
        return "quad_list".equals(PolyType);
    }
}
