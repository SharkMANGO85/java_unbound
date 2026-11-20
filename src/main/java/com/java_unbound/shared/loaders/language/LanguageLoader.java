package com.java_unbound.shared.loaders.language;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.java_unbound.JavaUnbound;
import com.java_unbound.shared.definitions.language.LanguageDefinition;
import com.java_unbound.shared.definitions.language.LanguageResourceDefinition;
import com.java_unbound.shared.resourcepack.Folder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public class LanguageLoader {
    private static final LanguageResourceDefinition Resource = new LanguageResourceDefinition();

    private static Path GetLanguageFolder() {

        if (!Files.exists(LanguageFolder)) {
            return null;
        }

        return LanguageFolder;
    }

    private static Path GetLanguageResource() {

        if (!Files.exists(LanguageResource)) {
            return null;
        }

        return LanguageResource;
    }

    private static Path GetLanguageFile(String Language) {

        if (!Files.exists(LanguageFile)) {
            return null;
        }

        return LanguageFile;
    }

    private static void LoadLanguage(String Language) throws IOException {
        Path LanguageFile = GetLanguageFile(Language);

        if (LanguageFile == null) {
            return;
        }

        LanguageDefinition Definition = new LanguageDefinition();

        for (String Line : Files.readAllLines(LanguageFile)) {
            if (Line.isBlank() || Line.startsWith("#")) {continue;}

            int Separator = Line.indexOf('=');
            if (Separator == -1) {continue;}

            String Key = Line.substring(0, Separator).trim();
            String Value = Line.substring(Separator + 1).trim();

            Value = Value.replaceAll("\\s*#\\\\?$", "");

            Definition.SetText(Key, Value);
        }

        Resource.SetLanguage(Language, Definition);
    }

    public static void LoadLanguages() throws IOException {
        Path LanguageResource = GetLanguageResource();
        assert LanguageResource != null;

        if (LanguageResource == null) {
            JavaUnbound.LOGGER.error("Language resource folder doesn't exist");
            return;
        }

        JsonArray Languages = JsonParser.parseString(Files.readString(LanguageResource)).getAsJsonArray();

        for (JsonElement Language : Languages) {
            LoadLanguage(Language.getAsString());
        }
    }

    public static String GetText(String Key) {
        LanguageDefinition Language = Resource.GetLanguage(JavaUnbound.LANGUAGE);
        if (Language == null) {return Key;}

        String Text = Language.GetText(Key);

    }
}
