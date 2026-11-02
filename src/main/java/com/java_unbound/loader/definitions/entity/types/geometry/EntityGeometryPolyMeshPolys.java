package com.java_unbound.loader.definitions.entity.types.geometry;

import com.google.gson.annotations.JsonAdapter;
import com.java_unbound.loader.definitions.entity.types.geometry.adapters.EntityGeometryPolyMeshPolysDeserializer;

@JsonAdapter(EntityGeometryPolyMeshPolysDeserializer.class)
public class EntityGeometryPolyMeshPolys {
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
