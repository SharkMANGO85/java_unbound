package com.java_unbound.loader.definitions.entity.types.texture;

import com.java_unbound.loader.resourcepack.Folder;

import java.nio.file.Path;

public class EntityTextureDefinition {
    public String Identifier;
    public Path AbsolutePath;
    public String ResourcePath;

    public void SetTextureIdentifier() {
        if (AbsolutePath == null || ResourcePath == null) {return;}

        Folder.SetIdentifier(ResourcePath, AbsolutePath.toString());
    }
}
