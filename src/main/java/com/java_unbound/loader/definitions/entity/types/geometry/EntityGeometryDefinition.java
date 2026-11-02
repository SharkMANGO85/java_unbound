package com.java_unbound.loader.definitions.entity.types.geometry;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class EntityGeometryDefinition {
    public String Identifier;

    @SerializedName("description")
    public EntityGeometryDescription Description = new EntityGeometryDescription();

    @SerializedName("bones")
    public List<EntityGeometryBone> Bones = new ArrayList<>();

    @SerializedName("item_display_transforms")
    public EntityGeometryItemDisplayTransforms ItemDisplayTransforms;
}