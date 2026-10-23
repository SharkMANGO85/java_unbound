package com.java_unbound;

import com.java_unbound.entities.actions.EntityLoadHandler;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JavaUnbound implements ModInitializer {
    public static final String MOD_ID = "java_unbound";
    public static final String MOD_NAME = "Java Unbound";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        EntityLoadHandler.Register();
    }
}