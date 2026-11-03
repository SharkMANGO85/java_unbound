package com.java_unbound.entities.rendering;

import com.java_unbound.JavaUnbound;
import com.java_unbound.loader.definitions.entity.EntityDefinition;
import com.java_unbound.loader.definitions.entity.types.render_controller.EntityRenderControllerReference;
import com.java_unbound.loader.gecko_lib.entity.EntityRenderPass;
import com.java_unbound.molang.MolangParser;
import com.java_unbound.molang.types.MolangRenderControllerParser;

import net.minecraft.world.entity.Entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RenderController {
    public record ResolvedRenderController(String Geometry, List<String> Textures, List<Map<String, String>> Materials, List<EntityRenderPass.PartVisibility> PartVisibility) {}

    public static List<ResolvedRenderController> RenderEntity(Entity Entity) {
        List<ResolvedRenderController> ToRender = new ArrayList<>();

        EntityDefinition Definition = EntityDefinition.GetEntityDefinition(Entity);
        if (Definition == null) {return ToRender;}

        List<EntityRenderControllerReference> ActiveRenderControllers = GetActiveRenderControllers(Entity, Definition);

        for (EntityRenderControllerReference ControllerReference : ActiveRenderControllers) {
            ResolvedRenderController Resolved = ResolveRenderController(Entity, ControllerReference);

            ToRender.add(Resolved);
        }

        return ToRender;
    }

    private static List<EntityRenderControllerReference> GetActiveRenderControllers(Entity Entity, EntityDefinition Definition) {
        List<EntityRenderControllerReference> ActiveRenderControllers = new ArrayList<>();

        for (EntityRenderControllerReference ControllerReference : Definition.RenderControllers) {
            String Condition = ControllerReference.Condition;

            boolean IsActive = MolangParser.IsConditionMet(Entity, Condition);

            if (IsActive) {
                ActiveRenderControllers.add(ControllerReference);
            }
        }

        return ActiveRenderControllers;
    }

    private static ResolvedRenderController ResolveRenderController(Entity Entity, EntityRenderControllerReference RenderController) {
        var Definition = RenderController.Definition;

        String Geometry = MolangRenderControllerParser.ResolveGeometry(Entity, Definition.Geometry, Definition.Arrays);
        List<String> Textures = MolangRenderControllerParser.ResolveTextures(Entity, Definition.Textures, Definition.Arrays);
        List<Map<String, String>> Materials = MolangRenderControllerParser.ResolveMaterials(Entity, Definition.Materials, Definition.Arrays);

        List<EntityRenderPass.PartVisibility> PartVisibility = new ArrayList<>();

        if (Definition.PartVisibility != null) {
            for (var Part : Definition.PartVisibility) {
                String Visible = MolangParser.Evaluate(Entity, Part.Visibility);

                PartVisibility.add(new EntityRenderPass.PartVisibility(Part.BonePattern, MolangParser.IsTrue(Visible)));
            }
        }

        return new ResolvedRenderController(Geometry, Textures, Materials, PartVisibility);
    }
}