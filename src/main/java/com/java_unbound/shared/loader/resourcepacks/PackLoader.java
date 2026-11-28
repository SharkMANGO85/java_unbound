package com.java_unbound.shared.loader.resourcepacks;

import com.java_unbound.JavaUnbound;
import com.java_unbound.shared.definitions.resource_pack.ResourcePackDefinition;
import com.java_unbound.shared.resourcepack.FolderResources;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackCompatibility;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.world.flag.FeatureFlagSet;

import java.util.List;
import java.util.Optional;

public class PackLoader {
    public static String GetPackId(ResourcePackDefinition Definition) {
        String UUID = Definition.Manifest.Header.UUID;

        return UUID == null || UUID.isBlank() ? "java_unbound_" + Definition.FolderName : UUID;
    }

    public static String GetPackName(ResourcePackDefinition Definition) {
        String Name = Definition.Manifest.Header.Name;

        return Name == null || Name.equals(JavaUnbound.UNKNOWN) ? Definition.FolderName : Name;
    }

    public static String GetDescription(ResourcePackDefinition Definition) {
        String Description = Definition.Manifest.Header.Description;

        if (Description == null) {
            Description = JavaUnbound.UNKNOWN;
        }

        return "§6■ " + GetPackName(Definition) + "\n§e■ " + Description + "\n";
    }

    public static Pack Create(ResourcePackDefinition Definition) {
        PackLocationInfo Location = new PackLocationInfo(GetPackId(Definition), Component.literal(GetPackName(Definition)), PackSource.BUILT_IN, Optional.empty());

        Pack.ResourcesSupplier Supplier = new Pack.ResourcesSupplier() {
            @Override
            public PackResources openPrimary(PackLocationInfo Info) {
                return new FolderResources(Definition, Info);
            }

            @Override
            public PackResources openFull(PackLocationInfo Info, Pack.Metadata Metadata) {
                return new FolderResources(Definition, Info);
            }
        };

        Pack.Metadata Metadata = new Pack.Metadata(Component.literal(GetDescription(Definition)), PackCompatibility.COMPATIBLE, FeatureFlagSet.of(), List.of());
        PackSelectionConfig Selection = new PackSelectionConfig(false, Pack.Position.TOP, false);

        return new Pack(Location, Supplier, Metadata, Selection);
    }
}