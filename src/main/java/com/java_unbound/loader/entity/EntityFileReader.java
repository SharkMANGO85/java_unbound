package com.java_unbound.loader.entity;

import com.java_unbound.JavaUnbound;
import com.java_unbound.loader.resourcepack.Folder;
import com.java_unbound.utils.functions.GetJsonFilesInPath;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class EntityFileReader {
    private EntityFileReader() {

    }

    private static final Path ENTITY_FOLDER = Folder.GetResourceFolder().resolve("entity");
    private static final List<Path> ENTITY_JSONS = GetJsonFilesInPath.Get(ENTITY_FOLDER);

    public static void Read() {
        for (Path File : ENTITY_JSONS) {
            try {
                ReadEntityFile(File);
            } catch (IOException Exception) {
                Exception.printStackTrace();
            }
        }
    }

    public static void ReadEntityFile(Path File) throws IOException {
        String Content = Files.readString(File);


    }
}