
package com.java_unbound.loader.resourcepack;

import com.java_unbound.utils.variables.HashMaps;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class LanguageLoader {
    private LanguageLoader() {}

    public static void PreloadLanguage() {
        Path LanguageFilePath = Folder.GetConfigFolder().resolve("texts/en_US.lang");
        if (!Files.isRegularFile(LanguageFilePath)) {return;}

        HashMaps.LanguageFiles.clear();

        try {
            for (String Line : Files.readAllLines(LanguageFilePath)) {
                int SeparatorIndex = Line.indexOf('=');
                if (SeparatorIndex == -1) {continue;}

                String Key = Line.substring(0, SeparatorIndex).trim();
                String Value = Line.substring(SeparatorIndex + 1).trim();

                int CommentIndex = Value.indexOf('\t');

                if (CommentIndex != -1) {
                    Value = Value.substring(0, CommentIndex).trim();
                }

                if (!Key.isEmpty()) {
                    HashMaps.LanguageFiles.put(Key, Value);
                }
            }
        } catch (IOException Exception) {
            Exception.printStackTrace();
        }
    }

    public static String GetText(String Identifier) {
        return HashMaps.LanguageFiles.getOrDefault(Identifier, Identifier);
    }
}
