package com.java_unbound.attachables.rendering;

import com.java_unbound.attachables.rendering.BedrockBone.ModelQuad;
import com.java_unbound.attachables.rendering.BedrockBone.TextureMesh;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.List;
import java.util.function.Function;

public final class BedrockModelRenderer {
    private BedrockModelRenderer() {}

    public static void Render(PoseStack PoseStack, SubmitNodeCollector SubmitNodeCollector, int LightCoords, BedrockModel Model, Identifier Texture, float Scale, boolean Center) {
        Render(PoseStack, SubmitNodeCollector, LightCoords, Model, Texture, Scale, Center, null, null);
    }

    public static void Render(PoseStack PoseStack, SubmitNodeCollector SubmitNodeCollector, int LightCoords, BedrockModel Model, Identifier Texture, float Scale, boolean Center, BedrockPose AnimationPose, Function<String, Identifier> TextureResolver) {
        float S = Scale / 16F;

        Matrix4f[] Matrices = Model.ComputeBoneMatrices(AnimationPose);
        boolean[] Visible = new boolean[Matrices.length];

        for (BedrockBone Bone : Model.GetBones()) {
            Visible[Bone.Index] = Model.IsVisible(Bone, AnimationPose);
        }

        PoseStack.pushPose();
        PoseStack.scale(S, S, S);

        if (Center) {
            PoseStack.translate(-Model.CenterX(), -Model.CenterY(), -Model.CenterZ());
        }

        RenderType MainType = RenderTypes.entityTranslucent(Texture);

        SubmitNodeCollector.submitCustomGeometry(PoseStack, MainType, (PoseEntry, Buffer) -> {
            Vector3f Position = new Vector3f();
            Vector3f Normal = new Vector3f();
            Matrix3f NormalMatrix = new Matrix3f();

            for (BedrockBone Bone : Model.GetBones()) {
                if (Bone.Quads.isEmpty() || !Visible[Bone.Index]) {continue;}

                Matrices[Bone.Index].normal(NormalMatrix);

                for (ModelQuad Quad : Bone.Quads) {
                    Emit(Buffer, PoseEntry, LightCoords, Quad, Matrices[Bone.Index], NormalMatrix, Position, Normal);
                }
            }
        });

        if (TextureResolver != null) {
            for (BedrockBone Bone : Model.GetBones()) {
                if (Bone.TextureMeshes.isEmpty() || !Visible[Bone.Index]) {continue;}

                for (TextureMesh Mesh : Bone.TextureMeshes) {
                    Identifier MeshTexture = TextureResolver.apply(Mesh.Texture());
                    if (MeshTexture == null) {continue;}

                    List<ModelQuad> MeshQuads = BedrockTextureMeshes.Get(MeshTexture);
                    if (MeshQuads == null || MeshQuads.isEmpty()) {continue;}

                    final Matrix4f MeshMatrix = new Matrix4f(Matrices[Bone.Index]).mul(BedrockModel.TextureMeshMatrix(Mesh));
                    final Matrix3f MeshNormalMatrix = MeshMatrix.normal(new Matrix3f());

                    SubmitNodeCollector.submitCustomGeometry(PoseStack, RenderTypes.entityCutout(MeshTexture), (PoseEntry, Buffer) -> {
                        Vector3f Position = new Vector3f();
                        Vector3f Normal = new Vector3f();

                        for (ModelQuad Quad : MeshQuads) {
                            Emit(Buffer, PoseEntry, LightCoords, Quad, MeshMatrix, MeshNormalMatrix, Position, Normal);
                        }
                    });
                }
            }
        }

        PoseStack.popPose();
    }

    private static void Emit(VertexConsumer Buffer, PoseStack.Pose PoseEntry, int LightCoords, ModelQuad Quad, Matrix4f Matrix, Matrix3f NormalMatrix, Vector3f Position, Vector3f Normal) {
        for (int I = 0; I < 4; I++) {
            Position.set(Quad.Positions()[I * 3], Quad.Positions()[I * 3 + 1], Quad.Positions()[I * 3 + 2]);
            Matrix.transformPosition(Position);

            Normal.set(Quad.Normals()[I * 3], Quad.Normals()[I * 3 + 1], Quad.Normals()[I * 3 + 2]);
            NormalMatrix.transform(Normal);

            if (Normal.lengthSquared() > 1.0E-8F) {
                Normal.normalize();
            } else {
                Normal.set(0F, 1F, 0F);
            }

            Buffer.addVertex(PoseEntry, Position.x, Position.y, Position.z).setColor(0xFFFFFFFF).setUv(Quad.Uvs()[I * 2], Quad.Uvs()[I * 2 + 1]).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightCoords).setNormal(PoseEntry, Normal.x, Normal.y, Normal.z);
        }
    }
}