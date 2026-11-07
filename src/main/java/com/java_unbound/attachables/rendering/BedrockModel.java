package com.java_unbound.attachables.rendering;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.java_unbound.JavaUnbound;
import com.java_unbound.attachables.rendering.BedrockBone.Locator;
import com.java_unbound.attachables.rendering.BedrockBone.ModelQuad;
import com.java_unbound.attachables.rendering.BedrockBone.TextureMesh;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class BedrockModel {
    public record Description(String Id, int TextureWidth, int TextureHeight, float VisibleBoundsWidth, float VisibleBoundsHeight, float[] VisibleBoundsOffset) {}
    private record Raw(String Id, String ParentId, JsonObject Desc, JsonObject Body) {}

    private static final boolean NegateXYRotation = true;

    private static final String[] FaceNames = {"north", "south", "east", "west", "up", "down"};
    private static final float[][] FaceNormals = {{0, 0, -1}, {0, 0, 1}, {1, 0, 0}, {-1, 0, 0}, {0, 1, 0}, {0, -1, 0}};

    private static final int[] Clockwise = {0, 3, 2, 1};

    private static final Map<String, Optional<BedrockModel>> Cache = new ConcurrentHashMap<>();

    private final Description Info;
    private final List<BedrockBone> Bones;
    private final Map<String, BedrockBone> BonesByName;
    private final Vector3f Min;
    private final Vector3f Max;
    private final boolean PolyMeshes;

    private BedrockModel(Description Info, List<BedrockBone> Bones, Map<String, BedrockBone> BonesByName,
                         Vector3f Min, Vector3f Max, boolean PolyMeshes) {
        this.Info = Info;
        this.Bones = Bones;
        this.BonesByName = BonesByName;
        this.Min = Min;
        this.Max = Max;
        this.PolyMeshes = PolyMeshes;
    }

    public Description GetInfo() {return Info;}
    public List<BedrockBone> GetBones() {return Bones;}
    public BedrockBone GetBone(String Name) {return BonesByName.get(Name);}
    public boolean HasPolyMeshes() {return PolyMeshes;}

    public float CenterX() {return (Min.x + Max.x) * 0.5F;}
    public float CenterY() {return (Min.y + Max.y) * 0.5F;}
    public float CenterZ() {return (Min.z + Max.z) * 0.5F;}

    public Matrix4f[] ComputeBoneMatrices(BedrockPose Pose) {
        Matrix4f[] Result = new Matrix4f[Bones.size()];

        for (BedrockBone Bone : Bones) {
            Resolve(Bone, Pose, Result);
        }

        return Result;
    }

    public boolean IsVisible(BedrockBone Bone, BedrockPose Pose) {
        if (Bone.NeverRender) {return false;}
        if (Pose == null) {return true;}

        for (BedrockBone Current = Bone; Current != null; Current = Current.Parent) {
            BedrockPose.BoneState State = Pose.Get(Current.Name);
            if (State != null && !State.Visible) {return false;}
        }

        return true;
    }

    public Matrix4f GetBoneMatrix(String BoneName, BedrockPose Pose) {
        BedrockBone Bone = BonesByName.get(BoneName);
        if (Bone == null) {return null;}
        return new Matrix4f(ComputeBoneMatrices(Pose)[Bone.Index]);
    }

    public Matrix4f GetLocatorMatrix(String LocatorName, BedrockPose Pose) {
        for (BedrockBone Bone : Bones) {
            Locator Locator = Bone.Locators.get(LocatorName);
            if (Locator == null) {continue;}

            float[] Offset = Locator.Offset();
            float[] Rotation = Locator.Rotation();

            return new Matrix4f(ComputeBoneMatrices(Pose)[Bone.Index]).translate(Offset[0], Offset[1], Offset[2]).rotateZ((float) Math.toRadians(Rotation[2])).rotateY((float) Math.toRadians(NegateXYRotation ? -Rotation[1] : Rotation[1])).rotateX((float) Math.toRadians(NegateXYRotation ? -Rotation[0] : Rotation[0]));
        }

        return null;
    }

    public static Matrix4f TextureMeshMatrix(TextureMesh Mesh) {
        float[] Position = Mesh.Position();

        return new Matrix4f().translate(Position[0], Position[1], Position[2]).mul(BuildTransform(Mesh.LocalPivot(), Mesh.Rotation(), null, Mesh.Scale()));
    }

    public static BedrockModel Load(Identifier Location) {
        return Load(Location, null);
    }

    public static BedrockModel Load(Identifier Location, String GeometryId) {
        String Key = Location + "#" + (GeometryId == null ? "" : GeometryId);

        return Cache.computeIfAbsent(Key, Unused -> {
            try {
                Resource Res = Minecraft.getInstance().getResourceManager().getResource(Location).orElseThrow(() -> new IllegalStateException("Geometry file not found: " + Location));

                try (Reader Reader = Res.openAsReader()) {
                    return Optional.of(Parse(JsonParser.parseReader(Reader).getAsJsonObject(), GeometryId));
                }
            } catch (Exception Exception) {
                JavaUnbound.LOGGER.error("Failed to load Bedrock geometry {}", Key, Exception);
                return Optional.<BedrockModel>empty();
            }
        }).orElse(null);
    }

    public static void ClearCache() {
        Cache.clear();
        BedrockTextureMeshes.ClearCache();
    }

    public static BedrockModel Parse(JsonObject Root, String WantedId) {
        List<Raw> Raws = new ArrayList<>();

        if (Root.has("minecraft:geometry")) {
            for (JsonElement Element : Root.getAsJsonArray("minecraft:geometry")) {
                JsonObject Body = Element.getAsJsonObject();
                JsonObject Description = Body.has("description") ? Body.getAsJsonObject("description") : Body;
                String Full = Description.has("identifier") ? Description.get("identifier").getAsString() : "geometry.unknown";

                Raws.add(MakeRaw(Full, Description, Body));
            }
        } else {
            for (Map.Entry<String, JsonElement> Entry : Root.entrySet()) {
                if (!Entry.getKey().startsWith("geometry.") || !Entry.getValue().isJsonObject()) {continue;}

                JsonObject Body = Entry.getValue().getAsJsonObject();

                Raws.add(MakeRaw(Entry.getKey(), Body, Body));
            }
        }

        if (Raws.isEmpty()) {
            throw new IllegalArgumentException("No geometry found in file");
        }

        Map<String, Raw> ById = new LinkedHashMap<>();

        for (Raw Raw : Raws) {
            ById.putIfAbsent(Raw.Id(), Raw);
        }

        Raw Selected = WantedId == null ? Raws.get(0) : ById.get(WantedId);

        if (Selected == null) {
            throw new IllegalArgumentException("Geometry '" + WantedId + "' not found, available: " + ById.keySet());
        }

        int TexW = 64, TexH = 64;
        boolean FoundW = false, FoundH = false;
        Raw Walk = Selected;
        int Depth = 0;

        while (Walk != null && Depth++ < 16) {
            if (!FoundW) {
                Integer Value = IntOf(Walk.Desc(), "texture_width", "texturewidth");

                if (Value != null) {
                    TexW = Value; FoundW = true;
                }
            }
            if (!FoundH) {
                Integer Value = IntOf(Walk.Desc(), "texture_height", "textureheight");

                if (Value != null) {
                    TexH = Value; FoundH = true;
                }
            }
            Walk = Walk.ParentId() == null ? null : ById.get(Walk.ParentId());
        }

        JsonObject Desc = Selected.Desc();
        Description Info = new Description(Selected.Id(), TexW, TexH, Desc.has("visible_bounds_width") ? Desc.get("visible_bounds_width").getAsFloat() : 0F, Desc.has("visible_bounds_height") ? Desc.get("visible_bounds_height").getAsFloat() : 0F, Floats(Desc, "visible_bounds_offset", 3));

        Map<String, JsonObject> BoneJson = new LinkedHashMap<>();
        CollectBones(Selected, ById, BoneJson, 0);

        List<BedrockBone> Bones = new ArrayList<>();
        Map<String, BedrockBone> ByName = new HashMap<>();

        for (JsonObject Json : BoneJson.values()) {
            BedrockBone Bone = new BedrockBone(Bones.size(), Json.get("name").getAsString());

            Bone.Pivot = Floats(Json, "pivot", 3);
            Bone.Rotation = Json.has("rotation") ? Floats(Json, "rotation", 3) : Floats(Json, "bind_pose_rotation", 3);
            Bone.Mirror = Json.has("mirror") && Json.get("mirror").getAsBoolean();
            Bone.NeverRender = Json.has("neverRender") && Json.get("neverRender").getAsBoolean();
            Bone.Inflate = Json.has("inflate") ? Json.get("inflate").getAsFloat() : 0F;
            Bone.Binding = Json.has("binding") ? Json.get("binding").getAsString() : null;
            Bone.Material = Json.has("material") ? Json.get("material").getAsString() : null;

            Bones.add(Bone);
            ByName.put(Bone.Name, Bone);
        }

        for (BedrockBone Bone : Bones) {
            JsonObject Json = BoneJson.get(Bone.Name);
            if (!Json.has("parent")) {continue;}

            BedrockBone Parent = ByName.get(Json.get("parent").getAsString());
            if (Parent == null || Parent == Bone || IsAncestor(Bone, Parent)) {continue;}

            Bone.Parent = Parent;
            Parent.Children.add(Bone);
        }

        boolean[] Done = new boolean[Bones.size()];

        for (BedrockBone Bone : Bones) {
            ComputeRest(Bone, Done);
        }

        boolean PolyMeshes = false;

        for (BedrockBone Bone : Bones) {
            JsonObject Json = BoneJson.get(Bone.Name);

            if (Json.has("cubes") && Json.get("cubes").isJsonArray()) {
                for (JsonElement Cube : Json.getAsJsonArray("cubes")) {
                    BakeCube(Cube.getAsJsonObject(), Bone, TexW, TexH);
                }
            }

            if (Json.has("poly_mesh") && Json.get("poly_mesh").isJsonObject()) {
                BakePolyMesh(Json.getAsJsonObject("poly_mesh"), Bone, TexW, TexH);
                PolyMeshes = true;
            }

            if (Json.has("locators") && Json.get("locators").isJsonObject()) {
                ParseLocators(Json.getAsJsonObject("locators"), Bone);
            }

            if (Json.has("texture_meshes") && Json.get("texture_meshes").isJsonArray()) {
                ParseTextureMeshes(Json.getAsJsonArray("texture_meshes"), Bone);
            }
        }

        Vector3f Min = new Vector3f(Float.MAX_VALUE);
        Vector3f Max = new Vector3f(-Float.MAX_VALUE);
        Vector3f Tmp = new Vector3f();
        boolean Any = false;

        for (BedrockBone Bone : Bones) {
            if (Bone.NeverRender) {continue;}

            for (ModelQuad Quad : Bone.Quads) {
                for (int I = 0; I < 4; I++) {
                    Tmp.set(Quad.Positions()[I * 3], Quad.Positions()[I * 3 + 1], Quad.Positions()[I * 3 + 2]);
                    Bone.RestMatrix.transformPosition(Tmp);
                    Min.min(Tmp);
                    Max.max(Tmp);
                    Any = true;
                }
            }
        }

        if (!Any) {
            Min.set(0F);
            Max.set(0F);
        }

        return new BedrockModel(Info, List.copyOf(Bones), Map.copyOf(ByName), Min, Max, PolyMeshes);
    }

    private static Raw MakeRaw(String Full, JsonObject Desc, JsonObject Body) {
        int Colon = Full.indexOf(':');
        String Id = Colon >= 0 ? Full.substring(0, Colon) : Full;
        String ParentId = Colon >= 0 ? Full.substring(Colon + 1) : null;

        return new Raw(Id, ParentId, Desc, Body);
    }

    private static void CollectBones(Raw Raw, Map<String, Raw> ById, Map<String, JsonObject> Out, int Depth) {
        if (Raw.ParentId() != null && Depth < 16) {
            Raw Parent = ById.get(Raw.ParentId());

            if (Parent != null) {
                CollectBones(Parent, ById, Out, Depth + 1);
            }
        }

        JsonElement BonesElement = Raw.Body().get("bones");
        if (BonesElement == null || !BonesElement.isJsonArray()) {return;}

        for (JsonElement Element : BonesElement.getAsJsonArray()) {
            JsonObject Bone = Element.getAsJsonObject();
            if (Bone.has("name")) {
                Out.put(Bone.get("name").getAsString(), Bone);
            }
        }
    }

    private static boolean IsAncestor(BedrockBone Bone, BedrockBone Candidate) {
        for (BedrockBone Current = Candidate; Current != null; Current = Current.Parent) {
            if (Current == Bone) {
                return true;
            }
        }

        return false;
    }

    private static void ComputeRest(BedrockBone Bone, boolean[] Done) {
        if (Done[Bone.Index]) {return;}

        if (Bone.Parent != null) {
            ComputeRest(Bone.Parent, Done);
            Bone.RestMatrix.set(Bone.Parent.RestMatrix);
        } else {
            Bone.RestMatrix.identity();
        }

        Bone.RestMatrix.mul(BuildTransform(Bone.Pivot, Bone.Rotation, null, null));
        Done[Bone.Index] = true;
    }

    private Matrix4f Resolve(BedrockBone Bone, BedrockPose Pose, Matrix4f[] Cache) {
        Matrix4f Cached = Cache[Bone.Index];
        if (Cached != null) {return Cached;}

        Matrix4f Result = new Matrix4f();

        if (Bone.Parent != null) {
            Result.set(Resolve(Bone.Parent, Pose, Cache));
        }

        BedrockPose.BoneState State = Pose == null ? null : Pose.Get(Bone.Name);
        if (State == null) {
            Result.mul(BuildTransform(Bone.Pivot, Bone.Rotation, null, null));
        } else {
            float[] Rotation = {Bone.Rotation[0] + State.Rotation[0], Bone.Rotation[1] + State.Rotation[1], Bone.Rotation[2] + State.Rotation[2]};

            Result.mul(BuildTransform(Bone.Pivot, Rotation, State.Position, State.Scale));
        }

        Cache[Bone.Index] = Result;
        return Result;
    }

    private static Matrix4f BuildTransform(float[] Pivot, float[] Rotation, float[] Position, float[] Scale) {
        float RX = NegateXYRotation ? -Rotation[0] : Rotation[0];
        float RY = NegateXYRotation ? -Rotation[1] : Rotation[1];

        Matrix4f Matrix = new Matrix4f().translate(Pivot[0], Pivot[1], Pivot[2]);

        if (Position != null) {
            Matrix.translate(Position[0], Position[1], Position[2]);
        }

        Matrix.rotateZ((float) Math.toRadians(Rotation[2])).rotateY((float) Math.toRadians(RY)).rotateX((float) Math.toRadians(RX));

        if (Scale != null) {
            Matrix.scale(Scale[0], Scale[1], Scale[2]);
        }

        return Matrix.translate(-Pivot[0], -Pivot[1], -Pivot[2]);
    }

    private static void BakeCube(JsonObject Cube, BedrockBone Bone, int TexW, int TexH) {
        if (!Cube.has("origin") || !Cube.has("size")) {return;}

        Matrix4f Matrix = BuildTransform(Floats(Cube, "pivot", 3), Floats(Cube, "rotation", 3), null, null);

        float[] Origin = Floats(Cube, "origin", 3);
        float[] Size = Floats(Cube, "size", 3);
        float Inflate = Cube.has("inflate") ? Cube.get("inflate").getAsFloat() : Bone.Inflate;
        boolean Mirror = Cube.has("mirror") ? Cube.get("mirror").getAsBoolean() : Bone.Mirror;

        float X0 = Origin[0] - Inflate, Y0 = Origin[1] - Inflate, Z0 = Origin[2] - Inflate;
        float X1 = Origin[0] + Size[0] + Inflate, Y1 = Origin[1] + Size[1] + Inflate, Z1 = Origin[2] + Size[2] + Inflate;

        Vector3f Vec = new Vector3f();

        for (int Face = 0; Face < 6; Face++) {
            float[] Rect = FaceUv(Cube, Face, Mirror, Size[0], Size[1], Size[2]);
            if (Rect == null) {continue;}

            float[] Corners = Corners(Face, X0, Y0, Z0, X1, Y1, Z1);
            float[] Positions = new float[12];
            float[] Normals = new float[12];

            Vec.set(FaceNormals[Face][0], FaceNormals[Face][1], FaceNormals[Face][2]);
            Matrix.transformDirection(Vec).normalize();
            float NX = Vec.x, NY = Vec.y, NZ = Vec.z;

            for (int I = 0; I < 4; I++) {
                Vec.set(Corners[I * 3], Corners[I * 3 + 1], Corners[I * 3 + 2]);
                Matrix.transformPosition(Vec);

                Positions[I * 3] = Vec.x;
                Positions[I * 3 + 1] = Vec.y;
                Positions[I * 3 + 2] = Vec.z;

                Normals[I * 3] = NX;
                Normals[I * 3 + 1] = NY;
                Normals[I * 3 + 2] = NZ;
            }

            float U = Rect[0], V = Rect[1], W = Rect[2], H = Rect[3];
            float[] Unrotated = {U, V, U, V + H, U + W, V + H, U + W, V};
            int Rotation = (((int) (Rect[4] / 90F)) % 4 + 4) % 4;

            float[] Uvs = new float[8];

            for (int Step = 0; Step < 4; Step++) {
                int Destination = Clockwise[Step];
                int Source = Clockwise[(Step - Rotation + 4) % 4];

                Uvs[Destination * 2] = Unrotated[Source * 2] / TexW;
                Uvs[Destination * 2 + 1] = Unrotated[Source * 2 + 1] / TexH;
            }

            String Material = FaceMaterial(Cube, Face, Bone.Material);
            Bone.Quads.add(new ModelQuad(Positions, Normals, Uvs, Material));
        }
    }

    private static float[] Corners(int Face, float X0, float Y0, float Z0, float X1, float Y1, float Z1) {
        return switch (Face) {
            case 0 -> new float[]{X1, Y1, Z0, X1, Y0, Z0, X0, Y0, Z0, X0, Y1, Z0};
            case 1 -> new float[]{X0, Y1, Z1, X0, Y0, Z1, X1, Y0, Z1, X1, Y1, Z1};
            case 2 -> new float[]{X1, Y1, Z1, X1, Y0, Z1, X1, Y0, Z0, X1, Y1, Z0};
            case 3 -> new float[]{X0, Y1, Z0, X0, Y0, Z0, X0, Y0, Z1, X0, Y1, Z1};
            case 4 -> new float[]{X0, Y1, Z0, X0, Y1, Z1, X1, Y1, Z1, X1, Y1, Z0};

            default -> new float[]{X0, Y0, Z1, X0, Y0, Z0, X1, Y0, Z0, X1, Y0, Z1};
        };
    }

    private static float[] FaceUv(JsonObject Cube, int Face, boolean Mirror, float SX, float SY, float SZ) {
        JsonElement UvElement = Cube.get("uv");
        if (UvElement == null) {return null;}

        if (UvElement.isJsonArray()) {
            JsonArray Array = UvElement.getAsJsonArray();
            if (Array.size() < 2) {return null;}

            float U = Array.get(0).getAsFloat();
            float V = Array.get(1).getAsFloat();
            float W = SX, H = SY, D = SZ;

            int Source = Face;

            if (Mirror && Face == 2) {
                Source = 3;
            } else if (Mirror && Face == 3) {
                Source = 2;
            }

            float[] Rect = switch (Source) {
                case 0 -> new float[]{U + D, V + D, W, H};
                case 1 -> new float[]{U + 2 * D + W, V + D, W, H};
                case 2 -> new float[]{U, V + D, D, H};
                case 3 -> new float[]{U + D + W, V + D, D, H};
                case 4 -> new float[]{U + D, V, W, D};

                default -> new float[]{U + D + W, V + D, W, -D};
            };

            if (Mirror) {
                Rect[0] += Rect[2];
                Rect[2] = -Rect[2];
            }

            return new float[]{Rect[0], Rect[1], Rect[2], Rect[3], 0F};
        }

        if (!UvElement.isJsonObject()) {return null;}

        JsonObject Faces = UvElement.getAsJsonObject();
        if (!Faces.has(FaceNames[Face]) || !Faces.get(FaceNames[Face]).isJsonObject()) {return null;}

        JsonObject FaceObject = Faces.getAsJsonObject(FaceNames[Face]);
        if (!FaceObject.has("uv")) {return null;}

        float[] Uv = Floats(FaceObject, "uv", 2);

        float[] UvSize = switch (Face) {
            case 0, 1 -> new float[]{SX, SY};
            case 2, 3 -> new float[]{SZ, SY};

            default -> new float[]{SX, SZ};
        };
        if (FaceObject.has("uv_size")) {
            UvSize = Floats(FaceObject, "uv_size", 2);
        }

        float Rotation = FaceObject.has("uv_rotation") ? FaceObject.get("uv_rotation").getAsFloat() : 0F;

        return new float[]{Uv[0], Uv[1], UvSize[0], UvSize[1], Rotation};
    }

    private static String FaceMaterial(JsonObject Cube, int Face, String Fallback) {
        JsonElement UvElement = Cube.get("uv");

        if (UvElement != null && UvElement.isJsonObject()) {
            JsonObject Faces = UvElement.getAsJsonObject();
            if (Faces.has(FaceNames[Face]) && Faces.get(FaceNames[Face]).isJsonObject()) {
                JsonObject FaceObject = Faces.getAsJsonObject(FaceNames[Face]);
                if (FaceObject.has("material_instance")) {
                    return FaceObject.get("material_instance").getAsString();
                }
            }
        }

        return Fallback;
    }

    private static void BakePolyMesh(JsonObject Mesh, BedrockBone Bone, int TexW, int TexH) {
        JsonElement Polys = Mesh.get("polys");
        if (Polys == null) {return;}

        float[][] Positions = Rows(Mesh, "positions", 3);
        float[][] Normals = Rows(Mesh, "normals", 3);
        float[][] Uvs = Rows(Mesh, "uvs", 2);
        boolean Normalized = Mesh.has("normalized_uvs") && Mesh.get("normalized_uvs").getAsBoolean();

        List<int[][]> Faces = new ArrayList<>();

        if (Polys.isJsonPrimitive()) {
            int PerFace = "tri_list".equals(Polys.getAsString()) ? 3 : 4;

            for (int Face = 0; Face < Positions.length / PerFace; Face++) {
                int[][] Vertices = new int[PerFace][];

                for (int K = 0; K < PerFace; K++) {
                    int Index = Face * PerFace + K;

                    Vertices[K] = new int[]{Index, Index, Index};
                }

                Faces.add(Vertices);
            }
        } else if (Polys.isJsonArray()) {
            for (JsonElement PolyElement : Polys.getAsJsonArray()) {
                JsonArray Poly = PolyElement.getAsJsonArray();
                int[][] Vertices = new int[Poly.size()][];

                for (int K = 0; K < Poly.size(); K++) {
                    JsonArray Triple = Poly.get(K).getAsJsonArray();
                    Vertices[K] = new int[]{Triple.get(0).getAsInt(), Triple.get(1).getAsInt(), Triple.get(2).getAsInt()};
                }

                Faces.add(Vertices);
            }
        }

        for (int[][] Face : Faces) {
            if (Face.length < 3 || Face.length > 4) {continue;}

            float[] P = new float[12];
            float[] N = new float[12];
            float[] U = new float[8];
            boolean Valid = true;
            boolean HaveNormals = true;

            for (int I = 0; I < 4 && Valid; I++) {
                int[] Vertex = Face[Math.min(I, Face.length - 1)]; // triangles repeat their last vertex

                if (!InRange(Vertex[0], Positions.length) || !InRange(Vertex[2], Uvs.length)) {
                    Valid = false;
                    break;
                }

                System.arraycopy(Positions[Vertex[0]], 0, P, I * 3, 3);

                if (InRange(Vertex[1], Normals.length)) {
                    System.arraycopy(Normals[Vertex[1]], 0, N, I * 3, 3);
                } else {
                    HaveNormals = false;
                }

                U[I * 2] = Normalized ? Uvs[Vertex[2]][0] : Uvs[Vertex[2]][0] / TexW;
                U[I * 2 + 1] = Normalized ? Uvs[Vertex[2]][1] : Uvs[Vertex[2]][1] / TexH;
            }

            if (!Valid) {continue;}

            if (!HaveNormals) {
                Vector3f Edge1 = new Vector3f(P[3] - P[0], P[4] - P[1], P[5] - P[2]);
                Vector3f Edge2 = new Vector3f(P[6] - P[0], P[7] - P[1], P[8] - P[2]);
                Vector3f Normal = Edge1.cross(Edge2);
                if (Normal.lengthSquared() > 1.0E-12F) {Normal.normalize();} else {Normal.set(0F, 1F, 0F);}

                for (int I = 0; I < 4; I++) {
                    N[I * 3] = Normal.x;
                    N[I * 3 + 1] = Normal.y;
                    N[I * 3 + 2] = Normal.z;
                }
            }

            Bone.Quads.add(new ModelQuad(P, N, U, Bone.Material));
        }
    }

    private static boolean InRange(int Index, int Length) {
        return Index >= 0 && Index < Length;
    }

    private static void ParseLocators(JsonObject Locators, BedrockBone Bone) {
        for (Map.Entry<String, JsonElement> Entry : Locators.entrySet()) {
            float[] Offset = new float[3];
            float[] Rotation = new float[3];
            boolean IgnoreScale = false;

            JsonElement Value = Entry.getValue();

            if (Value.isJsonArray()) {
                Offset = ToFloats(Value, 3);
            } else if (Value.isJsonObject()) {
                JsonObject Object = Value.getAsJsonObject();
                Offset = Floats(Object, "offset", 3);
                Rotation = Floats(Object, "rotation", 3);
                IgnoreScale = Object.has("ignore_inherited_scale") && Object.get("ignore_inherited_scale").getAsBoolean();
            }

            Bone.Locators.put(Entry.getKey(), new Locator(Entry.getKey(), Bone.Name, Offset, Rotation, IgnoreScale));
        }
    }

    private static void ParseTextureMeshes(JsonArray Meshes, BedrockBone Bone) {
        for (JsonElement Element : Meshes) {
            JsonObject Object = Element.getAsJsonObject();
            if (!Object.has("texture")) {continue;}

            float[] Scale = {1F, 1F, 1F};
            if (Object.has("scale")) {
                Scale = ToFloats(Object.get("scale"), 3);
            }

            Bone.TextureMeshes.add(new TextureMesh(Object.get("texture").getAsString(), Floats(Object, "position", 3), Floats(Object, "rotation", 3), Floats(Object, "local_pivot", 3), Scale));
        }
    }

    private static Integer IntOf(JsonObject Object, String... Keys) {
        for (String Key : Keys) {
            if (Object.has(Key) && Object.get(Key).isJsonPrimitive()) {
                return Object.get(Key).getAsInt();
            }
        }
        return null;
    }

    private static float[] Floats(JsonObject Object, String Key, int Length) {
        return Object.has(Key) ? ToFloats(Object.get(Key), Length) : new float[Length];
    }

    private static float[] ToFloats(JsonElement Element, int Length) {
        float[] Result = new float[Length];

        if (Element.isJsonArray()) {
            JsonArray Array = Element.getAsJsonArray();
            for (int I = 0; I < Length && I < Array.size(); I++) {
                Result[I] = Array.get(I).getAsFloat();
            }
        } else if (Element.isJsonPrimitive()) {
            java.util.Arrays.fill(Result, Element.getAsFloat());
        }

        return Result;
    }

    private static float[][] Rows(JsonObject Object, String Key, int Width) {
        if (!Object.has(Key) || !Object.get(Key).isJsonArray()) {return new float[0][];}

        JsonArray Array = Object.getAsJsonArray(Key);
        float[][] Result = new float[Array.size()][];

        for (int I = 0; I < Array.size(); I++) {
            Result[I] = ToFloats(Array.get(I), Width);
        }

        return Result;
    }
}