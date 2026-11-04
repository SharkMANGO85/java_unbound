package com.java_unbound.entities.geckolib;

import com.java_unbound.JavaUnbound;
import com.java_unbound.entities.rendering.RenderController;

import com.java_unbound.loader.definitions.entity.EntityDefinition;
import com.java_unbound.loader.definitions.entity.types.geometry.EntityGeometryDefinition;
import com.java_unbound.loader.definitions.entity.types.texture.EntityTextureDefinition;
import com.java_unbound.loader.gecko_lib.entity.EntityRenderPass;

import com.java_unbound.molang.MolangParser;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;

import java.util.ArrayList;
import java.util.List;

import static com.terraformersmc.modmenu.ModMenu.LOGGER;

public class GetRenderingData {
    public static List<EntityRenderPass> GetRenderingData(Entity Entity) {
        List<EntityRenderPass> RenderPasses = new ArrayList<>();

        EntityDefinition Definition = EntityDefinition.GetEntityDefinition(Entity);

        if (Definition == null) {
            JavaUnbound.LOGGER.error("Entity definition not found!");
            return RenderPasses;
        }

        List<RenderController.ResolvedRenderController> ActiveRenderControllers = RenderController.RenderEntity(Entity);

        if (ActiveRenderControllers == null) {
            JavaUnbound.LOGGER.warn("Active Render Controllers is null!");
            return RenderPasses;
        }

        for (RenderController.ResolvedRenderController Controller : ActiveRenderControllers) {
            String GeometryIdentifier = Controller.Geometry().replaceFirst("^Geometry\\.", "");

            EntityGeometryDefinition Geometry = Definition.Geometries.get(GeometryIdentifier);

            if (Geometry == null) {
                Geometry = Definition.Geometries.get("default");
                //JavaUnbound.LOGGER.error("Geometry not found: {}, Entity: {}", GeometryIdentifier, Entity.getName().getString());
                //continue;
            }

            if (Geometry == null) {
                JavaUnbound.LOGGER.error("Geometry not found: {}, Entity: {}", GeometryIdentifier, Entity.getName().getString());
                continue;
            }

            for (String TextureIdentifier : Controller.Textures()) {
                String TextureAlias = TextureIdentifier.replaceFirst("^Texture\\.", "");
                EntityTextureDefinition Texture = Definition.Textures.get(TextureAlias);

                if (Texture == null) {
                    Texture = Definition.Textures.get("default");
                }

                if (Texture == null || Texture.AbsolutePath == null || Texture.ResourcePath == null) {JavaUnbound.LOGGER.error("Texture not found: {}", TextureAlias);continue;}

                Identifier GeometryPath = Identifier.fromNamespaceAndPath("minecraft", Geometry.Identifier);

                String TextureResourcePath = Texture.ResourcePath.endsWith(".png") ? Texture.ResourcePath : Texture.ResourcePath + ".png";

                Identifier TexturePath = Identifier.fromNamespaceAndPath("minecraft", TextureResourcePath);

                EntityRenderPass.EntityScale Scale = ResolveScale(Entity, Definition);

                RenderPasses.add(new EntityRenderPass(GeometryPath, TexturePath, Controller.PartVisibility(), Scale));
            }
        }

        return RenderPasses;
    }

    private static EntityRenderPass.EntityScale ResolveScale(Entity Entity, EntityDefinition Definition) {
        if (Definition.Scripts == null) {return EntityRenderPass.EntityScale.NONE;}

        var Scripts = Definition.Scripts;

        float Uniform = EvaluateFloat(Entity, Scripts.Scale, 1.0f);

        return new EntityRenderPass.EntityScale(EvaluateFloat(Entity, Scripts.ScaleX, Uniform), EvaluateFloat(Entity, Scripts.ScaleY, Uniform), EvaluateFloat(Entity, Scripts.ScaleZ, Uniform));
    }

    private static float EvaluateFloat(Entity Entity, String Expression, float Default) {
        if (Expression == null || Expression.isBlank()) {return Default;}

        try {
            return Float.parseFloat(MolangParser.Evaluate(Entity, Expression));
        } catch (Exception Exception) {
            return Default;
        }
    }
}