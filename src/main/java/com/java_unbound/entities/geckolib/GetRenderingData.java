package com.java_unbound.entities.geckolib;

import com.java_unbound.JavaUnbound;
import com.java_unbound.entities.rendering.RenderController;
import com.java_unbound.loader.definitions.entity.EntityDefinition;
import com.java_unbound.loader.definitions.entity.types.geometry.EntityGeometryDefinition;
import com.java_unbound.loader.definitions.entity.types.render_controller.EntityRenderControllerPartVisibility;
import com.java_unbound.loader.definitions.entity.types.render_controller.EntityRenderControllerReference;
import com.java_unbound.loader.definitions.entity.types.texture.EntityTextureDefinition;
import com.java_unbound.loader.gecko_lib.entity.EntityRenderPass;

import com.java_unbound.molang.MolangParser;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;

import java.util.ArrayList;
import java.util.List;

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

            for (EntityRenderControllerReference RenderController : Definition.RenderControllers) {
                if (RenderController.Definition.PartVisibility == null) {continue;}

                for (EntityRenderControllerPartVisibility Part : RenderController.Definition.PartVisibility) {
                    String Visible = MolangParser.Evaluate(Entity, Part.Visibility);

                    JavaUnbound.LOGGER.info("Part: " + Part.BonePattern + ", Visible: " + Visible);
                }
            }

            if (Geometry == null) {
                JavaUnbound.LOGGER.error("Geometry not found: {}", GeometryIdentifier);
                continue;
            }

            for (String TextureIdentifier : Controller.Textures()) {
                String TextureAlias = TextureIdentifier.replaceFirst("^Texture\\.", "");

                EntityTextureDefinition Texture = Definition.Textures.get(TextureAlias);

                if (Texture == null) {
                    JavaUnbound.LOGGER.error("Texture not found: {}", TextureAlias);
                    continue;
                }

                Identifier GeometryPath = Identifier.fromNamespaceAndPath("minecraft", Geometry.Identifier);
                String TextureResourcePath = Texture.ResourcePath.endsWith(".png") ? Texture.ResourcePath : Texture.ResourcePath + ".png";
                Identifier TexturePath = Identifier.fromNamespaceAndPath("minecraft", TextureResourcePath);

                RenderPasses.add(new EntityRenderPass(GeometryPath, TexturePath));
            }
        }

        return RenderPasses;
    }
}