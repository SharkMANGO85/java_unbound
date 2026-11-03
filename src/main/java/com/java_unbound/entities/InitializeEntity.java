package com.java_unbound.entities;

import com.java_unbound.JavaUnbound;
import com.java_unbound.entities.interfaces.EntityVariableInterface;
import com.java_unbound.loader.definitions.entity.EntityDefinition;
import com.java_unbound.loader.definitions.entity.types.render_controller.EntityRenderControllerReference;
import com.java_unbound.molang.MolangParser;
import net.minecraft.world.entity.Entity;

import static com.java_unbound.entities.functions.SetEntityBiome.SetEntityBiome;
import static com.java_unbound.entities.rendering.RenderController.RenderEntity;

public class InitializeEntity {
    public static void InitializeEntity(Entity Entity) {
        if (Entity == null || !Entity.level().isClientSide()) {return;}

        if (!(Entity instanceof EntityVariableInterface Variables) || Variables.JavaUnbound$isInitialized()) {return;}

        EntityDefinition Definition = EntityDefinition.GetEntityDefinition(Entity);
        if (Definition == null) {return;}

        InitializeBiome(Entity);

        if (Definition.Scripts != null && Definition.Scripts.Initialize != null) {
            Definition.Scripts.Initialize.forEach(Script -> MolangParser.Evaluate(Entity, Script));
        }

        Variables.JavaUnbound$setInitialized(true);

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