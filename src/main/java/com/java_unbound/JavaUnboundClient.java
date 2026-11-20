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

import java.io.IOException;

public class JavaUnboundClient implements ClientModInitializer {
    @FunctionalInterface
    private interface Loader {
        void Load() throws IOException;
    }

    private static void DebugPrint(String Text, Loader Function) {
        JavaUnbound.LOGGER.error("------------------------------------------------");
        JavaUnbound.LOGGER.error("Loading " + Text);
        JavaUnbound.LOGGER.error("");

        try {
            Function.Load();

        } catch (IOException Exception) {
            throw new RuntimeException(Exception);
        }

        JavaUnbound.LOGGER.error("");
        JavaUnbound.LOGGER.error("Finished Loading " + Text);
        JavaUnbound.LOGGER.error("------------------------------------------------");
    }

    @Override
    public void onInitializeClient() {
        JavaUnbound.LOGGER.error("------------------{}------------------", JavaUnbound.MOD_NAME);

        GeckoLibLogFilter.Initialize();
        EntityRendererRegistration.RegisterEntityRenderer();

        JavaUnbound.LOGGER.error("------------------{}------------------", JavaUnbound.MOD_NAME);
    }
}