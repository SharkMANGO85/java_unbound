package com.java_unbound.shared.definitions.resource_pack;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.java_unbound.JavaUnbound;
import com.java_unbound.shared.definitions.language.LanguageResourceDefinition;
import com.java_unbound.shared.definitions.manifest.ManifestDefinition;
import com.java_unbound.shared.loader.resourcepacks.LanguageLoader;
import com.java_unbound.shared.loader.resourcepacks.ManifestLoader;
import com.java_unbound.shared.resourcepack.ResourceMapper;
import com.java_unbound.shared.resourcepack.VirtualResources;

import java.nio.file.Path;

public class ResourcePackDefinition {
    public Path Path;
    public String FolderName;
    public ManifestDefinition Manifest;
    public LanguageResourceDefinition LanguageResource;

    public final ResourceMapper Mapper = new ResourceMapper();
    public final VirtualResources Virtual = new VirtualResources(this);

    public void Clear() {
        Mapper.Clear();
        Virtual.Clear();
    }

    public String GetTranslatedValue(ResourcePackDefinition ResourcePackDefinition, JsonObject Json, String Key) {
        String Value = ManifestLoader.GetString(Json, Key);

        return Value == null ? JavaUnbound.UNKNOWN : GetTranslatedText(ResourcePackDefinition, Value);
    }

    public String GetTranslatedText(ResourcePackDefinition ResourcePackDefinition, String Text) {
        if (Text == null) return JavaUnbound.UNKNOWN;

        String TranslatedText = LanguageLoader.FindText(ResourcePackDefinition.LanguageResource, Text);

        return TranslatedText != null ? TranslatedText : Text;
    }
}