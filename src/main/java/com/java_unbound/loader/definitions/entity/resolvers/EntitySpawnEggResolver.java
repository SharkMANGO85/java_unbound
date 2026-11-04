package com.java_unbound.loader.definitions.entity.resolvers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.java_unbound.JavaUnbound;
import com.java_unbound.loader.definitions.entity.types.spawn_egg.EntitySpawnEggDefinition;

import java.nio.file.Files;
import java.nio.file.Path;

public class EntitySpawnEggResolver {
    private static JsonObject ReadJson(Path JsonFile) {
        try {
            JsonElement Element = JsonParser.parseString(Files.readString(JsonFile));

            if (!Element.isJsonObject()) {
                JavaUnbound.LOGGER.debug("Skipping JSON file (root is not an object): {}", JsonFile);
                return null;
            }

            return Element.getAsJsonObject();
        } catch (Exception Exception) {
            JavaUnbound.LOGGER.error("Failed to read JSON file: {}", JsonFile, Exception);
            return null;
        }
    }

    private static JsonObject GetSpawnEggObject(Path EntityFile) {
        JsonObject Root = ReadJson(EntityFile);
        if (Root == null) {return null;}

        JsonObject ClientEntity = ReadObject(Root, "minecraft:client_entity");

        if (ClientEntity == null) {
            JavaUnbound.LOGGER.error("No minecraft:client_entity found: {}", EntityFile);
            return null;
        }

        JsonObject Description = ReadObject(ClientEntity, "description");

        if (Description == null) {
            JavaUnbound.LOGGER.error("No entity description found: {}", EntityFile);
            return null;
        }

        JsonObject SpawnEgg = ReadObject(Description, "spawn_egg");

        if (SpawnEgg == null) {
            JavaUnbound.LOGGER.debug("No spawn egg found: {}", EntityFile);
        }

        return SpawnEgg;
    }

    private static EntitySpawnEggDefinition ParseSpawnEgg(JsonObject Json) {
        EntitySpawnEggDefinition Definition = new EntitySpawnEggDefinition();

        Definition.Texture = ReadString(Json.get("texture"));
        Definition.TextureIndex = ReadIndex(Json.get("texture_index"));
        Definition.BaseColor = NormalizeColor(ReadString(Json.get("base_color")));
        Definition.OverlayColor = NormalizeColor(ReadString(Json.get("overlay_color")));

        if (!HasTexture(Definition) && !HasColors(Definition)) {return null;}

        return Definition;
    }

    private static JsonObject ReadObject(JsonObject Parent, String Key) {
        JsonElement Element = Parent.get(Key);
        return Element != null && Element.isJsonObject() ? Element.getAsJsonObject() : null;
    }

    private static String ReadString(JsonElement Element) {
        if (Element == null || !Element.isJsonPrimitive()) {return null;}

        String Value = Element.getAsString().trim();
        return Value.isEmpty() ? null : Value;
    }

    private static String ReadIndex(JsonElement Element) {
        if (Element == null || !Element.isJsonPrimitive()) {return null;}

        if (Element.getAsJsonPrimitive().isNumber()) {
            return String.valueOf(Element.getAsNumber().intValue());
        }

        return ReadString(Element);
    }

    private static boolean HasTexture(EntitySpawnEggDefinition Definition) {
        return Definition != null && Definition.Texture != null && !Definition.Texture.isEmpty();
    }

    private static boolean HasColors(EntitySpawnEggDefinition Definition) {
        return Definition != null && Definition.BaseColor != null && Definition.OverlayColor != null;
    }

    private static int GetTextureIndex(EntitySpawnEggDefinition Definition) {
        if (Definition == null || Definition.TextureIndex == null) {return 0;}

        try {
            return Integer.parseInt(Definition.TextureIndex);
        } catch (NumberFormatException Exception) {
            return 0;
        }
    }

    private static int ParseColor(String Color) {
        String Normalized = NormalizeColor(Color);
        if (Normalized == null) {return -1;}

        return Integer.parseInt(Normalized.substring(1), 16);
    }

    private static String NormalizeColor(String Color) {
        if (Color == null || Color.isEmpty()) {return null;}

        String Hex = Color.startsWith("#") ? Color.substring(1) : Color;
        if (!Hex.matches("[0-9a-fA-F]{6}")) {return null;}

        return "#" + Hex.toUpperCase();
    }

    public static EntitySpawnEggDefinition GetEntitySpawnEgg(Path JsonFile) {
        if (JsonFile == null) {
            JavaUnbound.LOGGER.error("Cannot load entity spawn egg: JSON file path is null.");
            return null;
        }

        if (!Files.isRegularFile(JsonFile)) {
            JavaUnbound.LOGGER.error("Entity JSON file does not exist: {}", JsonFile);
            return null;
        }

        JavaUnbound.LOGGER.debug("Loading entity spawn egg from: {}", JsonFile);

        JsonObject SpawnEggObject = GetSpawnEggObject(JsonFile);

        if (SpawnEggObject == null) {return null;}

        try {
            EntitySpawnEggDefinition Definition = ParseSpawnEgg(SpawnEggObject);

            if (Definition == null) {
                JavaUnbound.LOGGER.debug("Spawn egg has no texture or colors: {}", JsonFile);
                return null;
            }

            JavaUnbound.LOGGER.debug("Loaded entity spawn egg: {} | Texture: {} | TextureIndex: {} | BaseColor: {} | OverlayColor: {}", JsonFile, Definition.Texture, Definition.TextureIndex, Definition.BaseColor, Definition.OverlayColor);

            return Definition;
        } catch (Exception Exception) {
            JavaUnbound.LOGGER.error("Failed to parse entity spawn egg from: {}", JsonFile, Exception);
            return null;
        }
    }
}