package com.java_unbound.loader.resourcepack;

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
    private PackLoader() {
    }

    public static String GetPackName(Path ResourcePack) {
        return ReadLang(ResourcePack, "pack.name", "Java Unbound");
    }

    public static String GetDescription(Path ResourcePack) {
        return ReadLang(ResourcePack, "pack.description", "Java Unbound");
    }

    public static String ReadLang(Path Root, String Key, String Fallback) {
        Path LangFile = Root.resolve("texts").resolve("en_US.lang");

        if (!Files.isRegularFile(LangFile)) {
            return Fallback;
        }

        try (BufferedReader Reader = Files.newBufferedReader(LangFile, StandardCharsets.UTF_8)) {
            String Line;

            while ((Line = Reader.readLine()) != null) {
                Line = Line.trim();

                if (Line.isEmpty() || Line.startsWith("#")) {
                    continue;
                }

                if (!Line.startsWith(Key + "=")) {
                    continue;
                }

                String Value = Line.substring((Key + "=").length()).trim();

                int Hash = Value.indexOf('#');

                if (Hash >= 0) {
                    Value = Value.substring(0, Hash).trim();
                }

                return Value;
            }
        } catch (IOException E) {
            return Fallback;
        }

        return Fallback;
    }

    public static Pack Create(Path ResourcePack) {
        String Name = GetPackName(ResourcePack);
        String Description = GetDescription(ResourcePack);

        PackLocationInfo Location = new PackLocationInfo("java_unbound", Component.literal(Name), PackSource.BUILT_IN, Optional.empty());

        Pack.ResourcesSupplier Supplier = new Pack.ResourcesSupplier() {
            @Override
            public PackResources openPrimary(PackLocationInfo Info) {
                return new FolderResources(ResourcePack, Info
                );
            }

            @Override
            public PackResources openFull(PackLocationInfo Info, Pack.Metadata Metadata) {
                return new FolderResources(ResourcePack, Info);
            }
        };

        Pack.Metadata Metadata = new Pack.Metadata(Component.literal(Description), PackCompatibility.COMPATIBLE, FeatureFlagSet.of(), List.of());

        PackSelectionConfig Selection = new PackSelectionConfig(true, Pack.Position.TOP, true);

        return new Pack(Location, Supplier, Metadata, Selection);
    }
}