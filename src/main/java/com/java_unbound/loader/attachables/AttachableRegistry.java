package com.java_unbound.loader.attachables;

import java.util.HashMap;
import java.util.Map;

public final class AttachableRegistry {
    private static final Map<String, AttachableDefinition> Loaded = new HashMap<>();

    private AttachableRegistry() {}

    public static void Register(AttachableDefinition Attachable) {
        if (Attachable == null || Attachable.Identifier == null) {return;}

        Loaded.put(Attachable.Identifier, Attachable);
    }

    public static AttachableDefinition Get(String Identifier) {
        return Loaded.get(Identifier);
    }

    public static Map<String, AttachableDefinition> GetAll() {
        return Loaded;
    }

    public static void Clear() {
        Loaded.clear();
    }
}