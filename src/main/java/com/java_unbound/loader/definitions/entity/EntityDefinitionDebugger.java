package com.java_unbound.loader.definitions.entity;

import com.java_unbound.JavaUnbound;
import com.java_unbound.loader.definitions.entity.types.render_controller.EntityRenderControllerReference;
import com.java_unbound.loader.definitions.entity.types.scripts.EntityScriptsAnimate;
import com.java_unbound.loader.definitions.entity.types.scripts.EntityScriptsDefinition;
import com.java_unbound.loader.definitions.entity.types.scripts.EntityScriptsVariables;
import com.java_unbound.loader.definitions.entity.types.spawn_egg.EntitySpawnEggDefinition;
import com.java_unbound.utils.variables.HashMaps;

public class EntityDefinitionDebugger {
    private static String Truncate(String Text, int Max) {
        if (Text == null) {return "null";}
        return Text.length() <= Max ? Text : Text.substring(0, Max) + "... (" + Text.length() + " chars)";
    }

    public static void DebugEntity(String Identifier) {
        EntityDefinition Definition = HashMaps.Entities.get(Identifier);

        if (Definition == null) {
            JavaUnbound.LOGGER.error("[DEBUG] Entity not loaded: {} | Loaded entities: {}", Identifier, HashMaps.Entities.keySet());
            return;
        }

        JavaUnbound.LOGGER.info("[DEBUG] ===== {} =====", Definition.Identifier);

        // Geometries
        JavaUnbound.LOGGER.info("[DEBUG] Geometries: {}", Definition.Geometries == null ? "null" : Definition.Geometries.size());
        if (Definition.Geometries != null) {
            Definition.Geometries.forEach((Alias, Geometry) ->
                    JavaUnbound.LOGGER.info("[DEBUG]   {} -> {} | Bones: {}", Alias, Geometry.Identifier, Geometry.Bones.size()));
        }

        // Textures
        JavaUnbound.LOGGER.info("[DEBUG] Textures: {}", Definition.Textures == null ? "null" : Definition.Textures.size());
        if (Definition.Textures != null) {
            Definition.Textures.forEach((Key, Texture) ->
                    JavaUnbound.LOGGER.info("[DEBUG]   {} -> {}", Key, Texture.AbsolutePath == null ? "UNRESOLVED" : Texture.AbsolutePath));
        }

        // Render controllers
        JavaUnbound.LOGGER.info("[DEBUG] Render controllers: {}", Definition.RenderControllers == null ? "null" : Definition.RenderControllers.size());
        if (Definition.RenderControllers != null) {
            for (EntityRenderControllerReference Reference : Definition.RenderControllers) {
                JavaUnbound.LOGGER.info("[DEBUG]   {} | Condition: {}", Reference.Identifier, Reference.Condition);
            }
        }

        // Scripts
        EntityScriptsDefinition Scripts = Definition.Scripts;
        if (Scripts == null) {
            JavaUnbound.LOGGER.info("[DEBUG] Scripts: null");
        } else {
            JavaUnbound.LOGGER.info("[DEBUG] Scripts | Initialize: {} | PreAnimation: {} | Animate: {} | Variables: {}",
                    Scripts.Initialize.size(), Scripts.PreAnimation.size(), Scripts.Animate.size(), Scripts.Variables.size());
            JavaUnbound.LOGGER.info("[DEBUG]   Scale: {}", Scripts.Scale);

            for (EntityScriptsAnimate Animate : Scripts.Animate) {
                JavaUnbound.LOGGER.info("[DEBUG]   animate: {} | Condition: {}", Animate.Key, Animate.Value);
            }
            for (EntityScriptsVariables Variable : Scripts.Variables) {
                JavaUnbound.LOGGER.info("[DEBUG]   variable: {} = {}", Variable.Key, Variable.Value);
            }
            for (int Index = 0; Index < Scripts.Initialize.size(); Index++) {
                JavaUnbound.LOGGER.info("[DEBUG]   initialize[{}]: {}", Index, Truncate(Scripts.Initialize.get(Index), 120));
            }
            for (int Index = 0; Index < Scripts.PreAnimation.size(); Index++) {
                JavaUnbound.LOGGER.info("[DEBUG]   pre_animation[{}]: {}", Index, Truncate(Scripts.PreAnimation.get(Index), 120));
            }
        }

        //Spawn Egg
        EntitySpawnEggDefinition SpawnEgg = Definition.SpawnEgg;

        if (SpawnEgg == null) {
            JavaUnbound.LOGGER.info("[DEBUG] SpawnEgg: null");
        } else {
            JavaUnbound.LOGGER.info("[DEBUG] SpawnEgg | Texture: {} | TextureIndex: {} | BaseColor: {} | OverlayColor: {}", SpawnEgg.Texture, SpawnEgg.TextureIndex, SpawnEgg.BaseColor, SpawnEgg.OverlayColor);
        }

        JavaUnbound.LOGGER.info("[DEBUG] ===== end {} =====", Definition.Identifier);
    }
}
