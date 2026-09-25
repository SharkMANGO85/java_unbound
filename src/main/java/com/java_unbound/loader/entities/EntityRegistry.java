package com.java_unbound.loader.entities;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.java_unbound.JavaUnbound;
import com.java_unbound.global.HashMaps;
import com.java_unbound.loader.definitions.EntitiesDefinition;
import com.java_unbound.loader.definitions.RenderControllersDefinition;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;

public class EntityRegistry {
    private String Identifier = "";
    private HashMap<String, Path> Textures = null;
    private HashMap<String, JsonElement> Geometries = null;
    private HashMap<String, JsonElement> RenderControllers = null;
    private JsonElement Animations = JsonNull.INSTANCE;
    private JsonElement Scripts = JsonNull.INSTANCE;
    private JsonElement SoundEffects = JsonNull.INSTANCE;
    private JsonElement ParticleEffects = JsonNull.INSTANCE;
    private boolean EnableAttachables = true;
    private JsonElement SpawnEgg = JsonNull.INSTANCE;

    public void LoadEntity(String Identifier) {
        JsonElement EntityFile = EntitiesDefinition.GetEntityJsonByIdentifier(Identifier);

        if (EntityFile == null) {
            JavaUnbound.LOGGER.error("TEXTURES: No Entity file found for " + Identifier);
            return;
        }

        if (!EntityFile.isJsonObject()) {
            JavaUnbound.LOGGER.error("TEXTURES: Entity file is not a JSON object: " + Identifier);
            return;
        }

        JsonObject EntityObject = EntityFile.getAsJsonObject();
        JsonElement DescriptionElement = EntityObject.get("description");

        if (DescriptionElement == null || !DescriptionElement.isJsonObject()) {
            JavaUnbound.LOGGER.error("TEXTURES: Entity has no description object: " + Identifier);
            return;
        }

        JsonObject Description = DescriptionElement.getAsJsonObject();

        this.Identifier = Identifier;
        this.Textures = EntityResolver.GetTextures(Identifier, Description);
        this.Geometries = EntityResolver.GetGeometries(Identifier, Description);
        this.RenderControllers = EntityResolver.GetRenderControllers(Identifier, Description);

        HashMaps.EntityRegistries.put(Identifier, this);
    }
}