package com.java_unbound;

import com.java_unbound.loader.converter.TgaConverter;
import com.java_unbound.loader.definitions.AnimationControllerDefinition;
import com.java_unbound.loader.definitions.GeometriesDefinition;
import com.java_unbound.loader.definitions.RenderControllersDefinition;
import com.java_unbound.loader.definitions.TexturesDefinition;
import com.java_unbound.loader.resourcepack.Folder;

import com.java_unbound.loader.resourcepack.ResourceMapper;
import com.java_unbound.loader.resourcepack.ResourceMappings;
import net.fabricmc.api.ClientModInitializer;

import java.io.IOException;

public class JavaUnboundClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ResourceMapper.Clear();
        TgaConverter.ConvertAll(Folder.GetConfigFolder());
        ResourceMappings.Register();

        //Loading Definitions
        JavaUnbound.LOGGER.error("-------------------------Definitions-------------------------");
        JavaUnbound.LOGGER.error("");
        JavaUnbound.LOGGER.error("---------------------------Textures---------------------------");
        JavaUnbound.LOGGER.error("Loading Textures");

        try {
            TexturesDefinition.LoadTextures();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        JavaUnbound.LOGGER.error("Finished Loading Textures");
        JavaUnbound.LOGGER.error("--------------------------------------------------------------");
        JavaUnbound.LOGGER.error("");
        JavaUnbound.LOGGER.error("--------------------------Geometries--------------------------");
        JavaUnbound.LOGGER.error("Loading Geometries");

        try {
            GeometriesDefinition.LoadGeometries();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        JavaUnbound.LOGGER.error("Finished Loading Geometries");
        JavaUnbound.LOGGER.error("--------------------------------------------------------------");
        JavaUnbound.LOGGER.error("");
        JavaUnbound.LOGGER.error("---------------------Animation Controllers---------------------");
        JavaUnbound.LOGGER.error("Loading Animation Controllers");

        try {
            AnimationControllerDefinition.LoadAnimationControllers();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        JavaUnbound.LOGGER.error("Finished Loading Animation Controllers");
        JavaUnbound.LOGGER.error("--------------------------------------------------------------");
        JavaUnbound.LOGGER.error("");
        JavaUnbound.LOGGER.error("---------------------Render Controllers---------------------");
        JavaUnbound.LOGGER.error("Loading Render Controllers");

        try {
            RenderControllersDefinition.LoadRenderControllers();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        JavaUnbound.LOGGER.error("Finished Loading Render Controllers");
        JavaUnbound.LOGGER.error("--------------------------------------------------------------");
    }
}