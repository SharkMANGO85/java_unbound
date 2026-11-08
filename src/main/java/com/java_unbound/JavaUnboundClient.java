package com.java_unbound;

import com.java_unbound.attachables.AttachableRegistry;
import com.java_unbound.loader.definitions.attachable.AttachableDefinition;
import com.java_unbound.loader.definitions.entity.resolvers.EntityAnimationResolver;
import com.java_unbound.geckolib.rendering.EntityRendererRegistration;
import com.java_unbound.loader.converters.GeckoLibConverter;
import com.java_unbound.loader.converters.MaterialToJsonConverter;
import com.java_unbound.loader.converters.TgaConverter;
import com.java_unbound.loader.converters.TransparencyConverter;
import com.java_unbound.loader.definitions.entity.EntityDefinition;
import com.java_unbound.loader.definitions.entity.resolvers.EntityGeometryResolver;
import com.java_unbound.loader.definitions.entity.resolvers.EntityRenderControllerResolver;
import com.java_unbound.geckolib.utils.GeckoLibLogFilter;
import com.java_unbound.loader.definitions.global.materials.resolvers.MaterialResolver;
import com.java_unbound.loader.files.AttachableGeometryLoader;
import com.java_unbound.loader.resourcepack.Folder;
import com.java_unbound.loader.resourcepack.LanguageLoader;
import com.java_unbound.loader.resourcepack.ResourceMapper;
import com.java_unbound.loader.resourcepack.ResourceMappings;
import com.java_unbound.mod_menu.config.ConfigManager;
import com.java_unbound.molang.types.MolangAttachableParser;
import net.fabricmc.api.ClientModInitializer;
import com.java_unbound.entities.InitializeEntity;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

import java.io.IOException;

public class JavaUnboundClient implements ClientModInitializer {
    public static String Debug = "";

    private static void DebugError(String Type, Runnable Function) {
        String LoadingMessage = "------------------Loading " + Type + "------------------";
        String FinishedMessage = "------------------Finished " + Type + "------------------\n";

        Debug += LoadingMessage + "\n\n";

        JavaUnbound.LOGGER.error("{}", LoadingMessage);

        try {
            Function.run();
        } catch (Exception Exception) {
            String ErrorMessage = "Error while loading " + Type + ": " + Exception;
            Debug += ErrorMessage + "\n\n";

            JavaUnbound.LOGGER.error(ErrorMessage);
            Exception.printStackTrace();
        }

        Debug += "\n\n" + FinishedMessage + "\n\n";

        JavaUnbound.LOGGER.error("{}\n\n", FinishedMessage);
    }

    public static void Loading() {
        EntityGeometryResolver.ClearCache();
        EntityRenderControllerResolver.ClearCache();
        EntityAnimationResolver.ClearCache();

        DebugError("Material to Json Converter", MaterialToJsonConverter.Convert());
        DebugError("Material Resolver", MaterialResolver::Load);
        DebugError("TGA Converter", TgaConverter.ConvertAll(Folder.GetConfigFolder()));
        DebugError("Transparency Converter", TransparencyConverter.LoadTextureTransparencies(Folder.GetConfigFolder().resolve("subpacks").resolve(JavaUnbound.SUB_PACK).resolve("textures").toFile()));
        DebugError("Languages", LanguageLoader::PreloadLanguage);
        DebugError("Entity Definitions", EntityDefinition::PreloadEntityFiles);
        DebugError("Attachable Definitions", AttachableDefinition::PreloadAttachableFiles);
        DebugError("Attachable Geometry Files", AttachableGeometryLoader::LoadAttachableGeometryFiles);
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

        //AttachableRegistry.RegisterItems();
    }
}