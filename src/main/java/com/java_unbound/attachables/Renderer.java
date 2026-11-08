package com.java_unbound.attachables;

import com.java_unbound.attachables.rendering.AttachableRenderPass;
import com.java_unbound.attachables.rendering.GetRenderingData;
import com.java_unbound.attachables.rendering.bedrock.BedrockModel;
import com.java_unbound.attachables.rendering.bedrock.BedrockModelRenderer;
import com.java_unbound.loader.definitions.attachable.AttachableDefinition;
import com.java_unbound.loader.definitions.global.materials.rendering.BedrockItemMaterials;
import com.java_unbound.loader.definitions.global.materials.rendering.BedrockRenderMaterial;
import com.java_unbound.molang.types.MolangAttachableParser;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;

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

    public static void RegisterItemShorted(Item Item, List<RenderRecord> Renderers) {
        List<RenderRecord> Expanded = new ArrayList<>(Renderers.size());

        for (RenderRecord Record : Renderers) {
            Expanded.add(new RenderRecord(Record.Namespace(), "textures/oreville/ans/" + Record.TexturePath() + ".png", "geometries/attachables/geometry.oreville_ans." + Record.GeometryPath() + ".json"));
        }

        RegisterItem(Item, Expanded);
    }

    public static void RegisterItemShorted(Item Item, RenderRecord... Renderers) {
        RegisterItemShorted(Item, List.of(Renderers));
    }

    public static void RegisterItem(Item Item, RenderRecord... Renderers) {
        RegisterItem(Item, List.of(Renderers));
    }

    public static boolean HasRenderer(Item Item) {
        return AttachableDefinition.GetAttachableDefinition(Item) != null || Renderers.containsKey(Item);
    }

    private static void RegisterFromPasses(Item Item, List<AttachableRenderPass> RenderPasses) {
        List<RenderRecord> Records = new ArrayList<>(RenderPasses.size());

        for (AttachableRenderPass Pass : RenderPasses) {
            Records.add(new RenderRecord(Pass.Geometry().getNamespace(), Pass.Texture().getPath(), Pass.Geometry().getPath()));
        }

        if (!Records.equals(Renderers.get(Item))) {
            RegisterItem(Item, Records);
        }
    }

    public static void Render(MolangAttachableParser.Context Context, PoseStack PoseStack, SubmitNodeCollector SubmitNodeCollector, int LightCoords) {
        if (Context == null) {return;}

        long Time = System.currentTimeMillis();

        float AngleX = (Time % 3000L) / 3000.0F * 360.0F; // 1 turn per 3.0s
        float AngleY = (Time % 2000L) / 2000.0F * 360.0F; // 1 turn per 2.0s
        float AngleZ = (Time % 4000L) / 4000.0F * 360.0F; // 1 turn per 4.0s

        List<AttachableRenderPass> RenderPasses = GetRenderingData.GetRenderingData(Context);
        if (RenderPasses.isEmpty()) {return;}

        RegisterFromPasses(Context.Stack.getItem(), RenderPasses);

        boolean OffHand = Context.Slot == MolangAttachableParser.Slot.OFF_HAND;
        BedrockRenderMaterial Material = BedrockItemMaterials.Get(Context.Stack.getItem());

        PoseStack.pushPose();
        PoseStack.translate(OffHand ? -0 : 0, 0, -1);

        PoseStack.mulPose(Axis.XP.rotationDegrees(AngleX));
        PoseStack.mulPose(Axis.YP.rotationDegrees(AngleY));
        PoseStack.mulPose(Axis.ZP.rotationDegrees(AngleZ));

        for (AttachableRenderPass Pass : RenderPasses) {
            BedrockModel Model = Models.computeIfAbsent(Pass.Geometry(), BedrockModel::Load);
            if (Model == null) { continue;}

            PoseStack.pushPose();
            PoseStack.scale(Pass.Scale().X(), Pass.Scale().Y(), Pass.Scale().Z());

            BedrockModelRenderer.Render(PoseStack, SubmitNodeCollector, LightCoords, Model, Pass.Texture(), 0.5F, false, null, null, Material);

            PoseStack.popPose();
        }

        PoseStack.popPose();
    }

    /*public static void Render(Item Item, PoseStack PoseStack, SubmitNodeCollector SubmitNodeCollector, int LightCoords, boolean OffHand) {
        List<RenderRecord> ItemRenderers = Renderers.get(Item);
        if (ItemRenderers == null) return;

        long time = System.currentTimeMillis();

        float angleX = (time % 3000L) / 3000.0F * 360.0F; // 1 turn per 3.0s
        float angleY = (time % 2000L) / 2000.0F * 360.0F; // 1 turn per 2.0s
        float angleZ = (time % 4000L) / 4000.0F * 360.0F; // 1 turn per 4.0s

        PoseStack.pushPose();
        PoseStack.translate(OffHand ? -0 : 0, 0, -1);

        PoseStack.mulPose(Axis.XP.rotationDegrees(angleX));
        PoseStack.mulPose(Axis.YP.rotationDegrees(angleY));
        PoseStack.mulPose(Axis.ZP.rotationDegrees(angleZ));

        PoseStack.mulPose(Axis.YP.rotationDegrees(180));

        BedrockRenderMaterial Material = BedrockItemMaterials.Get(Item);

        for (RenderRecord Entry : ItemRenderers) {
            String Namespace = Entry.Namespace;

            if (Namespace.isEmpty() || Namespace.isBlank()) {
                Namespace = "minecraft";
            }

            Identifier Geometry = Identifier.fromNamespaceAndPath(Namespace, Entry.GeometryPath());
            Identifier Texture = Identifier.fromNamespaceAndPath(Namespace, Entry.TexturePath());

            BedrockModel Model = Models.computeIfAbsent(Geometry, BedrockModel::Load);
            if (Model == null) {JavaUnbound.LOGGER.error("Failed to load Bedrock model: {}", Geometry); continue;}

            BedrockModelRenderer.Render(PoseStack, SubmitNodeCollector, LightCoords, Model, Texture, 0.5F, true, null, null, Material);
        }

        PoseStack.popPose();
    }*/
}