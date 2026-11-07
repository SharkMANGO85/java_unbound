package com.java_unbound.attachables;

import com.java_unbound.JavaUnbound;
import com.java_unbound.attachables.rendering.BedrockModel;
import com.java_unbound.attachables.rendering.BedrockModelRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import org.joml.Matrix4fc;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import javax.naming.Name;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Renderer {
    private Renderer() {}

    private static final Map<Item, List<RenderRecord>> Renderers = new HashMap<>();
    private static final Map<Identifier, BedrockModel> Models = new HashMap<>();

    public record RenderRecord(String Namespace, String TexturePath, String GeometryPath) {}

    public static void RegisterItem(Item Item, List<RenderRecord> Renderers) {
        Renderer.Renderers.put(Item, new ArrayList<>(Renderers));
    }

    public static void RegisterItem(Item Item, RenderRecord... Renderers) {
        RegisterItem(Item, List.of(Renderers));
    }

    public static boolean HasRenderer(Item Item) {
        return Renderers.containsKey(Item);
    }

    public static void Render(Item Item, PoseStack PoseStack, SubmitNodeCollector SubmitNodeCollector, int LightCoords) {
        List<RenderRecord> ItemRenderers = Renderers.get(Item);
        if (ItemRenderers == null) return;

        float Rotation = ((System.currentTimeMillis() % 3600L) / 10.0F);

        PoseStack.pushPose();

        PoseStack.translate(0.2, 0, -0.1);

        /*PoseStack.mulPose(Axis.XP.rotationDegrees(Rotation * 2.0F));
        PoseStack.mulPose(Axis.YP.rotationDegrees(Rotation * 3.0F));
        PoseStack.mulPose(Axis.ZP.rotationDegrees(Rotation * 1.5F));*/

        PoseStack.mulPose(Axis.YP.rotationDegrees(200f));
        PoseStack.mulPose(Axis.XP.rotationDegrees(-7f));

        for (RenderRecord Entry : ItemRenderers) {
            String Namespace = Entry.Namespace;

            if (Namespace.isEmpty() || Namespace.isBlank()) {
                Namespace = "minecraft";
            }

            Identifier Geometry = Identifier.fromNamespaceAndPath(Namespace, Entry.GeometryPath());
            Identifier Texture = Identifier.fromNamespaceAndPath(Namespace, Entry.TexturePath());

            BedrockModel Model = Models.computeIfAbsent(Geometry, BedrockModel::Load);
            if (Model == null) {JavaUnbound.LOGGER.error("Failed to load Bedrock model: {}", Geometry); continue;}

            BedrockModelRenderer.Render(PoseStack, SubmitNodeCollector, LightCoords, Model, Texture, 0.5F, true);
        }

        PoseStack.popPose();
    }
}