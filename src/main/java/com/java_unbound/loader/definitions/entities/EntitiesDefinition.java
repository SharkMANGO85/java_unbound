package com.java_unbound.loader.definitions.entities;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.java_unbound.JavaUnbound;
import com.java_unbound.global.HashMaps;
import com.java_unbound.entities.loader.EntityRegistry;
import com.java_unbound.loader.resourcepack.Folder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class EntitiesDefinition {
    private static final Path BaseEntityFolder = Folder.GetConfigFolder().resolve("entity");
    private static final Path SubpackEntityFolder0 = Folder.GetConfigFolder().resolve("subpacks").resolve("SP0").resolve("entity");
    private static final Path SubpackEntityFolder1 = Folder.GetConfigFolder().resolve("subpacks").resolve("SP1").resolve("entity");
    private static final Path SubpackEntityFolder2 = Folder.GetConfigFolder().resolve("subpacks").resolve("SP2").resolve("entity");

    private static final Path[] EntityPriorityOrder = {SubpackEntityFolder2, BaseEntityFolder, SubpackEntityFolder1, SubpackEntityFolder0};

    private static String AddJsonToFiles(Path EntityPath) {
        try {
            String JsonContent = Files.readString(EntityPath);
            JsonElement Root = JsonParser.parseString(JsonContent);

            if (!Root.isJsonObject()) {
                JavaUnbound.LOGGER.error("Entity file root is not a JSON object: " + EntityPath);
                return null;
            }

            JsonObject RootObject = Root.getAsJsonObject();
            JsonElement ClientEntityElement = RootObject.get("minecraft:client_entity");

            if (ClientEntityElement == null || !ClientEntityElement.isJsonObject()) {
                JavaUnbound.LOGGER.error("Entity file has no minecraft:client_entity object: " + EntityPath);
                return null;
            }

            JsonObject EntityObject = ClientEntityElement.getAsJsonObject();
            JsonElement DescriptionElement = EntityObject.get("description");

            if (DescriptionElement == null || !DescriptionElement.isJsonObject()) {return null;}

            JsonObject Description = DescriptionElement.getAsJsonObject();
            JsonElement IdentifierElement = Description.get("identifier");

            if (IdentifierElement == null || !IdentifierElement.isJsonPrimitive()) {return null;}

            String Identifier = IdentifierElement.getAsString();

            if (Identifier.equals("oreville_ans:disabled")) {return null;}

            HashMaps.EntityFiles.putIfAbsent(Identifier, EntityObject);

            return Identifier;

        } catch (IOException | RuntimeException Exception) {
            JavaUnbound.LOGGER.error("Failed to load entity file: " + EntityPath, Exception);
            return null;
        }
    }

    private static String GetIdentifierFromPath(Path EntityPath) {
        try {
            String JsonContent = Files.readString(EntityPath);
            JsonElement Root = JsonParser.parseString(JsonContent);

            if (!Root.isJsonObject()) {return null;}

            JsonObject RootObject = Root.getAsJsonObject();

            if (!RootObject.has("minecraft:client_entity") || !RootObject.get("minecraft:client_entity").isJsonObject()) {return null;}

            JsonObject EntityObject = RootObject.getAsJsonObject("minecraft:client_entity");

            if (!EntityObject.has("description") || !EntityObject.get("description").isJsonObject()) {return null;}

            JsonObject Description = EntityObject.getAsJsonObject("description");

            if (!Description.has("identifier") || !Description.get("identifier").isJsonPrimitive()) {return null;}

            return Description.get("identifier").getAsString();

        } catch (IOException | RuntimeException Exception) {
            JavaUnbound.LOGGER.error("Failed to get entity identifier from: " + EntityPath);
            Exception.printStackTrace();
            return null;
        }
    }

    public static void LoadEntities() throws IOException {
        HashMaps.EntityFiles.clear();

        for (Path EntityFolder : EntityPriorityOrder) {
            if (!Files.isDirectory(EntityFolder)) {continue;}

            try (Stream<Path> Paths = Files.walk(EntityFolder)) {
                Paths.filter(Files::isRegularFile).filter(Path -> Path.getFileName().toString().toLowerCase().endsWith(".json")).forEach(EntityPath -> {AddJsonToFiles(EntityPath);
                            String Identifier = GetIdentifierFromPath(EntityPath);

                            if (Identifier == null || Identifier.equals("oreville_ans:disabled")) {return;}

                            EntityRegistry EntityRegister = new EntityRegistry();
                            EntityRegister.LoadEntity(Identifier);
                        });
            }
        }
    }

    public static JsonElement GetEntityJsonByIdentifier(String Identifier) {
        return HashMaps.EntityFiles.get(Identifier);
    }
}