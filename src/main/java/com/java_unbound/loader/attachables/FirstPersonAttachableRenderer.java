package com.java_unbound.loader.attachables;

import com.google.gson.JsonElement;
import com.java_unbound.JavaUnbound;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class FirstPersonAttachableRenderer {
    public static boolean HasAttachable(ItemStack ItemStack) {
        if (ItemStack == null || ItemStack.isEmpty()) {
            return false;
        }

        String ItemId = BuiltInRegistries.ITEM.getKey(ItemStack.getItem()).toString();
        return AttachableRegistry.Get(ItemId) != null;
    }

    private static void GetDefintions(AttachableDefinition Attachable) {
        String Identifier = Attachable.Identifier;
        JsonElement Materials = Attachable.Materials;
        JsonElement Textures = Attachable.Textures;
        List<JsonElement> Geometries = Attachable.Geometries;
        JsonElement Animations = Attachable.Animations;
        JsonElement Scripts = Attachable.Scripts;
        List<JsonElement> RenderControllers = Attachable.RenderControllers;

        JavaUnbound.LOGGER.error("RenderControllers: " + RenderControllers);
    }

    public static void Render(float TickDelta, PoseStack PoseStack, SubmitNodeCollector Collector, LocalPlayer Player, int Light) {
        if (PoseStack == null || Collector == null || Player == null) {return;}

        ItemStack ItemStack = Player.getMainHandItem();
        if (ItemStack == null || ItemStack.isEmpty()) {return;}

        String ItemId = BuiltInRegistries.ITEM.getKey(ItemStack.getItem()).toString();
        AttachableDefinition Attachable = AttachableRegistry.Get(ItemId);

        GetDefintions(Attachable);

        if (Attachable == null || Attachable.Geometries == null || Attachable.Geometries.isEmpty()) {return;}

        var Geometry = Attachable.Geometries.getFirst();
        if (Geometry == null) {return;}

        AttachableRenderer.Render(Geometry, null, PoseStack, Collector, Light);
    }
}

/*



 */