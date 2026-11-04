package com.java_unbound.loader.definitions.attachable;

import com.java_unbound.loader.definitions.attachable.types.geomtry.*;
import com.java_unbound.loader.definitions.attachable.types.render_controller.*;
import com.java_unbound.loader.definitions.attachable.types.texture.*;
import com.java_unbound.loader.definitions.attachable.types.scripts.*;

import java.util.HashMap;
import java.util.List;

public class AttachableDefinition {
    public String Identifier;

    public HashMap<String, AttachableTextureDefinition> Textures;
    public HashMap<String, AttachableGeometryDefinition> Geomtries;
    public List<AttachableRenderControllerDefinition> RenderControllers;
    public AttachableScriptsDefinition Scripts;

    public HashMap<String, String> Materials;
    public HashMap<String, String> Animations;
}
