package com.java_unbound;

import com.java_unbound.entities.geckolib.EntityRendererRegistration;
import com.java_unbound.loader.converters.GeckoLibConverter;
import com.java_unbound.loader.converters.MaterialToJsonConverter;
import com.java_unbound.loader.converters.TgaConverter;
import com.java_unbound.loader.converters.TransparencyConverter;
import com.java_unbound.loader.definitions.entity.EntityDefinition;
import com.java_unbound.loader.definitions.entity.resolvers.EntityAnimationControllerResolver;
import com.java_unbound.loader.definitions.entity.resolvers.EntityGeometryResolver;
import com.java_unbound.loader.definitions.entity.resolvers.EntityRenderControllerResolver;
import com.java_unbound.loader.gecko_lib.GeckoLibLogFilter;
import com.java_unbound.loader.resourcepack.Folder;
import com.java_unbound.loader.resourcepack.LanguageLoader;
import com.java_unbound.loader.resourcepack.ResourceMapper;
import com.java_unbound.loader.resourcepack.ResourceMappings;
import com.java_unbound.mod_menu.config.ConfigManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.Commands;
import com.java_unbound.entities.InitializeEntity;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;

import java.io.IOException;

public class JavaUnboundClient implements ClientModInitializer {
    public static String Debug = "";

    private static void DebugError(String Type, Runnable Function) {
        StringBuilder DebugBuilder = new StringBuilder();

        DebugBuilder.append("---------------------").append(Type).append("---------------------\n");
        DebugBuilder.append("Loading ").append(Type).append("\n");

        long StartTime = System.nanoTime();

        try {
            Function.run();
        } catch (Exception Exception) {
            DebugBuilder.append("Error while loading ").append(Type).append(": ").append(Exception).append("\n");
        }

        long LoadingTime = (System.nanoTime() - StartTime) / 1_000_000;

        DebugBuilder.append("Finished Loading ").append(Type).append(" in ").append(LoadingTime).append(" ms\n");
        DebugBuilder.append("--------------------------------------------------------------\n\n");

        Debug += DebugBuilder;
    }

    public static void Loading() {
        EntityGeometryResolver.ClearCache();
        EntityRenderControllerResolver.ClearCache();
        EntityAnimationControllerResolver.ClearCache();
        EntityGeometryResolver.ClearCache();

        DebugError("Material to Json Converter", MaterialToJsonConverter.Convert());
        DebugError("TGA Converter", TgaConverter.ConvertAll(Folder.GetConfigFolder()));
        DebugError("Transparency Converter", TransparencyConverter.LoadTextureTransparencies(Folder.GetConfigFolder().resolve("subpacks").resolve(JavaUnbound.SUB_PACK).resolve("textures").toFile()));
        DebugError("Languages", LanguageLoader::PreloadLanguage);
        DebugError("Entity Definitions", EntityDefinition::PreloadEntityFiles);
        DebugError("Config Manager", ConfigManager::Load);

        DebugError("GeckoLib Converter", () -> {
            try {
                GeckoLibConverter.LoadResources();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        //DebugError("Texture Loading", TextureLoader::LoadItemIcons);

        /*DebugError("Entity Debugging", () -> {
            HashMaps.Entities.forEach((Identifier, Definition) -> {
                EntityDefinitionDebugger.DebugEntity(Identifier);
            });
        });*/

        JavaUnbound.LOGGER.error("\n\n\n\n\n{}" + "\n\n\n\n\n", Debug);
    }

    @Override
    public void onInitializeClient() {
        ResourceMapper.Clear();
        TgaConverter.ConvertAll(Folder.GetConfigFolder());
        ResourceMappings.Register();

        Loading();

        GeckoLibLogFilter.Initialize();
        EntityRendererRegistration.RegisterEntityRenderer();

        ClientEntityEvents.ENTITY_LOAD.register((Entity, World) -> {
            InitializeEntity.InitializeEntity(Entity);
        });

    }
}