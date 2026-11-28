package com.java_unbound.shared.resourcepack;

import com.java_unbound.JavaUnbound;
import com.java_unbound.shared.definitions.manifest.ManifestDefinition;
import com.java_unbound.shared.definitions.resource_pack.ResourcePackDefinition;
import com.java_unbound.shared.loader.resourcepacks.PackLoader;
import com.java_unbound.shared.loader.resourcepacks.ResourcePackLoader;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.resources.ResourceManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResourcePackOrder {
    private static final Map<String, Integer> GlobalResourcepacks = new HashMap<>();
    private static final Map<String, Integer> BedrockResourcepacks = new HashMap<>();
    private static String AppliedSelection = null;

    public record PackEntry(String Id, int GlobalPosition, int BedrockPosition, ResourcePackDefinition Definition) {
        public boolean IsBedrock() {
            return Definition != null;
        }
    }

    public static synchronized void Update() {
        Apply(GetResourcepacks());
    }

    public static synchronized void Update(PackRepository Repository) {
        Apply(GetResourcepacks(Repository));
    }

    private static void Apply(List<PackEntry> Entries) {
        GlobalResourcepacks.clear();
        BedrockResourcepacks.clear();

        StringBuilder BedrockIds = new StringBuilder();

        for (PackEntry Entry : Entries) {
            GlobalResourcepacks.put(Entry.Id(), Entry.GlobalPosition());

            if (Entry.IsBedrock()) {
                BedrockResourcepacks.put(Entry.Id(), Entry.BedrockPosition());
                BedrockIds.append(Entry.Id()).append('(').append(Entry.BedrockPosition()).append(") ");
            }
        }

        JavaUnbound.LOGGER.info("[ResourcePackOrder] Updated order: {} packs, {} bedrock: {}", Entries.size(), BedrockResourcepacks.size(), BedrockIds.toString().trim());
    }

    public static synchronized boolean ConsumeSelectionChange(PackRepository Repository) {
        String Current = String.join(";", Repository.getSelectedIds());

        if (Current.equals(AppliedSelection)) {
            JavaUnbound.LOGGER.info("[ResourcePackOrder] Selection unchanged");
            return false;
        }

        JavaUnbound.LOGGER.info("[ResourcePackOrder] Selection changed from [{}] to [{}]", AppliedSelection, Current);
        AppliedSelection = Current;
        return true;
    }

    public static synchronized int GetCachedPosition(String UUID) {
        return UUID == null ? -1 : GlobalResourcepacks.getOrDefault(UUID, -1);
    }

    public static synchronized int GetCachedPosition(ManifestDefinition Manifest) {
        return Manifest == null || Manifest.Header == null ? -1 : GetCachedPosition(Manifest.Header.UUID);
    }

    public static synchronized int GetCachedPositionAmongBedrockResourcepacks(String UUID) {
        return UUID == null ? -1 : BedrockResourcepacks.getOrDefault(UUID, -1);
    }

    public static synchronized int GetCachedPositionAmongBedrockResourcepacks(ManifestDefinition Manifest) {
        return Manifest == null || Manifest.Header == null ? -1 : GetCachedPositionAmongBedrockResourcepacks(Manifest.Header.UUID);
    }

    public static int GetPosition(ManifestDefinition Manifest) {
        if (Manifest == null || Manifest.Header == null) {
            return -1;
        }

        return GetPosition(Manifest.Header.UUID);
    }

    public static int GetPosition(String UUID) {
        if (UUID == null) {
            return -1;
        }

        for (PackEntry Entry : GetResourcepacks()) {
            if (Entry.Id().equals(UUID)) {
                return Entry.GlobalPosition();
            }
        }

        return -1;
    }

    public static int GetPositionAmongBedrockResourcepacks(ManifestDefinition Manifest) {
        if (Manifest == null || Manifest.Header == null) {
            return -1;
        }

        return GetPositionAmongBedrockResourcepacks(Manifest.Header.UUID);
    }

    public static int GetPositionAmongBedrockResourcepacks(String UUID) {
        if (UUID == null) {
            return -1;
        }

        for (PackEntry Entry : GetResourcepacks()) {
            if (Entry.IsBedrock() && Entry.Id().equals(UUID)) {
                return Entry.BedrockPosition();
            }
        }

        return -1;
    }

    public static int GetPosition(PackRepository Repository, ManifestDefinition Manifest) {
        if (Manifest == null || Manifest.Header == null) {
            return -1;
        }

        return GetPosition(Repository, Manifest.Header.UUID);
    }

    public static int GetPosition(PackRepository Repository, String UUID) {
        if (UUID == null) {
            return -1;
        }

        List<Pack> Selected = GetSelectedTopFirst(Repository);

        for (int Index = 0; Index < Selected.size(); Index++) {
            if (Selected.get(Index).getId().equals(UUID)) {
                return Index + 1;
            }
        }

        return -1;
    }

    public static int GetPositionAmongBedrockResourcepacks(PackRepository Repository, ManifestDefinition Manifest) {
        if (Manifest == null || Manifest.Header == null) {
            return -1;
        }

        return GetPositionAmongBedrockResourcepacks(Repository, Manifest.Header.UUID);
    }

    public static int GetPositionAmongBedrockResourcepacks(PackRepository Repository, String UUID) {
        if (UUID == null || ResourcePackLoader.GetPack(UUID) == null) {
            return -1;
        }

        List<ResourcePackDefinition> Loaded = GetLoadedOrder(Repository);

        for (int Index = 0; Index < Loaded.size(); Index++) {
            if (PackLoader.GetPackId(Loaded.get(Index)).equals(UUID)) {
                return Index + 1;
            }
        }

        return -1;
    }

    public static List<PackEntry> GetResourcepacks() {
        Minecraft Client = Minecraft.getInstance();
        PackRepository Repository = Client == null ? null : Client.getResourcePackRepository();

        return GetResourcepacks(Repository);
    }

    public static List<PackEntry> GetResourcepacks(PackRepository Repository) {
        List<PackEntry> Result = new ArrayList<>();
        List<String> Ids = new ArrayList<>();

        if (Repository != null) {
            for (Pack SelectedPack : Repository.getSelectedPacks()) {
                Ids.add(SelectedPack.getId());
            }
        } else {
            Minecraft Client = Minecraft.getInstance();
            ResourceManager Manager = Client == null ? null : Client.getResourceManager();

            if (Manager == null) {
                JavaUnbound.LOGGER.error("[ResourcePackOrder] No pack repository and no resource manager available");
                return Result;
            }

            Manager.listPacks().forEach(ManagerPack -> Ids.add(ManagerPack.packId()));
        }

        Collections.reverse(Ids);

        int OwnPosition = 1;

        for (int Index = 0; Index < Ids.size(); Index++) {
            String Id = Ids.get(Index);

            if (FabricLoader.getInstance().isModLoaded(Id)) {
                continue;
            }

            ResourcePackDefinition Definition = FindDefinition(Id);

            int GlobalPosition = Index + 1;
            int BedrockPosition = Definition != null ? OwnPosition++ : -1;

            Result.add(new PackEntry(Id, GlobalPosition, BedrockPosition, Definition));
        }

        return Result;
    }

    public static List<PackEntry> GetMyResourcepacks() {
        List<PackEntry> Result = new ArrayList<>();

        for (PackEntry Entry : GetResourcepacks()) {
            if (Entry.IsBedrock()) Result.add(Entry);
        }

        return Result;
    }

    public static List<ResourcePackDefinition> GetLoadedOrder(PackRepository Repository) {
        List<ResourcePackDefinition> Result = new ArrayList<>();

        for (Pack Pack : GetSelectedTopFirst(Repository)) {
            ResourcePackDefinition Definition = FindDefinition(Pack.getId());

            if (Definition != null) {
                Result.add(Definition);
            }
        }

        return Result;
    }

    private static List<Pack> GetSelectedTopFirst(PackRepository Repository) {
        List<Pack> Selected = new ArrayList<>(Repository.getSelectedPacks());

        Collections.reverse(Selected);

        return Selected;
    }

    private static ResourcePackDefinition FindDefinition(String Id) {
        if (Id == null) {
            return null;
        }

        ResourcePackDefinition Definition = ResourcePackLoader.GetPack(Id);

        if (Definition == null && Id.startsWith("file/")) {
            Definition = ResourcePackLoader.GetPack(Id.substring(5));
        }

        return Definition;
    }
}