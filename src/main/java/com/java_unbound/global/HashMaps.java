package com.java_unbound.global;

import com.google.gson.JsonElement;
import com.java_unbound.entities.loaders.EntityRegistry;
import com.java_unbound.entities.render_controller.EntityRenderControllerRegistry;

import java.util.HashMap;
import java.util.Map;

//saving the hole data here
public final class HashMaps {
    public HashMaps() {}

    //Definitions
    public static final Map<String, JsonElement> AnimationControllerFiles = new HashMap<>();
    public static final Map<String, JsonElement> EntityFiles = new HashMap<>();
    public static final Map<String, JsonElement> GeoemtryFiles = new HashMap<>();
    public static final Map<String, JsonElement> RenderControllerFiles = new HashMap<>();
    public static final Map<String, String> TextureFiles = new HashMap<>();
    public static final Map<String, JsonElement> AnimationFiles = new HashMap<>();

    //Entities
    public static final Map<String, EntityRegistry> EntityRegistries = new HashMap<>();
    public static Map<String, EntityRenderControllerRegistry> EntityRenderControllers = new HashMap<>();
}