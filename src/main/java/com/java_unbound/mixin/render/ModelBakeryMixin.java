package com.java_unbound.mixin.render;

import net.minecraft.client.resources.model.ModelBakery;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(ModelBakery.class)
public class ModelBakeryMixin {
    @ModifyConstant(method = "<clinit>", constant = @Constant(stringValue = "fire_1"))
    private static String useFire0ForFire1(String original) {
        return "fire_0";
    }
}