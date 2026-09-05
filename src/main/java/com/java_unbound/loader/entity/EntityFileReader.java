package com.java_unbound.loader.entity;

import com.google.gson.JsonElement;
import com.java_unbound.JavaUnbound;
import com.java_unbound.loader.resourcepack.Folder;
import com.java_unbound.utils.functions.GetJsonFilesInPath;
import com.java_unbound.utils.functions.GetJsonValue;

import javax.swing.text.html.parser.Entity;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class EntityFileReader {
    private EntityFileReader() {

    }

    private static final Path ENTITY_FOLDER = Folder.GetResourceFolder().resolve("entity");
    private static final List<Path> ENTITY_JSONS = GetJsonFilesInPath.Get(ENTITY_FOLDER);

    public static void Read() {
        for (Path File : ENTITY_JSONS) {
            try {
                ReadEntityFile(File);
            } catch (IOException Exception) {
                Exception.printStackTrace();
            }
        }
    }

    private static JsonElement GetEntityIdentifier(String Content) {
        return GetJsonValue.GetValue(Content, "description.identifier");
    }

    private static JsonElement GetEntityMaterials(String Content) {
        return GetJsonValue.GetValue(Content, "description.materials");
    }

    private static JsonElement GetEntityTextures(String Content) {
        return GetJsonValue.GetValue(Content, "description.textures");
    }

    private static JsonElement GetEntityGeometry(String Content) {
        return GetJsonValue.GetValue(Content, "description.geometry");
    }

    private static JsonElement GetEntityAnimations(String Content) {
        return GetJsonValue.GetValue(Content, "description.animations");
    }

    private static JsonElement GetEntityScripts(String Content) {
        return GetJsonValue.GetValue(Content, "description.scripts");
    }

    private static JsonElement GetEntitySpawnEgg(String Content) {
        return GetJsonValue.GetValue(Content, "description.spawn_egg");
    }

    private static JsonElement GetEntityRenderControllers(String Content) {
        return GetJsonValue.GetValue(Content, "description.render_controllers");
    }

    private static JsonElement GetEntitySoundEffects(String Content) {
        return GetJsonValue.GetValue(Content, "description.sound_effects");
    }

    private static JsonElement GetEntityQueryableGeometry(String Content) {
        return GetJsonValue.GetValue(Content, "description.queryable_geometry");
    }

    private static JsonElement GetEntityParticleEffects(String Content) {
        return GetJsonValue.GetValue(Content, "description.particle_effects");
    }

    public static void ReadEntityFile(Path File) throws IOException {
        String Content = Files.readString(File);

        JsonElement JsonIdentifier = GetEntityIdentifier(Content);
        JsonElement JsonMaterials = GetEntityMaterials(Content);
        JsonElement JsonTextures = GetEntityTextures(Content);
        JsonElement JsonGeometries = GetEntityGeometry(Content);
        JsonElement JsonAnimations = GetEntityAnimations(Content);
        JsonElement JsonScripts = GetEntityScripts(Content);
        JsonElement JsonSpawnEgg = GetEntitySpawnEgg(Content);
        JsonElement JsonRenderControllers = GetEntityRenderControllers(Content);
        JsonElement JsonSoundEffects = GetEntitySoundEffects(Content);
        JsonElement JsonQueryableGeometry = GetEntityQueryableGeometry(Content);
        JsonElement JsonParticleEffects = GetEntityParticleEffects(Content);

        EntityTextureResolver.ResolveEntityTexture(JsonTextures);
    }
}