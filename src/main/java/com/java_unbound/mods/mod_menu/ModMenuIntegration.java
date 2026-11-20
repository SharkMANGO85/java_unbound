package com.java_unbound.mods.mod_menu;

import com.java_unbound.JavaUnbound;
import com.java_unbound.mods.yacl.ModScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        JavaUnbound.LOGGER.info("Mod Menu asked for the config screen factory");
        return parent -> {
            JavaUnbound.LOGGER.info("Config screen opened");
            return ModScreen.ToggleScreen(parent);
        };
    }
}