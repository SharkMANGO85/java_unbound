package com.java_unbound.attachables;

import com.java_unbound.JavaUnbound;
import net.minecraft.world.item.Items;

import java.util.List;

public class AttachableRegistry {
    public static void RegisterItems() {
        Renderer.RegisterItem(Items.CREEPER_SPAWN_EGG, List.of(new Renderer.RenderRecord(JavaUnbound.MOD_ID, "textures/czc.png", "geometries/geometry.oreville_ans.fdzeyg.json")));
        Renderer.RegisterItem(Items.COW_SPAWN_EGG, List.of(new Renderer.RenderRecord(JavaUnbound.MOD_ID, "textures/czc.png", "geometries/geometry.oreville_ans.oodtml.json")));
    }
}