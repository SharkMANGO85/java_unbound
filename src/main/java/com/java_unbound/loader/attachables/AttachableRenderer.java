package com.java_unbound.loader.attachables;

import com.google.gson.JsonElement;
import com.java_unbound.JavaUnbound;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket;
import net.minecraft.resources.Identifier;
import org.joml.Quaternionf;

public class AttachableRenderer {
    public static void Render(JsonElement Geometry, Identifier Texture, PoseStack PoseStack, SubmitNodeCollector Collector, int Light) {
        PoseStack.pushPose();
        PoseStack.translate(0.0F, 0.0F, -2.0F);
        PoseStack.scale(0.05F, 0.05F, 0.05F);

        Collector.submitCustomGeometry(PoseStack, RenderTypes.debugFilledBox(), (Pose, Buffer) -> {
            float x1 = -1.0F;
            float y1 = -1.0F;
            float z1 = -1.0F;
            float x2 = 1.0F;
            float y2 = 1.0F;
            float z2 = 1.0F;

            int color = 0xFFFFFFFF;

            Pose.rotate(new Quaternionf().rotationXYZ((float) Math.toRadians(25), (float) Math.toRadians(25), (float) Math.toRadians(25)));

            // Front
            Buffer.addVertex(Pose, x1, y1, z1).setColor(color).setLight(Light).setNormal(Pose, 0.0F, 0.0F, -1.0F);
            Buffer.addVertex(Pose, x2, y1, z1).setColor(color).setLight(Light).setNormal(Pose, 0.0F, 0.0F, -1.0F);
            Buffer.addVertex(Pose, x2, y2, z1).setColor(color).setLight(Light).setNormal(Pose, 0.0F, 0.0F, -1.0F);
            Buffer.addVertex(Pose, x1, y2, z1).setColor(color).setLight(Light).setNormal(Pose, 0.0F, 0.0F, -1.0F);

            // Back
            Buffer.addVertex(Pose, x1, y1, z2).setColor(color).setLight(Light).setNormal(Pose, 0.0F, 0.0F, 1.0F);
            Buffer.addVertex(Pose, x1, y2, z2).setColor(color).setLight(Light).setNormal(Pose, 0.0F, 0.0F, 1.0F);
            Buffer.addVertex(Pose, x2, y2, z2).setColor(color).setLight(Light).setNormal(Pose, 0.0F, 0.0F, 1.0F);
            Buffer.addVertex(Pose, x2, y1, z2).setColor(color).setLight(Light).setNormal(Pose, 0.0F, 0.0F, 1.0F);

            // Left
            Buffer.addVertex(Pose, x1, y1, z1).setColor(color).setLight(Light).setNormal(Pose, -1.0F, 0.0F, 0.0F);
            Buffer.addVertex(Pose, x1, y2, z1).setColor(color).setLight(Light).setNormal(Pose, -1.0F, 0.0F, 0.0F);
            Buffer.addVertex(Pose, x1, y2, z2).setColor(color).setLight(Light).setNormal(Pose, -1.0F, 0.0F, 0.0F);
            Buffer.addVertex(Pose, x1, y1, z2).setColor(color).setLight(Light).setNormal(Pose, -1.0F, 0.0F, 0.0F);

            // Right
            Buffer.addVertex(Pose, x2, y1, z2).setColor(color).setLight(Light).setNormal(Pose, 1.0F, 0.0F, 0.0F);
            Buffer.addVertex(Pose, x2, y2, z2).setColor(color).setLight(Light).setNormal(Pose, 1.0F, 0.0F, 0.0F);
            Buffer.addVertex(Pose, x2, y2, z1).setColor(color).setLight(Light).setNormal(Pose, 1.0F, 0.0F, 0.0F);
            Buffer.addVertex(Pose, x2, y1, z1).setColor(color).setLight(Light).setNormal(Pose, 1.0F, 0.0F, 0.0F);

            // Top
            Buffer.addVertex(Pose, x1, y2, z1).setColor(color).setLight(Light).setNormal(Pose, 0.0F, 1.0F, 0.0F);
            Buffer.addVertex(Pose, x2, y2, z1).setColor(color).setLight(Light).setNormal(Pose, 0.0F, 1.0F, 0.0F);
            Buffer.addVertex(Pose, x2, y2, z2).setColor(color).setLight(Light).setNormal(Pose, 0.0F, 1.0F, 0.0F);
            Buffer.addVertex(Pose, x1, y2, z2).setColor(color).setLight(Light).setNormal(Pose, 0.0F, 1.0F, 0.0F);

            // Bottom
            Buffer.addVertex(Pose, x1, y1, z2).setColor(color).setLight(Light).setNormal(Pose, 0.0F, -1.0F, 0.0F);
            Buffer.addVertex(Pose, x2, y1, z2).setColor(color).setLight(Light).setNormal(Pose, 0.0F, -1.0F, 0.0F);
            Buffer.addVertex(Pose, x2, y1, z1).setColor(color).setLight(Light).setNormal(Pose, 0.0F, -1.0F, 0.0F);
            Buffer.addVertex(Pose, x1, y1, z1).setColor(color).setLight(Light).setNormal(Pose, 0.0F, -1.0F, 0.0F);
        });

        PoseStack.popPose();
    }
}