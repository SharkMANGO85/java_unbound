package com.java_unbound.loader.entities;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.java_unbound.JavaUnbound;
import com.java_unbound.global.HashMaps;
import com.java_unbound.loader.definitions.EntitiesDefinition;

import java.nio.file.Path;
import java.util.HashMap;

public class EntityRegistry {
    public String Identifier = "";
    public HashMap<String, Path> Textures = null;
    public HashMap<String, JsonElement> Geometries = null;
    public HashMap<String, JsonElement> RenderControllers = null;
    public JsonElement Animations = JsonNull.INSTANCE;
    public HashMap<String, JsonArray> Scripts = null;
    public JsonElement SoundEffects = JsonNull.INSTANCE;
    public JsonElement ParticleEffects = JsonNull.INSTANCE;
    public boolean EnableAttachables = true;
    public JsonElement SpawnEgg = JsonNull.INSTANCE;

    public void LoadEntity(String Identifier) {
        JsonElement EntityFile = EntitiesDefinition.GetEntityJsonByIdentifier(Identifier);

        if (EntityFile == null) {
            JavaUnbound.LOGGER.error("ENTITY: No entity file found for " + Identifier);
            return;
        }

        if (!EntityFile.isJsonObject()) {
            JavaUnbound.LOGGER.error("ENTITY: Entity file is not a JSON object: " + Identifier);
            return;
        }

        JsonObject EntityObject = EntityFile.getAsJsonObject();
        JsonElement DescriptionElement = EntityObject.get("description");

        if (DescriptionElement == null || !DescriptionElement.isJsonObject()) {
            JavaUnbound.LOGGER.error("ENTITY: Entity has no description object: " + Identifier);
            return;
        }

        JsonObject Description = DescriptionElement.getAsJsonObject();

        this.Identifier = Identifier;
        this.Textures = EntityResolver.GetTextures(Identifier, Description);
        this.Geometries = EntityResolver.GetGeometries(Identifier, Description);
        this.RenderControllers = EntityResolver.GetRenderControllers(Identifier, Description);
        this.Scripts = EntityResolver.GetScripts(Identifier, Description);

        HashMaps.EntityRegistries.put(Identifier, this);
    }
}