package com.java_unbound.loader.definitions.global.materials.rendering;

import com.java_unbound.loader.definitions.global.materials.resolvers.MaterialResolver;
import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;

public final class BedrockItemMaterials {
    private static final Logger Log = LogUtils.getLogger();
    private static final Map<Identifier, BedrockRenderMaterial> Materials = new HashMap<>();

    static {
        Register("minecraft:totem_of_undying", "oreville_kipfdc");
        Register("minecraft:bell", BedrockMaterial.TRANSLUCENT_CULL);
        Register("minecraft:diamond", BedrockMaterial.EMISSIVE_OPAQUE_CULL);
        Register("minecraft:enchanted_golden_apple", "oreville_mqwxej");
        Register("minecraft:sulfur_cube_bucket", "entity_alphatest");
    }

    private BedrockItemMaterials() {}

    public static void Register(String ItemId, BedrockRenderMaterial Material) {
        Materials.put(Identifier.parse(ItemId), Material);
    }

    public static void Register(String ItemId, String MaterialName) {
        Materials.put(Identifier.parse(ItemId), new NamedMaterial(MaterialName));
    }

    public static BedrockRenderMaterial Get(Identifier ItemId) {
        return Materials.getOrDefault(ItemId, BedrockMaterial.TRANSLUCENT);
    }

    public static BedrockRenderMaterial Get(Item Item) {
        return Get(BuiltInRegistries.ITEM.getKey(Item));
    }

    public static BedrockRenderMaterial Get(ItemStack Stack) {
        return Get(Stack.getItem());
    }

    private static final class NamedMaterial implements BedrockRenderMaterial {
        private final String Name;
        private BedrockRenderMaterial Cached;
        private boolean Warned;

        NamedMaterial(String Name) {
            this.Name = Name;
        }

        private BedrockRenderMaterial Delegate() {
            if (Cached != null) {return Cached;}

            BedrockRenderMaterial Found = MaterialResolver.Get(Name);
            if (Found == null) {
                if (!Warned) {
                    Warned = true;
                    Log.warn("[BedrockItemMaterials] Material {} nicht gefunden, verwende TRANSLUCENT", Name);
                }
                return BedrockMaterial.TRANSLUCENT;
            }

            Cached = Found;
            return Found;
        }

        @Override
        public RenderType GetRenderType(Identifier Texture) {
            return Delegate().GetRenderType(Texture);
        }

        @Override
        public boolean IsFullBright() {
            return Delegate().IsFullBright();
        }
    }
}