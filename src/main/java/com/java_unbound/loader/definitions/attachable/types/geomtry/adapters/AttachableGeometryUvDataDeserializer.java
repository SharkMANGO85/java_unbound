package com.java_unbound.loader.definitions.attachable.types.geomtry.adapters;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.java_unbound.loader.definitions.attachable.types.geomtry.AttachableGeometryUvData;
import com.java_unbound.loader.definitions.attachable.types.geomtry.AttachableGeometryUvFace;

import java.lang.reflect.Type;

public class AttachableGeometryUvDataDeserializer implements JsonDeserializer<AttachableGeometryUvData> {
    @Override
    public AttachableGeometryUvData deserialize(JsonElement Json, Type TypeOfT, JsonDeserializationContext Context) {
        if (Json == null || Json.isJsonNull()) {
            return null;
        }

        AttachableGeometryUvData UV = new AttachableGeometryUvData();

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

    private static AttachableGeometryUvFace ReadFace(
            JsonObject Object,
            String FaceName,
            JsonDeserializationContext Context
    ) {
        JsonElement FaceElement = Object.get(FaceName);

        if (FaceElement == null || !FaceElement.isJsonObject()) {
            return null;
        }

        return Context.deserialize(FaceElement, AttachableGeometryUvFace.class);
    }
}
