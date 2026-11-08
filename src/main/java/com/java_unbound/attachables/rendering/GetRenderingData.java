package com.java_unbound.attachables.rendering;

import com.java_unbound.JavaUnbound;
import com.java_unbound.loader.definitions.attachable.AttachableDefinition;
import com.java_unbound.loader.definitions.attachable.types.texture.AttachableTextureDefinition;
import com.java_unbound.loader.definitions.entity.EntityDefinition;
import com.java_unbound.loader.definitions.entity.types.geometry.EntityGeometryDefinition;
import com.java_unbound.molang.MolangParser;
import com.java_unbound.molang.types.MolangAttachableParser;
import com.java_unbound.attachables.*;
import com.java_unbound.utils.variables.HashMaps;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;

import java.util.ArrayList;
import java.util.List;

public class GetRenderingData {
    public static List<AttachableRenderPass> GetRenderingData(MolangAttachableParser.Context Context) {
        List<AttachableRenderPass> RenderPasses = new ArrayList<>();

        AttachableDefinition Definition = AttachableDefinition.GetAttachableDefinition(Context);
        if (Definition == null) {return RenderPasses;}

        List<RenderController.ResolvedRenderController> ActiveRenderControllers = RenderController.RenderAttachable(Context);
        if (ActiveRenderControllers == null) {return RenderPasses;}

        for (RenderController.ResolvedRenderController Controller : ActiveRenderControllers) {
            String GeometryIdentifier = Controller.Geometry().replaceFirst("^Geometry\\.", "");
            EntityGeometryDefinition Geometry = Definition.Geomtries.get(GeometryIdentifier);

            if (Geometry == null) {
                Geometry = Definition.Geomtries.get("default");
            }

            if (Geometry == null) {continue;}

            for (String TextureIdentifier : Controller.Textures()) {
                String TextureAlias = TextureIdentifier.replaceFirst("^Texture\\.", "");
                AttachableTextureDefinition Texture = Definition.Textures.get(TextureAlias);

                if (Texture == null) {Texture = Definition.Textures.get("default");}
                if (Texture == null || Texture.AbsolutePath == null || Texture.ResourcePath == null) {continue;}

                if (Texture.ResourcePath.contains("textures/oreville/ans/cdx")) {
                    continue;
                }

                String GeometryFile = Geometry.Identifier.endsWith(".json") ? Geometry.Identifier : Geometry.Identifier + ".json";
                Identifier GeometryPath = Identifier.fromNamespaceAndPath("minecraft", "geometries/attachables/" + GeometryFile);
                String TextureResourcePath = Texture.ResourcePath.endsWith(".png") ? Texture.ResourcePath : Texture.ResourcePath + ".png";
                Identifier TexturePath = Identifier.fromNamespaceAndPath("minecraft", TextureResourcePath);
                AttachableRenderPass.EntityScale Scale = ResolveScale(Context, Definition);

                RenderPasses.add(new AttachableRenderPass(GeometryPath, TexturePath, Controller.PartVisibility(), Scale));
            }
        }

        return RenderPasses;
    }

    private static AttachableRenderPass.EntityScale ResolveScale(MolangAttachableParser.Context Context, AttachableDefinition Definition) {
        if (Definition.Scripts == null) {return AttachableRenderPass.EntityScale.NONE;}

        var Scripts = Definition.Scripts;

        float Uniform = EvaluateFloat(Context, Scripts.Scale, 1.0f);

        return new AttachableRenderPass.EntityScale(EvaluateFloat(Context, Scripts.ScaleX, Uniform), EvaluateFloat(Context, Scripts.ScaleY, Uniform), EvaluateFloat(Context, Scripts.ScaleZ, Uniform));
    }

    private static float EvaluateFloat(MolangAttachableParser.Context Context, String Expression, float Default) {
        if (Expression == null || Expression.isBlank()) {return Default;}

        try {
            return Float.parseFloat(MolangAttachableParser.Evaluate(Context, Expression));
        } catch (Exception Exception) {
            return Default;
        }
    }
}