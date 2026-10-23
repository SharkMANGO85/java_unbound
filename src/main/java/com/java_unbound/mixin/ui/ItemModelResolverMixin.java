package com.java_unbound.mixin.ui;

import com.java_unbound.loader.ui.textures.PotionBottleTextures;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier; // 1.21.10 and older: ResourceLocation
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ItemModelResolver.class)
public class ItemModelResolverMixin {

}