package com.java_unbound.attachables.rendering;

import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class BedrockBone {
    public record Locator(String Name, String Bone, float[] Offset, float[] Rotation, boolean IgnoreInheritedScale) {}
    public record TextureMesh(String Texture, float[] Position, float[] Rotation, float[] LocalPivot, float[] Scale) {}
    public record ModelQuad(float[] Positions, float[] Normals, float[] Uvs, String Material) {}

    final int Index;
    final String Name;
    BedrockBone Parent;
    final List<BedrockBone> Children = new ArrayList<>();

    float[] Pivot = new float[3];
    float[] Rotation = new float[3];
    boolean Mirror;
    boolean NeverRender;
    float Inflate;
    String Binding;
    String Material;

    final List<ModelQuad> Quads = new ArrayList<>();
    final Map<String, Locator> Locators = new LinkedHashMap<>();
    final List<TextureMesh> TextureMeshes = new ArrayList<>();
    final Matrix4f RestMatrix = new Matrix4f();

    BedrockBone(int Index, String Name) {
        this.Index = Index;
        this.Name = Name;
    }

    public String GetName() {return Name;}
    public BedrockBone GetParent() {return Parent;}
    public List<BedrockBone> GetChildren() {return Children;}
    public float[] GetPivot() {return Pivot;}
    public float[] GetRotation() {return Rotation;}
    public boolean IsMirror() {return Mirror;}
    public boolean IsNeverRender() {return NeverRender;}
    public float GetInflate() {return Inflate;}

    public String GetBinding() {return Binding;}

    public String GetMaterial() {return Material;}
    public List<ModelQuad> GetQuads() {return Quads;}
    public Map<String, Locator> GetLocators() {return Locators;}
    public List<TextureMesh> GetTextureMeshes() {return TextureMeshes;}
    public Matrix4f GetRestMatrix() {return new Matrix4f(RestMatrix);}
}