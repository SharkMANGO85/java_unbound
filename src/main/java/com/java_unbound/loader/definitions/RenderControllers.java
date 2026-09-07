package com.java_unbound.loader.definitions;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.java_unbound.JavaUnbound;
import com.java_unbound.loader.resourcepack.Folder;
import com.java_unbound.utils.functions.GetJsonFilesInPath;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RenderControllers {
    public static final Map<String, String> RenderControllerFiles = new HashMap<>();

    private static final Path SubpackFolder = Folder.GetResourceFolder().resolve("subpacks").resolve(JavaUnbound.SUBPACK);

    private static final Path BaseRenderControllersFolder = Folder.GetResourceFolder().resolve("render_controllers");
    private static final Path SubpackRenderControllersFolder = SubpackFolder.resolve("render_controllers");

    public static void Load() {
        RenderControllerFiles.clear();

        AddRenderControllers(BaseRenderControllersFolder);
        AddRenderControllers(SubpackRenderControllersFolder);
    }

    public static JsonElement GetRenderController(String Identifier) {
        String RenderController = RenderControllerFiles.get(Identifier);
        return RenderController == null ? null : JsonParser.parseString(RenderController);
    }

    public static List<JsonElement> GetRenderControllers(JsonElement RenderControllerElement) {
        List<JsonElement> ResolvedRenderControllers = new ArrayList<>();

        if (RenderControllerElement == null) {
            return ResolvedRenderControllers;
        }

        if (RenderControllerElement.isJsonPrimitive()) {
            JsonElement RenderController = GetRenderController(RenderControllerElement.getAsString());

            if (RenderController != null) {
                ResolvedRenderControllers.add(RenderController);
            }

            return ResolvedRenderControllers;
        }

        if (RenderControllerElement.isJsonArray()) {
            for (JsonElement Element : RenderControllerElement.getAsJsonArray()) {
                if (!Element.isJsonPrimitive()) {
                    continue;
                }

                JsonElement RenderController = GetRenderController(Element.getAsString());

                if (RenderController != null) {
                    ResolvedRenderControllers.add(RenderController);
                }
            }
        }

        return ResolvedRenderControllers;
    }

    private static void AddRenderControllers(Path RenderControllerFolder) {
        if (!Files.isDirectory(RenderControllerFolder)) {
            return;
        }

        List<Path> FilesInFolder = GetJsonFilesInPath.GetDescendant(RenderControllerFolder);

        for (Path File : FilesInFolder) {
            try {
                String Content = Files.readString(File);
                JsonElement Root = JsonParser.parseString(Content);

                if (!Root.isJsonObject()) {
                    continue;
                }

                JsonElement RenderControllersElement = Root.getAsJsonObject().get("render_controllers");

                if (RenderControllersElement == null || !RenderControllersElement.isJsonObject()) {
                    continue;
                }

                JsonObject RenderControllersObject = RenderControllersElement.getAsJsonObject();

                for (Map.Entry<String, JsonElement> Entry : RenderControllersObject.entrySet()) {
                    String Identifier = Entry.getKey();
                    JsonElement RenderController = Entry.getValue();

                    if (!RenderController.isJsonObject()) {
                        continue;
                    }

                    RenderControllerFiles.put(Identifier, RenderController.toString());
                }
            } catch (IOException | RuntimeException Exception) {
                JavaUnbound.LOGGER.error("Failed to read render controller file: {}", File, Exception);
            }
        }
    }
}