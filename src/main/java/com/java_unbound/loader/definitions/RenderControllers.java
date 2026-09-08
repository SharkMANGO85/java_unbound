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

    private static final Path Subpack0Folder = Folder.GetResourceFolder().resolve("subpacks").resolve("SP0");
    private static final Path Subpack1Folder = Folder.GetResourceFolder().resolve("subpacks").resolve("SP1");
    private static final Path Subpack2Folder = Folder.GetResourceFolder().resolve("subpacks").resolve("SP2");

    private static final Path BaseRenderControllersFolder = Folder.GetResourceFolder().resolve("render_controllers");
    private static final Path Subpack0RenderControllersFolder = Subpack0Folder.resolve("render_controllers");
    private static final Path Subpack1RenderControllersFolder = Subpack1Folder.resolve("render_controllers");
    private static final Path Subpack2RenderControllersFolder = Subpack2Folder.resolve("render_controllers");

    public static void LoadRenderControllers() {
        RenderControllerFiles.clear();

        AddRenderControllers(BaseRenderControllersFolder);
        AddRenderControllers(Subpack0RenderControllersFolder);
        AddRenderControllers(Subpack1RenderControllersFolder);
        AddRenderControllers(Subpack2RenderControllersFolder);
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

                for (Map.Entry<String, JsonElement> Entry : RenderControllersElement.getAsJsonObject().entrySet()) {
                    if (!Entry.getValue().isJsonObject()) {continue;}

                    JsonObject RenderController = Entry.getValue().getAsJsonObject();
                    RenderController.addProperty("identifier", Entry.getKey());
                    RenderControllerFiles.put(Entry.getKey(), RenderController.toString());
                }
            } catch (IOException | RuntimeException Exception) {
                JavaUnbound.LOGGER.error("Failed to read render controller file: {}", File, Exception);
            }
        }
    }
}