package com.java_unbound.shared.loader.resourcepacks;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.java_unbound.JavaUnbound;
import com.java_unbound.shared.definitions.language.LanguageDefinition;
import com.java_unbound.shared.definitions.language.LanguageResourceDefinition;
import com.java_unbound.shared.definitions.manifest.ManifestDefinition;
import com.java_unbound.shared.definitions.resource_pack.ResourcePackDefinition;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class LanguageLoader {
    public static LanguageResourceDefinition GetLanguageResourceDefinition(Path TextsFolderPath, ResourcePackDefinition PackDefinition) {
        LanguageResourceDefinition Definition = new LanguageResourceDefinition();

        try {
            LoadLanguages(TextsFolderPath, Definition);
        } catch (IOException Exception) {
            Exception.printStackTrace();
        }

        return Definition;
    }

    public static String GetText(LanguageResourceDefinition Resource, String Key) {
        String Text = FindText(Resource, Key);
        return Text != null ? Text : Key;
    }

    public static String FindText(LanguageResourceDefinition Resource, String Key) {
        if (Resource == null || Key == null) return null;

        String Text = Lookup(Resource, JavaUnbound.LANGUAGE, Key);
        if (Text == null) Text = Lookup(Resource, JavaUnbound.FALLBACK_LANGUAGE, Key);

        return Text;
    }

    private static String Lookup(LanguageResourceDefinition Resource, String Language, String Key) {
        if (Language == null) return null;

        LanguageDefinition Definition = Resource.GetLanguage(Language);

        return Definition == null ? null : Definition.GetText(Key);
    }

    private static Path GetLanguagesFilePath(Path TextsFolderPath) {
        Path LanguagesFilePath = TextsFolderPath.resolve("languages.json");

        if (!Files.exists(LanguagesFilePath)) {
            JavaUnbound.LOGGER.error("Languages.json file not found, Path: " + LanguagesFilePath);
            return null;
        }

        return LanguagesFilePath;
    }

    private static Path GetLanguageFile(Path TextsFolderPath, String Language) {
        Path LanguageFile = TextsFolderPath.resolve(Language + ".lang");

        if (!Files.exists(LanguageFile)) {
            JavaUnbound.LOGGER.error("Language file doesn't exist for {} at Path: {}", Language + ".lang", LanguageFile);
            return null;
        }

        return LanguageFile;
    }

    private static void LoadLanguage(Path LanguageFile, LanguageResourceDefinition ResourceDefinition) throws IOException {
        String Language = LanguageFile.getFileName().toString().replace(".lang", "");
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

        ResourceDefinition.SetLanguage(Language, Definition);
    }

    private static void LoadLanguages(Path TextsFolderPath, LanguageResourceDefinition Definition) throws IOException {
        Path LanguageResource = GetLanguagesFilePath(TextsFolderPath);

        if (LanguageResource == null) {
            JavaUnbound.LOGGER.error("Languages.json file not found, Path: " + LanguageResource);
            return;
        }

        JsonArray Languages = JsonParser.parseString(Files.readString(LanguageResource)).getAsJsonArray();

        for (JsonElement LanguageElement : Languages) {
            String Language = LanguageElement.getAsString();
            Path LanguageFile = GetLanguageFile(TextsFolderPath, Language);

            if (LanguageFile == null) {
                JavaUnbound.LOGGER.error("Language file not found, Path: " + LanguageFile);
                continue;
            }

            LoadLanguage(LanguageFile, Definition);
        }
    }
}
