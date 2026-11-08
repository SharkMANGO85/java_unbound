package com.java_unbound.attachables;

import com.java_unbound.JavaUnbound;
import com.java_unbound.attachables.rendering.AttachableRenderPass;
import com.java_unbound.attachables.rendering.GetRenderingData;
import com.java_unbound.molang.types.MolangAttachableParser;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Items;

import java.util.List;

public class AttachableRegistry {
    public static void RegisterItems() {
        Renderer.RegisterItemShorted(Items.COMPARATOR, List.of(new Renderer.RenderRecord("", "czc", "lzupbq")));
        Renderer.RegisterItemShorted(Items.NAME_TAG, List.of(new Renderer.RenderRecord("", "czc", "gkbovu")));
        Renderer.RegisterItemShorted(Items.FLINT_AND_STEEL, List.of(new Renderer.RenderRecord("", "dla", "nfxeyk")));
        Renderer.RegisterItemShorted(Items.OAK_CHEST_BOAT, List.of(new Renderer.RenderRecord("", "czc", "btbtno")));
        Renderer.RegisterItemShorted(Items.BED.white(), List.of(new Renderer.RenderRecord("", "czc", "bpmnkv")));
        Renderer.RegisterItemShorted(Items.WOODEN_AXE, List.of(new Renderer.RenderRecord("", "dim", "gozzef")));
        Renderer.RegisterItemShorted(Items.TOTEM_OF_UNDYING, List.of(new Renderer.RenderRecord("", "czc", "qmejte")));
        Renderer.RegisterItemShorted(Items.GLASS_BOTTLE, List.of(new Renderer.RenderRecord("", "dkv", "awpzmx")));
        Renderer.RegisterItemShorted(Items.CARROT_ON_A_STICK, List.of(new Renderer.RenderRecord("", "czc", "vgqozs")));
        Renderer.RegisterItemShorted(Items.ARMOR_STAND, List.of(new Renderer.RenderRecord("", "czc", "azyeof")));
        Renderer.RegisterItemShorted(Items.BELL, List.of(new Renderer.RenderRecord("", "czc", "tzxalh")));
        Renderer.RegisterItemShorted(Items.SULFUR_CUBE_BUCKET, List.of(new Renderer.RenderRecord("", "djy", "esjnpo"), new Renderer.RenderRecord("", "npa", "tdirdq")));
        Renderer.RegisterItemShorted(Items.PUFFERFISH_BUCKET, List.of(new Renderer.RenderRecord("", "djy", "esjnpo"), new Renderer.RenderRecord("", "bfz", "zjhkch")));
        Renderer.RegisterItemShorted(Items.COPPER_CHAIN.weathering().unaffected(), List.of(new Renderer.RenderRecord("", "czc", "vsfhqm")));
        Renderer.RegisterItemShorted(Items.SLIME_SPAWN_EGG, List.of(new Renderer.RenderRecord("", "czc", "lekbxs")));
        Renderer.RegisterItemShorted(Items.DIAMOND, List.of(new Renderer.RenderRecord("", "czc", "xjqyru")));
        Renderer.RegisterItemShorted(Items.LANTERN, List.of(
                new Renderer.RenderRecord("", "czc", "dlyqho"),
                //new Renderer.RenderRecord("", "czc", "xohjbd"),
                //new Renderer.RenderRecord("", "czc", "ctdknl"),
                new Renderer.RenderRecord("", "nop", "evxwjz")
        ));
    }


    /*public static List<AttachableRenderPass> RegisterAttachable(MolangAttachableParser.Context Context) {
        return GetRenderingData.GetRenderingData();
    }*/
}