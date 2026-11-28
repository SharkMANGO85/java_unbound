package com.java_unbound.entrypoints;

import com.java_unbound.fallback.FallbackLoader;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;

public class JavaUnboundPreLaunch implements PreLaunchEntrypoint {
    @Override
    public void onPreLaunch() {
        FallbackLoader.PreloadFallbackAsync();
    }
}