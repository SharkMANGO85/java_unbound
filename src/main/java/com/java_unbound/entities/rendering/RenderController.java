package com.java_unbound.entities.rendering;

import com.java_unbound.JavaUnbound;
import com.java_unbound.loader.definitions.entity.EntityDefinition;
import com.java_unbound.loader.definitions.entity.types.render_controller.EntityRenderControllerDefinition;
import com.java_unbound.loader.definitions.entity.types.render_controller.EntityRenderControllerReference;
import com.java_unbound.molang.MolangParser;
import com.java_unbound.molang.types.MolangRenderControllerParser;
import net.minecraft.world.entity.Entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RenderController {
    public record ResolvedRenderController(String Geometry, List<String> Textures, List<Map<String, String>> Materials) {}

    public static void RenderEntity(Entity Entity) {
        List<EntityRenderControllerReference> ActiveRenderControllers = new ArrayList<>();
        List<ResolvedRenderController> ToRender = new ArrayList<>();

        EntityDefinition Definition = EntityDefinition.GetEntityDefinition(Entity);
        if (Definition == null) {return;}

        ActiveRenderControllers = GetActiveRenderControllers(Entity, Definition);

        for (EntityRenderControllerReference RenderController : ActiveRenderControllers) {
            var Resolved = ResolveRenderController(Entity, RenderController);

            ToRender.add(Resolved);
        }
    }

    private static List<EntityRenderControllerReference> GetActiveRenderControllers(Entity Entity, EntityDefinition Definition) {
        List<EntityRenderControllerReference> ActiveRenderControllers = new ArrayList<>();

        for (EntityRenderControllerReference RenderController : Definition.RenderControllers) {
            if (MolangParser.IsConditionMet(Entity, RenderController.Condition)) {
                ActiveRenderControllers.add(RenderController);
            }
        }

        return ActiveRenderControllers;
    }

    private static ResolvedRenderController ResolveRenderController(Entity Entity, EntityRenderControllerReference RenderController) {
        var Definition = RenderController.Definition;

        String Geometry = MolangRenderControllerParser.ResolveGeometry(Entity, Definition.Geometry, Definition.Arrays);
        List<String> Textures = MolangRenderControllerParser.ResolveTextures(Entity, Definition.Textures, Definition.Arrays);
        List<Map<String, String>> Materials = MolangRenderControllerParser.ResolveMaterials(Entity, Definition.Materials, Definition.Arrays);

        return new ResolvedRenderController(Geometry, Textures, Materials);
    }
}
