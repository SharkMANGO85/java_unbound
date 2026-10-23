package com.java_unbound;

import com.java_unbound.loader.converters.TgaConverter;
import com.java_unbound.loader.converters.TransparencyConverter;
import com.java_unbound.loader.definitions.entities.EntitiesDefinition;
import com.java_unbound.loader.definitions.global_use.*;
import com.java_unbound.loader.gecko_lib.EntityRendererRegistration;
import com.java_unbound.loader.gecko_lib.GeckoLibConverter;
import com.java_unbound.loader.resourcepack.Folder;
import com.java_unbound.loader.resourcepack.LanguageLoader;
import com.java_unbound.loader.resourcepack.ResourceMapper;
import com.java_unbound.loader.resourcepack.ResourceMappings;
import com.java_unbound.loader.ui.textures.loaders.TextureLoader;
import com.java_unbound.mod_menu.config.ConfigManager;
import net.fabricmc.api.ClientModInitializer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class JavaUnboundClient implements ClientModInitializer {
    public static void Loading() {
        //Folder.ClearResourcePack();

        TransparencyConverter.LoadTextureTransparencies(Folder.GetConfigFolder().resolve("subpacks").resolve("SP2").resolve("textures").toFile());

        // Loading Definitions
        JavaUnbound.LOGGER.error("-------------------------Definitions-------------------------");
        JavaUnbound.LOGGER.error("");
        JavaUnbound.LOGGER.error("---------------------------Textures---------------------------");
        JavaUnbound.LOGGER.error("Loading Textures");

        try {
            TexturesDefinition.LoadTextures();
        } catch (IOException Execption) {
            throw new RuntimeException(Execption);
        }

        JavaUnbound.LOGGER.error("Finished Loading Textures");
        JavaUnbound.LOGGER.error("--------------------------------------------------------------");
        JavaUnbound.LOGGER.error("");

        JavaUnbound.LOGGER.error("--------------------------Geometries--------------------------");
        JavaUnbound.LOGGER.error("Loading Geometries");

        try {
            GeometriesDefinition.LoadGeometries();
        } catch (IOException Execption) {
            throw new RuntimeException(Execption);
        }

        JavaUnbound.LOGGER.error("Finished Loading Geometries");
        JavaUnbound.LOGGER.error("--------------------------------------------------------------");
        JavaUnbound.LOGGER.error("");

        JavaUnbound.LOGGER.error("---------------------Animation Controllers---------------------");
        JavaUnbound.LOGGER.error("Loading Animation Controllers");

        try {
            AnimationControllersDefinition.LoadAnimationControllers();
        } catch (IOException Execption) {
            throw new RuntimeException(Execption);
        }

        JavaUnbound.LOGGER.error("Finished Loading Animation Controllers");
        JavaUnbound.LOGGER.error("--------------------------------------------------------------");
        JavaUnbound.LOGGER.error("");

        JavaUnbound.LOGGER.error("---------------------Animations---------------------");
        JavaUnbound.LOGGER.error("Loading Animations");

        try {
            AnimationsDefinition.LoadAnimations();
        } catch (IOException Execption) {
            throw new RuntimeException(Execption);
        }

        JavaUnbound.LOGGER.error("Finished Loading Animations");
        JavaUnbound.LOGGER.error("--------------------------------------------------------------");
        JavaUnbound.LOGGER.error("");

        JavaUnbound.LOGGER.error("---------------------Render Controllers---------------------");
        JavaUnbound.LOGGER.error("Loading Render Controllers");

        try {
            RenderControllersDefinition.LoadRenderControllers();
        } catch (IOException Execption) {
            throw new RuntimeException(Execption);
        }

        JavaUnbound.LOGGER.error("Finished Loading Render Controllers");
        JavaUnbound.LOGGER.error("--------------------------------------------------------------");
        JavaUnbound.LOGGER.error("");

        JavaUnbound.LOGGER.error("---------------------Entities---------------------");
        JavaUnbound.LOGGER.error("Loading Entities");

        try {
            EntitiesDefinition.LoadEntities();
        } catch (IOException Execption) {
            throw new RuntimeException(Execption);
        }

        JavaUnbound.LOGGER.error("Finished Loading Entities");
        JavaUnbound.LOGGER.error("--------------------------------------------------------------");
        JavaUnbound.LOGGER.error("");

        JavaUnbound.LOGGER.error("---------------------Language---------------------");
        JavaUnbound.LOGGER.error("Loading Languages");

        LanguageLoader.PreloadLanguage();

        JavaUnbound.LOGGER.error("Finished Loading Languages");
        JavaUnbound.LOGGER.error("--------------------------------------------------------------");
        JavaUnbound.LOGGER.error("");

        LanguageLoader.PreloadLanguage();

        JavaUnbound.LOGGER.error("Finished Loading Languages");
        JavaUnbound.LOGGER.error("--------------------------------------------------------------");
        JavaUnbound.LOGGER.error("");

        JavaUnbound.LOGGER.error("---------------------Config Manager---------------------");
        JavaUnbound.LOGGER.error("Loading Config Manager");

        ConfigManager.Load();

        JavaUnbound.LOGGER.error("Finished Loading Config Manager");
        JavaUnbound.LOGGER.error("--------------------------------------------------------------");
        JavaUnbound.LOGGER.error("");

        JavaUnbound.LOGGER.error("---------------------Gecko Lib Loader---------------------");
        JavaUnbound.LOGGER.error("Loading Gecko Lib");

        try {
            GeckoLibConverter.LoadResources();
        } catch (IOException Execption) {
            throw new RuntimeException(Execption);
        }

        JavaUnbound.LOGGER.error("Finished Gecko Lib");
        JavaUnbound.LOGGER.error("--------------------------------------------------------------");
        JavaUnbound.LOGGER.error("");

        JavaUnbound.LOGGER.error("---------------------Item Icon Loader---------------------");
        JavaUnbound.LOGGER.error("Loading Item Icons");

        TextureLoader.LoadItemIcons();

        JavaUnbound.LOGGER.error("Finished Item Icons");
        JavaUnbound.LOGGER.error("--------------------------------------------------------------");
        JavaUnbound.LOGGER.error("");
    }

   @Override
    public void onInitializeClient() {
        ResourceMapper.Clear();
        TgaConverter.ConvertAll(Folder.GetConfigFolder());
        ResourceMappings.Register();
        Loading();
        EntityRendererRegistration.RegisterEntityRenderer();
    }
}