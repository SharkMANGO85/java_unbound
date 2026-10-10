package com.java_unbound.entities.render;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.java_unbound.JavaUnbound;
import com.java_unbound.loader.definitions.global_use.RenderControllersDefinition;
import com.java_unbound.molang.MolangParser;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;

import java.util.HashMap;
import java.util.Map;

//return the active render controllers, which tell which texture, geometry, etc. to use
public class GetActiveRenderControllers {
    public static HashMap<String, JsonElement> GetActiveRenderControllers(Entity Entity) {
        String Identifier = BuiltInRegistries.ENTITY_TYPE.getKey(Entity.getType()).toString();
        JsonArray RenderControllers = RenderControllersDefinition.GetRenderControllersByEntityId(Identifier);

        HashMap<String, JsonElement> ResolvedRenderControllers = new HashMap<>();

        for (JsonElement RenderControllerElement : RenderControllers) {
            if (RenderControllerElement.isJsonPrimitive()) {
                String RenderControllerIdentifier = RenderControllerElement.getAsString();
                JsonElement RenderController = RenderControllersDefinition.GetRenderControllerByIdentifier(RenderControllerIdentifier);

                if (RenderController == null) {
                    JavaUnbound.LOGGER.error("RENDER CONTROLLER: Could not resolve render controller '{}' for entity '{}'", RenderControllerIdentifier, Identifier);
                    continue;
                }

                //JavaUnbound.LOGGER.info("RC RESOLVED: Entity='{}' Controller='{}' JSON={}", Identifier, RenderControllerIdentifier, RenderController);

                ResolvedRenderControllers.put(RenderControllerIdentifier, RenderController);
                continue;
            }

            if (RenderControllerElement.isJsonObject()) {
                JsonObject ConditionalController = RenderControllerElement.getAsJsonObject();

                for (Map.Entry<String, JsonElement> Entry : ConditionalController.entrySet()) {
                    String RenderControllerIdentifier = Entry.getKey();
                    JsonElement Condition = Entry.getValue();
                    JsonElement RenderController = RenderControllersDefinition.GetRenderControllerByIdentifier(RenderControllerIdentifier);

                    if (RenderController == null) {
                        JavaUnbound.LOGGER.error("RENDER CONTROLLER: Could not resolve render controller '{}' for entity '{}'", RenderControllerIdentifier, Identifier);
                        continue;
                    }

                    //JavaUnbound.LOGGER.info("RC RESOLVED: Entity='{}' Controller='{}' JSON={}", Identifier, RenderControllerIdentifier, RenderController);

                    double Value = MolangParser.Evaluate(Entity, Condition.getAsString());

                    if (MolangParser.IsTrue(Value)) {
                        ResolvedRenderControllers.put(RenderControllerIdentifier, RenderController);
                    }
                }
            }
        }

        return ResolvedRenderControllers;
    }
}
