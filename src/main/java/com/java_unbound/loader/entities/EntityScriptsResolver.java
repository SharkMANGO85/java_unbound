package com.java_unbound.loader.entities;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.java_unbound.JavaUnbound;
import com.java_unbound.loader.definitions.EntitiesDefinition;
import com.java_unbound.loader.molang.MolangExpression;
import com.java_unbound.loader.molang.MolangParser;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;

import java.io.IOException;

public class EntityScriptsResolver {
    public void ResolveScripts(Entity RenderEntity) throws IOException {
        String Identifier = BuiltInRegistries.ENTITY_TYPE.getKey(RenderEntity.getType()).toString();
        JsonElement EntityJson = EntitiesDefinition.GetEntityJsonByIdentifier(Identifier);

        if (EntityJson == null || !EntityJson.isJsonObject()) {return;}

        JsonObject RootObject = EntityJson.getAsJsonObject();

        if (!RootObject.has("minecraft:client_entity") || !RootObject.get("minecraft:client_entity").isJsonObject()) {return;}

        JsonObject EntityObject = RootObject.getAsJsonObject("minecraft:client_entity");

        if (!EntityObject.has("description") || !EntityObject.get("description").isJsonObject()) {return;}

        JsonObject Description = EntityObject.getAsJsonObject("description");

        if (!Description.has("scripts") || !Description.get("scripts").isJsonObject()) {return;}

        JsonObject Scripts = Description.getAsJsonObject("scripts");

        ResolveInitialize(Scripts, RenderEntity);
        ResolvePreAnimation(Scripts, RenderEntity);
        ResolveAnimate(Scripts, RenderEntity);
        ResolveScale(Scripts, RenderEntity);
        ResolveVariables(Scripts, RenderEntity);
    }

    private static void ResolveInitialize(JsonObject Scripts, Entity RenderEntity) {
        if (!Scripts.has("initialize") || !Scripts.get("initialize").isJsonArray()) {return;}

        JsonArray Initialize = Scripts.getAsJsonArray("initialize");

        for (JsonElement Element : Initialize) {
            if (!Element.isJsonPrimitive() || !Element.getAsJsonPrimitive().isString()) {continue;}

            String Expression = Element.getAsString();
            MolangExpression Parsed = MolangParser.Classify(Expression);

            JavaUnbound.LOGGER.error("[Initialize] " + Parsed);
        }
    }

    private static void ResolvePreAnimation(JsonObject Scripts, Entity RenderEntity) {
        if (!Scripts.has("pre_animation") || !Scripts.get("pre_animation").isJsonArray()) {return;}

        JsonArray PreAnimation = Scripts.getAsJsonArray("pre_animation");

        for (JsonElement Element : PreAnimation) {
            if (!Element.isJsonPrimitive() || !Element.getAsJsonPrimitive().isString()) {continue;}

            String Expression = Element.getAsString();
            MolangExpression Parsed = MolangParser.Classify(Expression);

            JavaUnbound.LOGGER.error("[PreAnimation] " + Parsed);
        }
    }

    private static void ResolveAnimate(JsonObject Scripts, Entity RenderEntity) {
        if (!Scripts.has("animate") || !Scripts.get("animate").isJsonArray()) {return;}

        JsonArray Animate = Scripts.getAsJsonArray("animate");

        for (JsonElement Element : Animate) {
            if (Element.isJsonPrimitive() && Element.getAsJsonPrimitive().isString()) {
                String Expression = Element.getAsString();
                
                JavaUnbound.LOGGER.error("[Animate] " + MolangParser.Classify(Expression));
            }
        }
    }

    private static void ResolveScale(JsonObject Scripts, Entity RenderEntity) {
        if (!Scripts.has("scale") || !Scripts.get("scale").isJsonPrimitive()) {return;}

        String Expression = Scripts.get("scale").getAsString();
        MolangExpression Parsed = MolangParser.Classify(Expression);

        JavaUnbound.LOGGER.error("[Scale] " + Parsed);
    }

    private static void ResolveVariables(JsonObject Scripts, Entity RenderEntity) {
        if (!Scripts.has("variables") || !Scripts.get("variables").isJsonObject()) {return;}

        JsonObject Variables = Scripts.getAsJsonObject("variables");
        
        for (String Key : Variables.keySet()) {
            String Value = Variables.get(Key).getAsString();

            JavaUnbound.LOGGER.error("[Variable] " + Key + " = " + Value);
        }
    }
}