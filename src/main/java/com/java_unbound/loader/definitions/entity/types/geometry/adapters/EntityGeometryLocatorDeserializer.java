package com.java_unbound.loader.definitions.entity.types.geometry.adapters;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.java_unbound.loader.definitions.entity.types.geometry.EntityGeometryLocator;

import java.lang.reflect.Type;

public class EntityGeometryLocatorDeserializer implements JsonDeserializer<EntityGeometryLocator> {
    @Override
    public EntityGeometryLocator deserialize(JsonElement Json, Type TypeOfT, JsonDeserializationContext Context) {
        if (Json == null || Json.isJsonNull()) {
            return null;
        }

        EntityGeometryLocator Locator = new EntityGeometryLocator();

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
