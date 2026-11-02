package com.java_unbound.loader.definitions.entity.types.geometry;

import com.google.gson.annotations.SerializedName;

public class EntityGeometryItemDisplayTransforms {
    @SerializedName("gui")
    public EntityGeometryDisplayTransform Gui;

    @SerializedName("firstperson_righthand")
    public EntityGeometryDisplayTransform FirstpersonRighthand;

    @SerializedName("firstperson_lefthand")
    public EntityGeometryDisplayTransform FirstpersonLefthand;

    @SerializedName("thirdperson_righthand")
    public EntityGeometryDisplayTransform ThirdpersonRighthand;

    @SerializedName("thirdperson_lefthand")
    public EntityGeometryDisplayTransform ThirdpersonLefthand;

    @SerializedName("ground")
    public EntityGeometryDisplayTransform Ground;

    @SerializedName("fixed")
    public EntityGeometryDisplayTransform Fixed;

    @SerializedName("head")
    public EntityGeometryDisplayTransform Head;

    @SerializedName("embedded")
    public EntityGeometryDisplayTransform Embedded;

    @SerializedName("shelf")
    public EntityGeometryDisplayTransform Shelf;
}