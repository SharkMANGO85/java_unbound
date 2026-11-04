package com.java_unbound.loader.definitions.attachable.types.geomtry;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class AttachableGeometryDefinition {
    public String Identifier;

    @SerializedName("description")
    public AttachableGeometryDescription Description = new AttachableGeometryDescription();

    @SerializedName("bones")
    public List<AttachableGeometryBone> Bones = new ArrayList<>();

    @SerializedName("item_display_transforms")
    public AttachableGeometryItemDisplayTransforms ItemDisplayTransforms;
}