package com.java_unbound;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JavaUnbound implements ModInitializer {
    public static final String MOD_ID = "java_unbound";
    public static final String MOD_NAME = "Java Unbound";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final String PACK_ID = "java_unbound_addon";

    public static String UNKNOWN = "N/V";

    ///Change Later to mod_menu setting
    public static String SUB_PACK = "SP2";
    public static String LANGUAGE = "en_US";

    public static float SPLASH_SCALE = 2.0f;
    public static float SPLASH_POSITION_X = 160f;
    public static float SPLASH_POSITION_Y = 100f;
    public static int SPLASH_MAXIMUM_CHARACTERS = 24;
    public static boolean SPLASH_VISIBLE = false;

    public static float TITLE_SCALE = 2.0f;
    public static boolean TITLE_VISIBLE = true;

    @Override
    public void onInitialize() {

    }
}