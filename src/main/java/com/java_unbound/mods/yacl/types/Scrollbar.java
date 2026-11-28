package com.java_unbound.mods.yacl.types;

import com.java_unbound.JavaUnbound;
import com.java_unbound.mods.yacl.ResourcePackSettingsManager;
import com.java_unbound.mods.yacl.controllers.SnappingSliderController;
import com.java_unbound.mods.yacl.resource_pack.ResourcePackScreen;
import com.java_unbound.shared.definitions.manifest.ManifestSubpackDefinition;
import com.java_unbound.shared.definitions.resource_pack.ResourcePackDefinition;
import com.java_unbound.shared.resources.ResourceUpdater;
import com.java_unbound.shared.resources.ResourceUtilities;
import com.java_unbound.shared.resources.textures.TextureUpdater;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class Scrollbar {
    private static final List<String> SubpackNames = new ArrayList<>();

    public static void AddonCreateScrollbarOption(ResourcePackDefinition PackDefintion) {

    }

    public static void SubpackCreateScrollbarOption(ResourcePackDefinition PackDefinition) {
        SubpackNames.clear();

        if (PackDefinition.Manifest.Subpacks == null || PackDefinition.Manifest.Subpacks.isEmpty()) return;

        for (ManifestSubpackDefinition SubpackDefinition : PackDefinition.Manifest.Subpacks) {
            String Name = SubpackDefinition.Name != null ? SubpackDefinition.Name : SubpackDefinition.FolderName;
            SubpackNames.add(Name);
        }

        String PackId = ResourcePackScreen.GetCurrentPackId();
        var PackSettings = ResourcePackSettingsManager.GetPackSettings(PackId);
        double Step = SubpackNames.size() > 1 ? 1.0 / (SubpackNames.size() - 1) : 1.0;
        double InitialValue = PackSettings.has("subpack") ? PackSettings.get("subpack").getAsDouble() : 0.0;

        InitialValue = Math.clamp(InitialValue, 0.0, 1.0);

        double[] Current = {InitialValue};

        TextLabel.CreateTextLabel("ResourcePack", "Subpack Selection §8The Subpack you want to play with.");

        Option<Double> ScrollbarOption = Option.<Double>createBuilder()
                .name(Component.literal(JavaUnbound.UNKNOWN))
                .description(OptionDescription.of(Component.empty()))
                .binding(InitialValue, () -> Current[0], Value -> {
                    Current[0] = Value;

                    ResourcePackSettingsManager.SetPackSetting(PackId, "subpack", Value);
                    ResourceUpdater.UpdateResources();
                    TextureUpdater.LoadTexturesAndReload();
                })
                .customController(Opt -> new SnappingSliderController(Opt, 0.0, 1.0, Step))
                .build();

        ResourcePackScreen.GetCategory("ResourcePack").option(ScrollbarOption);

        TextLabel.CreateTextLabel("ResourcePack", "");
    }


    public static String GetSubpackName(double Value) {
        if (SubpackNames.isEmpty()) {return "Subpack";}

        int Index = (int) Math.round(Value * (SubpackNames.size() - 1));
        Index = Math.clamp(Index, 0, SubpackNames.size() - 1);

        return SubpackNames.get(Index).replaceAll("§[0-9a-fk-or]", "");
    }
}