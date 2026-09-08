package com.java_unbound.loader.attachables;

import java.util.HashMap;
import java.util.Map;

public final class AttachableRegistry {
    private static final Map<String, AttachableDefinition> Attachables = new HashMap<>();

    private AttachableRegistry() {
    }

    public static void Register(AttachableDefinition Attachable) {
        if (Attachable == null || Attachable.Identifier == null) {
            return;
        }

        Attachables.put(Attachable.Identifier, Attachable);
    }

    public static AttachableDefinition Get(String Identifier) {
        return Attachables.get(Identifier);
    }

    public static void Clear() {
        Attachables.clear();
    }
}