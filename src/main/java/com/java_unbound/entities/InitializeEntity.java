package com.java_unbound.entities;

import com.java_unbound.JavaUnbound;
import com.java_unbound.loader.definitions.entity.EntityDefinition;
import com.java_unbound.loader.definitions.entity.types.render_controller.EntityRenderControllerReference;
import com.java_unbound.molang.MolangParser;
import net.minecraft.world.entity.Entity;

import static com.java_unbound.entities.functions.SetEntityBiome.SetEntityBiome;
import static com.java_unbound.entities.rendering.RenderController.RenderEntity;

public class InitializeEntity {
    public static void InitializeEntity(Entity Entity) {
        if (Entity == null) {return;}

        InitializeBiome(Entity);

        EntityDefinition Definition = EntityDefinition.GetEntityDefinition(Entity);
        if (Definition == null) {return;}

        RenderEntity(Entity);
    }

    private static void InitializeBiome(Entity Entity) {
        try {
            SetEntityBiome(Entity);
        } catch (Exception Exception) {
            Exception.printStackTrace();
        }
    }
}