package com.java_unbound;

import com.java_unbound.entities.geckolib.EntityRendererRegistration;
import com.java_unbound.loader.converters.GeckoLibConverter;
import com.java_unbound.loader.converters.TgaConverter;
import com.java_unbound.loader.converters.TransparencyConverter;
import com.java_unbound.loader.debug.EntityDebugGenerator;
import com.java_unbound.loader.definitions.entity.EntityDefinition;
import com.java_unbound.loader.definitions.entity.EntityDefinitionDebugger;
import com.java_unbound.loader.definitions.entity.reolvers.EntityGeometryResolver;
import com.java_unbound.loader.definitions.entity.reolvers.EntityRenderControllerResolver;
import com.java_unbound.loader.gecko_lib.GeckoLibLogFilter;
import com.java_unbound.loader.resourcepack.Folder;
import com.java_unbound.loader.resourcepack.LanguageLoader;
import com.java_unbound.loader.resourcepack.ResourceMapper;
import com.java_unbound.loader.resourcepack.ResourceMappings;
import com.java_unbound.molang.types.MolangEntityParser;
import com.java_unbound.utils.variables.HashMaps;
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

        DebugError("TGA Converter", TgaConverter.ConvertAll(Folder.GetConfigFolder()));
        DebugError("Transparency Converter", TransparencyConverter.LoadTextureTransparencies(Folder.GetConfigFolder().resolve("subpacks").resolve(JavaUnbound.SUB_PACK).resolve("textures").toFile()));
        DebugError("Languages", LanguageLoader::PreloadLanguage);
        DebugError("Entity Definitions", EntityDefinition::PreloadEntityFiles);

        DebugError("GeckoLib Converter", () -> {
            try {
                GeckoLibConverter.LoadResources();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        /*DebugError("Entity Debugging", () -> {
            HashMaps.Entities.forEach((Identifier, Definition) -> {
                EntityDefinitionDebugger.DebugEntity(Identifier);
            });
        });*/

        JavaUnbound.LOGGER.error("\n\n\n\n\n{}" + "\n\n\n\n\n", Debug);
    }

    @Override
    public void onInitializeClient() {
        ClientEntityEvents.ENTITY_LOAD.register((Entity, World) -> {
            InitializeEntity.InitializeEntity(Entity);
        });

        ResourceMapper.Clear();
        TgaConverter.ConvertAll(Folder.GetConfigFolder());
        ResourceMappings.Register();

        Loading();

        GeckoLibLogFilter.Initialize();
        EntityRendererRegistration.RegisterEntityRenderer();

        ClientEntityEvents.ENTITY_LOAD.register((Entity, World) -> {
            InitializeEntity.InitializeEntity(Entity);
        });

        CommandRegistrationCallback.EVENT.register((Dispatcher, RegistryAccess, Environment) -> {
            Dispatcher.register(Commands.literal("entitydebug")
                    .then(Commands.literal("generate").executes(Context ->
                            EntityDebugGenerator.Generate(Context.getSource().getLevel(), Context.getSource().getPosition().x, Context.getSource().getPosition().z, Message -> Context.getSource().sendSuccess(() -> Message, false))
                    )));
        });
    }
}