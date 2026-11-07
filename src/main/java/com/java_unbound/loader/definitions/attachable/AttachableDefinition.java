package com.java_unbound.loader.definitions.attachable;

import com.java_unbound.JavaUnbound;
import com.java_unbound.loader.definitions.attachable.resolvers.*;
import com.java_unbound.loader.definitions.attachable.types.geomtry.*;
import com.java_unbound.loader.definitions.attachable.types.texture.*;
import com.java_unbound.loader.definitions.entity.EntityDefinition;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;

public class AttachableDefinition {
    public String Identifier;

    public HashMap<String, AttachableTextureDefinition> Textures;
    public HashMap<String, AttachableGeometryDefinition> Geomtries;
    public List<String> RenderControllers;
    public String Scripts;

    public HashMap<String, String> Materials;
    public HashMap<String, String> Animations;

    public static AttachableDefinition ResolveAttachable(Path JsonFile) {
        AttachableDefinition Definition = new AttachableDefinition();
        String AttachableIdentifer = AttachableStringResolver.GetAttachableString("identifier", JsonFile);

        try {
            Definition.Identifier = AttachableIdentifer;


            return Definition;
        } catch (Exception Exception) {
            JavaUnbound.LOGGER.error("Failed to resolve attachable definition: {}", JsonFile, Exception);
            return null;
        }
    }
}
