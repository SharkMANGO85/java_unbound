package com.java_unbound.loader.textures.entity;

import com.java_unbound.loader.resourcepack.Folder;
import com.java_unbound.loader.resourcepack.FolderResources;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.stream.Stream;

public class EntityTextureLoader {
    private EntityTextureLoader() {

    }

    Path BaseEntityFolder = Folder.GetResourceFolder().resolve("entity");
    Path SubpackEntityFolder0 = Folder.GetResourceFolder().resolve("")
}
