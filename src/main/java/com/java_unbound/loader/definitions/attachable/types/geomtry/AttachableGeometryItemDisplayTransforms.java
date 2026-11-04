package com.java_unbound.loader.definitions.attachable.types.geomtry;

import com.google.gson.annotations.SerializedName;

public class AttachableGeometryItemDisplayTransforms {
    @SerializedName("gui")
    public AttachableGeometryDisplayTransform Gui;

    @SerializedName("firstperson_righthand")
    public AttachableGeometryDisplayTransform FirstpersonRighthand;

    @SerializedName("firstperson_lefthand")
    public AttachableGeometryDisplayTransform FirstpersonLefthand;

    @SerializedName("thirdperson_righthand")
    public AttachableGeometryDisplayTransform ThirdpersonRighthand;

    @SerializedName("thirdperson_lefthand")
    public AttachableGeometryDisplayTransform ThirdpersonLefthand;

    @SerializedName("ground")
    public AttachableGeometryDisplayTransform Ground;

    @SerializedName("fixed")
    public AttachableGeometryDisplayTransform Fixed;

    @SerializedName("head")
    public AttachableGeometryDisplayTransform Head;

    @SerializedName("embedded")
    public AttachableGeometryDisplayTransform Embedded;

    @SerializedName("shelf")
    public AttachableGeometryDisplayTransform Shelf;
}