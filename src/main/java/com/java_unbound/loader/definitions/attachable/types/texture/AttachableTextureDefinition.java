package com.java_unbound.loader.definitions.attachable.types.texture;

import com.java_unbound.loader.resourcepack.Folder;

import java.nio.file.Path;

public class AttachableTextureDefinition {
    public String Identifier;
    public Path AbsolutePath;
    public String ResourcePath;

    public void SetTextureIdentifier() {
        if (AbsolutePath == null || ResourcePath == null) {return;}

        Folder.SetIdentifier(ResourcePath, AbsolutePath.toString());
    }
}
