package com.java_unbound.loader.definitions.entity.types.geometry.adapters;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.java_unbound.loader.definitions.entity.types.geometry.EntityGeometryUvData;
import com.java_unbound.loader.definitions.entity.types.geometry.EntityGeometryUvFace;

import java.lang.reflect.Type;

public class EntityGeometryUvDataDeserializer implements JsonDeserializer<EntityGeometryUvData> {
    @Override
    public EntityGeometryUvData deserialize(JsonElement Json, Type TypeOfT, JsonDeserializationContext Context) {
        if (Json == null || Json.isJsonNull()) {
            return null;
        }

        EntityGeometryUvData UV = new EntityGeometryUvData();

        if (Json.isJsonArray()) {
            UV.Uv = Context.deserialize(Json, float[].class);
            return UV;
        }

        if (!Json.isJsonObject()) {
            return null;
        }

        JsonObject Object = Json.getAsJsonObject();
        JsonElement UvElement = Object.get("uv");

        if (UvElement != null && UvElement.isJsonArray()) {
            UV.Uv = Context.deserialize(UvElement, float[].class);
        }

        UV.North = ReadFace(Object, "north", Context);
        UV.South = ReadFace(Object, "south", Context);
        UV.East = ReadFace(Object, "east", Context);
        UV.West = ReadFace(Object, "west", Context);
        UV.Up = ReadFace(Object, "up", Context);
        UV.Down = ReadFace(Object, "down", Context);

        return UV;
    }

    private static EntityGeometryUvFace ReadFace(
            JsonObject Object,
            String FaceName,
            JsonDeserializationContext Context
    ) {
        JsonElement FaceElement = Object.get(FaceName);

        if (FaceElement == null || !FaceElement.isJsonObject()) {
            return null;
        }

        return Context.deserialize(FaceElement, EntityGeometryUvFace.class);
    }
}
