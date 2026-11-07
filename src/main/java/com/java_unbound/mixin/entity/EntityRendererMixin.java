package com.java_unbound.mixin.entity;

import com.java_unbound.loader.definitions.entity.EntityDefinition;
import com.java_unbound.molang.MolangParser;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin {
   /*@Inject(method = "extractRenderState", at = @At("TAIL"))
    private void entity_Effects$outline(Entity Entity, EntityRenderState State, float PartialTicks, CallbackInfo CI) {
        EntityDefinition Definition = EntityDefinition.GetEntityDefinition(Entity);
        if (Definition == null) {return;}

        if (Definition.Scripts != null && Definition.Scripts.PreAnimation != null) {
            Definition.Scripts.PreAnimation.forEach(Script -> MolangParser.Evaluate(Entity, Script));
        }
    }*/
}
