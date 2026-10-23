package com.java_unbound.mixin.entity;

import com.google.gson.JsonElement;
import com.java_unbound.entities.functions.GetActiveRenderControllers;
import com.java_unbound.entities.interfaces.LivingEntityRenderStateInterface;
import com.java_unbound.entities.loaders.EntityRegistry;
//import com.java_unbound.entities.render.geometry.GeometryRenderer;
import com.java_unbound.utils.variables.HashMaps;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;

@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin {
    @Inject(method = "submit", at = @At("HEAD"), cancellable = true)
    private void JavaUnbound$RenderEntity(LivingEntityRenderState State, PoseStack PoseStack, SubmitNodeCollector SubmitNodeCollector, CameraRenderState CameraRenderState, CallbackInfo CallbackInfo) {
        LivingEntity Entity = ((LivingEntityRenderStateInterface) State).JavaUnbound$GetEntity();
        if (Entity == null) {return;}

        String EntityIdentifier = BuiltInRegistries.ENTITY_TYPE.getKey(Entity.getType()).toString();
        EntityRegistry EntityRegistry = HashMaps.EntityRegistries.get(EntityIdentifier);

        if (EntityRegistry == null) {return;}
        if (EntityRegistry.Geometries == null || EntityRegistry.Geometries.isEmpty()) {return;}

        HashMap<String, JsonElement> ActiveRenderControllers = GetActiveRenderControllers.GetActiveRenderControllers(Entity);
        if (ActiveRenderControllers.isEmpty()) {return;}

        //GeometryRenderer.RenderGeometries(PoseStack, SubmitNodeCollector, ActiveRenderControllers);

        //CallbackInfo.cancel();
    }
}