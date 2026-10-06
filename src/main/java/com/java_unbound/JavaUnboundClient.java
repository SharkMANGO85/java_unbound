package com.java_unbound;

import com.java_unbound.converter.TgaConverter;
import com.java_unbound.loader.definitions.entities.EntitiesDefinition;
import com.java_unbound.loader.definitions.global_use.*;
import com.java_unbound.loader.resourcepack.Folder;
import com.java_unbound.loader.resourcepack.ResourceMapper;
import com.java_unbound.loader.resourcepack.ResourceMappings;
import com.java_unbound.molang.testing.MolangParserTest;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

import java.io.IOException;

public class JavaUnboundClient implements ClientModInitializer {
    private boolean MolangTested = false;

    @Override
    public void onInitializeClient() {
        ResourceMapper.Clear();
        TgaConverter.ConvertAll(Folder.GetConfigFolder());
        ResourceMappings.Register();

        // Loading Definitions
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
            AnimationControllersDefinition.LoadAnimationControllers();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        JavaUnbound.LOGGER.error("Finished Loading Animation Controllers");
        JavaUnbound.LOGGER.error("--------------------------------------------------------------");
        JavaUnbound.LOGGER.error("");

        JavaUnbound.LOGGER.error("---------------------Animations---------------------");
        JavaUnbound.LOGGER.error("Loading Animations");

        try {
            AnimationsDefinition.LoadAnimations();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        JavaUnbound.LOGGER.error("Finished Loading Animations");
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
        JavaUnbound.LOGGER.error("");

        JavaUnbound.LOGGER.error("---------------------Entities---------------------");
        JavaUnbound.LOGGER.error("Loading Entities");

        try {
            EntitiesDefinition.LoadEntities();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        JavaUnbound.LOGGER.error("Finished Loading Entities");
        JavaUnbound.LOGGER.error("--------------------------------------------------------------");

        ClientTickEvents.END_CLIENT_TICK.register(Client -> {
            if (MolangTested) {return;}
            if (Client.level == null) {return;}

            JavaUnbound.LOGGER.info("[MoLang Test] Level found, starting tests.");

            MolangTested = true;
            MolangParserTest.test(Client.level);
        });
    }
}