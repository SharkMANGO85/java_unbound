package com.java_unbound.loader.definitions.attachable.types.geomtry.adapters;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.java_unbound.loader.definitions.attachable.types.geomtry.AttachableGeometryLocator;

import java.lang.reflect.Type;

public class AttachableGeometryLocatorDeserializer implements JsonDeserializer<AttachableGeometryLocator> {
    @Override
    public AttachableGeometryLocator deserialize(JsonElement Json, Type TypeOfT, JsonDeserializationContext Context) {
        if (Json == null || Json.isJsonNull()) {
            return null;
        }

        AttachableGeometryLocator Locator = new AttachableGeometryLocator();

        if (Json.isJsonArray()) {
            Locator.Offset = Context.deserialize(Json, float[].class);
            return Locator;
        }

        if (!Json.isJsonObject()) {
            return null;
        }

        JsonObject Object = Json.getAsJsonObject();
        JsonElement OffsetElement = Object.get("offset");
        JsonElement RotationElement = Object.get("rotation");
        JsonElement IgnoreInheritedScaleElement = Object.get("ignore_inherited_scale");

        if (OffsetElement != null && OffsetElement.isJsonArray()) {
            Locator.Offset = Context.deserialize(OffsetElement, float[].class);
        }

        if (RotationElement != null && RotationElement.isJsonArray()) {
            Locator.Rotation = Context.deserialize(RotationElement, float[].class);
        }

        if (IgnoreInheritedScaleElement != null && IgnoreInheritedScaleElement.isJsonPrimitive()) {
            Locator.IgnoreInheritedScale = IgnoreInheritedScaleElement.getAsBoolean();
        }

        return Locator;
    }
}
