package com.java_unbound.entities.functions;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.java_unbound.JavaUnbound;
import com.java_unbound.entities.interfaces.EntityVariableInterface;
import com.java_unbound.loader.definitions.global_use.RenderControllersDefinition;
import com.java_unbound.molang.MolangParser;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;

import java.util.HashMap;
import java.util.Map;

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

                    double Value = MolangParser.Evaluate(Entity, String.valueOf(Condition));

                    //JavaUnbound.LOGGER.info("RENDER CONTROLLER: Entity='{}' Controller='{}' Condition={} Value={} Variables={}", Identifier, RenderControllerIdentifier, Condition, Value, ((EntityVariableInterface) Entity).JavaUnbound$getVariables());

                    if (Value == 1.0) {
                        ResolvedRenderControllers.put(RenderControllerIdentifier, RenderController);
                    }
                }
            }
        }

        return ResolvedRenderControllers;
    }
}
