package com.java_unbound.attachables.rendering;

import com.java_unbound.geckolib.entity.EntityRenderPass;
import com.java_unbound.loader.definitions.attachable.AttachableDefinition;
import com.java_unbound.loader.definitions.entity.types.render_controller.EntityRenderControllerReference;
import com.java_unbound.molang.MolangParser;
import com.java_unbound.molang.types.MolangAttachableParser;
import com.java_unbound.molang.types.MolangEntityRenderControllerParser;
import com.java_unbound.molang.types.MolangRenderControllerAttachableParser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RenderController {
    public record ResolvedRenderController(String Geometry, List<String> Textures, List<Map<String, String>> Materials, List<AttachableRenderPass.PartVisibility> PartVisibility) {}

    public static List<ResolvedRenderController> RenderAttachable(MolangAttachableParser.Context Context) {
        List<ResolvedRenderController> ToRender = new ArrayList<>();

        AttachableDefinition Definition = AttachableDefinition.GetAttachableDefinition(Context);
        if (Definition == null) {return ToRender;}

        List<EntityRenderControllerReference> ActiveRenderControllers = GetActiveRenderControllers(Context, Definition);

        for (EntityRenderControllerReference ControllerReference : ActiveRenderControllers) {
            ResolvedRenderController Resolved = ResolveRenderController(Context, ControllerReference);

            ToRender.add(Resolved);
        }

        return ToRender;
    }

    private static List<EntityRenderControllerReference> GetActiveRenderControllers(MolangAttachableParser.Context Context, AttachableDefinition Definition) {
        List<EntityRenderControllerReference> ActiveRenderControllers = new ArrayList<>();

        if (Definition.RenderControllers == null) {
            return ActiveRenderControllers;
        }

        for (EntityRenderControllerReference ControllerReference : Definition.RenderControllers) {
            String Condition = ControllerReference.Condition;
            boolean IsActive = MolangAttachableParser.IsConditionMet(Context, Condition);

            if (IsActive) {
                ActiveRenderControllers.add(ControllerReference);
            }
        }

        return ActiveRenderControllers;
    }

    private static ResolvedRenderController ResolveRenderController(MolangAttachableParser.Context Context, EntityRenderControllerReference RenderController) {
        var Definition = RenderController.Definition;

        String Geometry = MolangRenderControllerAttachableParser.ResolveGeometry(Context, Definition.Geometry, Definition.Arrays);
        List<String> Textures = MolangRenderControllerAttachableParser.ResolveTextures(Context, Definition.Textures, Definition.Arrays);
        List<Map<String, String>> Materials = MolangRenderControllerAttachableParser.ResolveMaterials(Context, Definition.Materials, Definition.Arrays);

        List<AttachableRenderPass.PartVisibility> PartVisibility = new ArrayList<>();

        if (Definition.PartVisibility != null) {
            for (var Part : Definition.PartVisibility) {
                String Visible = MolangAttachableParser.Evaluate(Context, Part.Visibility);

                PartVisibility.add(new AttachableRenderPass.PartVisibility(Part.BonePattern, MolangParser.IsTrue(Visible)));
            }
        }

        return new ResolvedRenderController(Geometry, Textures, Materials, PartVisibility);
    }
}