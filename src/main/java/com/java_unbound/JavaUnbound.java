package com.java_unbound;

import com.java_unbound.loader.entities.on_spawn.EntityLoadHandler;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JavaUnbound implements ModInitializer {
    public static final String MOD_ID = "java_unbound";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final String SUBPACK = "SP2";

    @Override
    public void onInitialize() {
        EntityLoadHandler.Register();
    }
}