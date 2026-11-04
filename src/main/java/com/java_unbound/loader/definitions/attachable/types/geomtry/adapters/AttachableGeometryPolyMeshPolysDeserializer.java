package com.java_unbound.loader.definitions.attachable.types.geomtry.adapters;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.java_unbound.loader.definitions.attachable.types.geomtry.AttachableGeometryPolyMeshPolys;

import java.lang.reflect.Type;

public class AttachableGeometryPolyMeshPolysDeserializer implements JsonDeserializer<AttachableGeometryPolyMeshPolys> {
    @Override
    public AttachableGeometryPolyMeshPolys deserialize(JsonElement Json, Type TypeOfT, JsonDeserializationContext Context) {
        if (Json == null || Json.isJsonNull()) {
            return null;
        }

        AttachableGeometryPolyMeshPolys Polys = new AttachableGeometryPolyMeshPolys();

        if (Json.isJsonPrimitive() && Json.getAsJsonPrimitive().isString()) {
            Polys.PolyType = Json.getAsString();
            return Polys;
        }

        if (Json.isJsonArray()) {
            Polys.Polygons = Context.deserialize(Json, int[][][].class);
            return Polys;
        }

        return null;
    }
}
