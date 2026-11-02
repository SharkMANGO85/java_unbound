package com.java_unbound.utils.variables;

import com.google.gson.JsonElement;
import com.java_unbound.loader.definitions.entity.EntityDefinition;

import java.util.HashMap;
import java.util.Map;

//saving the hole data here
public final class HashMaps {
    public HashMaps() {}

    //Definitions
    public static final Map<String, EntityDefinition> Entities = new HashMap<>();

    //Language
    public static final Map<String, String> LanguageFiles = new HashMap<>();
}