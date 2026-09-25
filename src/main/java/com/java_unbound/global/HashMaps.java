package com.java_unbound.global;

import com.google.gson.JsonElement;
import com.java_unbound.loader.entities.EntityRegistry;

import java.util.HashMap;
import java.util.Map;

public final class HashMaps {
    private HashMaps() {
    }

    public static final Map<String, JsonElement> AnimationControllerFiles = new HashMap<>();
    public static final Map<String, JsonElement> EntityFiles = new HashMap<>();
    public static final Map<String, JsonElement> GeoemtryFiles = new HashMap<>();
    public static final Map<String, JsonElement> RenderControllerFiles = new HashMap<>();
    public static final Map<String, String> TextureFiles = new HashMap<>();
    public static final Map<String, JsonElement> AnimationFiles = new HashMap<>();

    public static final Map<String, EntityRegistry> EntityRegistries = new HashMap<>();
}