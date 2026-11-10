package com.java_unbound.shared.definitions.language;

import java.util.HashMap;
import java.util.Map;

public class LanguageDefinition {
    private final Map<String, String> Texts = new HashMap<>();

    public void SetText(String Key, String Value) {
        Texts.put(Key, Value);
    }

    public String GetText(String Key) {
        return Texts.get(Key);
    }

    public Map<String, String> GetTexts() {
        return Texts;
    }
}