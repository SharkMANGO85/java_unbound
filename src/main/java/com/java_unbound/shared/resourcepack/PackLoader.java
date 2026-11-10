package com.java_unbound.shared.resourcepack;

import com.java_unbound.JavaUnbound;
import com.java_unbound.shared.loaders.language.LanguageLoader;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackCompatibility;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.world.flag.FeatureFlagSet;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public final class PackLoader {
    private PackLoader() {}

    public static String GetPackName(Path ResourcePack) {
        return LanguageLoader.GetText("pack.name");
    }

    public static String GetDescription(Path ResourcePack) {
        return LanguageLoader.GetText("pack.description");
    }

    public static Pack Create(Path ResourcePack) {
        String Name = GetPackName(ResourcePack);
        String Description = GetDescription(ResourcePack);
        PackLocationInfo Location = new PackLocationInfo(JavaUnbound.PACK_ID, Component.literal(Name), PackSource.BUILT_IN, Optional.empty());

        Pack.ResourcesSupplier Supplier = new Pack.ResourcesSupplier() {
            @Override
            public PackResources openPrimary(PackLocationInfo Info) {
                return new FolderResources(ResourcePack, Info);
            }

            @Override
            public PackResources openFull(PackLocationInfo Info, Pack.Metadata Metadata) {
                return new FolderResources(ResourcePack, Info);
            }
        };

        Pack.Metadata Metadata = new Pack.Metadata(Component.literal(Description), PackCompatibility.COMPATIBLE, FeatureFlagSet.of(), List.of());
        PackSelectionConfig Selection = new PackSelectionConfig(true, Pack.Position.TOP, false);

        return new Pack(Location, Supplier, Metadata, Selection);
    }
}