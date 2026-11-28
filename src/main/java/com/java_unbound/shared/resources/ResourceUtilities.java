package com.java_unbound.shared.resources;

import com.java_unbound.JavaUnbound;
import com.java_unbound.fallback.FallbackUtilities;
import com.java_unbound.mods.yacl.ResourcePackSettingsUtilities;
import com.java_unbound.shared.converters.TgaConverter;
import com.java_unbound.shared.definitions.resource_pack.ResourcePackDefinition;
import com.java_unbound.shared.loader.resourcepacks.ResourcePackLoader;
import com.java_unbound.shared.resourcepack.PackFolders;
import com.java_unbound.shared.resourcepack.ResourcePackOrder;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.Identifier;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ResourceUtilities {
    public record TextureResourceRecord(boolean Bedrock, String ResourcePath) {}

    private static final Map<String, Identifier> FallbackTextures = new ConcurrentHashMap<>();
    private static final Map<String, Identifier> ResolvedTextures = new ConcurrentHashMap<>();
    private static final Set<String> MissingTextures = ConcurrentHashMap.newKeySet();

    public static void OnResourcesReloaded() {
        JavaUnbound.LOGGER.info("[ResourceUtilities] OnResourcesReloaded");
        ResourcePackOrder.Update();
        ResourceUpdater.UpdateResources();
    }

    public static Identifier GetTextureIdentifier(String JavaResourcePath, String BedrockResourcePath) {
        String CacheKey = JavaResourcePath + "|" + BedrockResourcePath;

        Identifier Cached = ResolvedTextures.get(CacheKey);

        if (Cached != null) {
            return Cached;
        }

        if (MissingTextures.contains(CacheKey)) {
            return null;
        }

        if (!GraphicsReady()) {
            JavaUnbound.LOGGER.info("[ResourceUtilities] Graphics device not ready, skipping lookup for {}", CacheKey);
            return null;
        }

        TextureResourceRecord Record = ResolveTextureResource(JavaResourcePath, BedrockResourcePath);

        if (Record == null) {
            MissingTextures.add(CacheKey);
            JavaUnbound.LOGGER.info("[ResourceUtilities] No texture found for {}", CacheKey);
            return null;
        }

        Identifier Result;

        if (Record.Bedrock()) {
            Result = RegisterFileTexture(Path.of(Record.ResourcePath()));
        } else {
            Result = Identifier.parse(Record.ResourcePath());
        }

        if (Result == null) {
            JavaUnbound.LOGGER.error("[ResourceUtilities] Failed to register texture for {} ({})", CacheKey, Record);
        } else if (IsFallback(Result)) {
            JavaUnbound.LOGGER.info("[ResourceUtilities] Using fallback texture {} for {}", Result, CacheKey);
        } else {
            JavaUnbound.LOGGER.debug("[ResourceUtilities] Resolved {} -> {}", CacheKey, Result);
            ResolvedTextures.put(CacheKey, Result);
        }

        return Result;
    }

    private static boolean GraphicsReady() {
        try {
            RenderSystem.getDevice();
        } catch (IllegalStateException Exception) {
            return false;
        }

        Minecraft Client = Minecraft.getInstance();

        return Client != null && Client.getTextureManager() != null;
    }

    public static void ClearTextureCache() {
        JavaUnbound.LOGGER.info("[ResourceUtilities] Clearing texture cache: {} resolved, {} missing, {} fallback", ResolvedTextures.size(), MissingTextures.size(), FallbackTextures.size());
        ReleaseFallbackTextures();
        ResolvedTextures.clear();
        MissingTextures.clear();
    }

    public static boolean MapTexture(String MinecraftPath, String BedrockResourcePath) {
        List<ResourcePackOrder.PackEntry> Packs = ResourcePackOrder.GetResourcepacks();
        int BedrockCount = 0;

        for (ResourcePackOrder.PackEntry PackEntry : Packs) {
            if (PackEntry.IsBedrock()) {
                PackEntry.Definition().Mapper.RemoveIdentifier(MinecraftPath);
                BedrockCount++;
            }
        }

        for (ResourcePackOrder.PackEntry PackEntry : Packs) {
            if (!PackEntry.IsBedrock()) {continue;}

            Path Resource = ResolveBedrockResource(PackEntry.Id(), BedrockResourcePath);
            if (Resource == null) {continue;}

            PackFolders.SetIdentifier(PackEntry.Definition(), MinecraftPath, Resource.toString());
            JavaUnbound.LOGGER.info("[ResourceUtilities] Mapped {} -> {} from pack {} (definition {})", MinecraftPath, Resource, PackEntry.Id(), System.identityHashCode(PackEntry.Definition()));
            return true;
        }

        JavaUnbound.LOGGER.info("[ResourceUtilities] No bedrock pack provides {} ({} bedrock packs checked), vanilla is used", BedrockResourcePath, BedrockCount);
        return false;
    }

    public static TextureResourceRecord ResolveTextureResource(String JavaResourcePath, String BedrockResourcePath) {
        for (ResourcePackOrder.PackEntry PackEntry : ResourcePackOrder.GetResourcepacks()) {
            if (PackEntry.IsBedrock()) {
                if (BedrockResourcePath != null) {
                    Path ResourcePath = ResolveBedrockResource(PackEntry.Id(), BedrockResourcePath);

                    if (ResourcePath != null) {
                        return new TextureResourceRecord(true, ResourcePath.toString());
                    }
                }
            } else {
                if (JavaResourcePath != null) {
                    Identifier Resource = Identifier.withDefaultNamespace(JavaResourcePath);

                    if (Minecraft.getInstance().getResourceManager().getResource(Resource).isPresent()) {
                        return new TextureResourceRecord(false, Resource.toString());
                    }
                }
            }
        }

        if (BedrockResourcePath != null) {
            Identifier Fallback = RegisterFallbackTexture(BedrockResourcePath);

            if (Fallback != null) {
                return new TextureResourceRecord(false, Fallback.toString());
            }
        }

        return null;
    }

    private static boolean IsFallback(Identifier Id) {
        return Id.getNamespace().equals(JavaUnbound.MOD_ID) && Id.getPath().startsWith("fallback/");
    }

    private static Identifier RegisterFallbackTexture(String BedrockResourcePath) {
        String Key = BedrockResourcePath.replace('\\', '/').toLowerCase();

        Identifier Existing = FallbackTextures.get(Key);

        if (Existing != null) {
            return Existing;
        }

        BufferedImage Image = FallbackUtilities.GetFallbackImage(Key);

        if (Image == null) {
            return null;
        }

        try {
            Identifier Id = Identifier.fromNamespaceAndPath(JavaUnbound.MOD_ID, "fallback/" + SanitizePath(Key));

            RegisterImage(Id, Image);

            FallbackTextures.put(Key, Id);

            return Id;
        } catch (Exception Exception) {
            JavaUnbound.LOGGER.error("[ResourceUtilities] Failed to register fallback texture: " + Key, Exception);
            return null;
        }
    }

    private static Identifier RegisterFileTexture(Path File) {
        String Key = "file/" + File.toString().replace('\\', '/').toLowerCase();

        Identifier Existing = FallbackTextures.get(Key);

        if (Existing != null) {
            return Existing;
        }

        try {
            BufferedImage Image = ImageIO.read(File.toFile());

            if (Image == null) {
                Image = TgaConverter.DecodeTga(Files.readAllBytes(File));
            }

            if (Image == null) {
                return null;
            }

            Identifier Id = Identifier.fromNamespaceAndPath(JavaUnbound.MOD_ID, "pack/" + Integer.toHexString(Key.hashCode()) + "_" + SanitizePath(File.getFileName().toString()));

            RegisterImage(Id, Image);

            FallbackTextures.put(Key, Id);

            return Id;
        } catch (Exception Exception) {
            JavaUnbound.LOGGER.error("[ResourceUtilities] Failed to register pack texture: " + File, Exception);
            return null;
        }
    }

    private static void RegisterImage(Identifier Id, BufferedImage Image) throws Exception {
        ByteArrayOutputStream Bytes = new ByteArrayOutputStream();

        ImageIO.write(Image, "png", Bytes);

        NativeImage Native = NativeImage.read(new ByteArrayInputStream(Bytes.toByteArray()));

        Minecraft.getInstance().getTextureManager().register(Id, new DynamicTexture(() -> "JavaUnbound texture " + Id, Native));
    }

    private static void ReleaseFallbackTextures() {
        if (FallbackTextures.isEmpty()) {
            return;
        }

        TextureManager TextureManager = Minecraft.getInstance().getTextureManager();

        for (Identifier Id : FallbackTextures.values()) {
            TextureManager.release(Id);
        }

        FallbackTextures.clear();
    }

    private static String SanitizePath(String Value) {
        return Value.toLowerCase().replaceAll("[^a-z0-9/._-]", "_");
    }

    private static Path ResolveBedrockResource(String PackId, String BedrockResourcePath) {
        ResourcePackDefinition PackDefinition = ResourcePackLoader.GetPack(PackId);

        if (PackDefinition == null) {
            JavaUnbound.LOGGER.error("[ResourceUtilities] No pack found for: " + PackId);
            return null;
        }

        Path ResourcePacksDirectory = PackFolders.GetResourcePacksFolder();

        if (ResourcePacksDirectory == null || !ResourcePacksDirectory.toFile().exists()) {
            JavaUnbound.LOGGER.error("[ResourceUtilities] Resource packs folder does not exist");
            return null;
        }

        Path BedrockPackDirectory = ResourcePacksDirectory.resolve(PackDefinition.FolderName).normalize();

        if (!BedrockPackDirectory.toFile().exists()) {
            JavaUnbound.LOGGER.error("[ResourceUtilities] Bedrock resource pack does not exist: " + BedrockPackDirectory);
            return null;
        }

        return ResolveBedrockResourcePath(PackId, BedrockPackDirectory, BedrockResourcePath);
    }

    private static Path ResolveBedrockResourcePath(String PackId, Path BedrockPackDirectory, String BedrockResourcePath) {
        String SelectedSubpack = ResourcePackSettingsUtilities.GetSubpackSetting(PackId);

        if (SelectedSubpack != null && !SelectedSubpack.isEmpty()) {
            Path SubpackDirectory = BedrockPackDirectory.resolve("subpacks").resolve(SelectedSubpack);
            Path SubpackResource = ResolveInside(SubpackDirectory, BedrockResourcePath);

            if (SubpackResource != null) {
                return SubpackResource;
            }
        }

        Path RootResource = ResolveInside(BedrockPackDirectory, BedrockResourcePath);

        if (RootResource != null) {
            return RootResource;
        }

        JavaUnbound.LOGGER.debug("Bedrock resource not found in pack " + PackId + ": " + BedrockResourcePath);
        return null;
    }

    private static Path ResolveInside(Path Directory, String RelativePath) {
        Path Base = Directory.normalize();
        Path Resolved = Base.resolve(RelativePath).normalize();

        if (!Resolved.startsWith(Base) || !Resolved.toFile().exists()) {
            return null;
        }

        return Resolved;
    }
}