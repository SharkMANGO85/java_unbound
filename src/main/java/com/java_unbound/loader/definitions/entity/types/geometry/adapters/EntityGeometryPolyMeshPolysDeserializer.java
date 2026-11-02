package com.java_unbound.loader.definitions.entity.types.geometry.adapters;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.java_unbound.loader.definitions.entity.types.geometry.EntityGeometryPolyMeshPolys;

import java.lang.reflect.Type;

public class EntityGeometryPolyMeshPolysDeserializer implements JsonDeserializer<EntityGeometryPolyMeshPolys> {
    @Override
    public EntityGeometryPolyMeshPolys deserialize(JsonElement Json, Type TypeOfT, JsonDeserializationContext Context) {
        if (Json == null || Json.isJsonNull()) {
            return null;
        }

        EntityGeometryPolyMeshPolys Polys = new EntityGeometryPolyMeshPolys();

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
