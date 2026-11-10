package com.java_unbound.shared.definitions.language;

import java.util.HashMap;

public class LanguageResourceDefinition {
    private final HashMap<String, LanguageDefinition> LanguageResources = new HashMap<>();

    public void SetLanguage(String Language, LanguageDefinition Definition) {
        LanguageResources.put(Language, Definition);
    }

    public LanguageDefinition GetLanguage(String Language) {
        return LanguageResources.get(Language);
    }

    public HashMap<String, LanguageDefinition> GetLanguages() {
        return LanguageResources;
    }
}