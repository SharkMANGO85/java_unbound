package com.java_unbound.entities.render_controller;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.java_unbound.JavaUnbound;
import com.java_unbound.global.EntityRenderControllerData;
import com.java_unbound.global.HashMaps;
import com.java_unbound.global.Vec4String;
import com.java_unbound.loader.definitions.global_use.RenderControllersDefinition;
import com.java_unbound.molang.MolangParser;
import com.java_unbound.molang.types.MolangRenderControllerParser;
import net.minecraft.world.entity.Entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//saves the data of a render controller
public class EntityRenderControllerRegistry {
    public String Identifier = "";

    public Map<String, Map<String, List<String>>> Arrays = new HashMap<>();

    public List<String> Textures = new ArrayList<>();
    public String Geometry = "";
    public Map<String, String> Materials = new HashMap<>();

    public List<Map<String, String>> PartVisibility = new ArrayList<>();

    public Map<String, List<String>> UVAnimation = new HashMap<>();

    public Vec4String OnFireColor = new Vec4String();
    public Vec4String IsHurtColor = new Vec4String();
    public Vec4String OverlayColor = new Vec4String();

    public boolean IgnoreLighting = false;
    public boolean FilterLighting = false;

    public String LightColorMultiplier = "";

    //Info: Identifier = the one of the render controller, not the one from the entity
    public void LoadEntityRenderController(String Identifier) {
        JsonElement EntityFile = RenderControllersDefinition.GetRenderControllerByIdentifier(Identifier);

        if (EntityFile == null) {
            JavaUnbound.LOGGER.error("ENTITY: No entity render controller file found for " + Identifier);
            return;
        }

        if (!EntityFile.isJsonObject()) {
            JavaUnbound.LOGGER.error("ENTITY: Entity Render Controller file is not a JSON object: " + Identifier);
            return;
        }

        JsonObject EntityRenderControllerObject = EntityFile.getAsJsonObject();

        this.Identifier = Identifier;
        this.Arrays = EntityRenderControllerResolver.GetArrays(Identifier, EntityRenderControllerObject);
        this.Textures = EntityRenderControllerResolver.GetTextures(Identifier, EntityRenderControllerObject);
        this.Geometry = EntityRenderControllerResolver.GetGeometries(Identifier, EntityRenderControllerObject);
        this.Materials = EntityRenderControllerResolver.GetMaterials(Identifier, EntityRenderControllerObject);
    }

    //returns, geometry, textures, materials, etc. for the current entity using the molang system to get the right ones for the current condition
    public static EntityRenderControllerData GetRenderControllerData(Entity Entity, String Identifier) {
        EntityRenderControllerRegistry Registry = HashMaps.EntityRenderControllers.get(Identifier);
        if (Registry == null) return null;

        EntityRenderControllerData ResolvedData = new EntityRenderControllerData();
        ResolvedData.Identifier = Registry.Identifier;
        ResolvedData.Geometry = MolangParser.EvaluateRenderController(Entity, Registry.Geometry, Registry);

        for (String Texture : Registry.Textures) {
            ResolvedData.Textures.add(MolangParser.EvaluateRenderController(Entity, Texture, Registry));
        }

        for (Map.Entry<String, String> Material : Registry.Materials.entrySet()) {
            ResolvedData.Materials.put(Material.getKey(), MolangParser.EvaluateRenderController(Entity, Material.getValue(), Registry));
        }

        return ResolvedData;
    }
}