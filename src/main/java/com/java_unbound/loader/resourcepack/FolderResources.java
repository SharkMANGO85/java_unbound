package com.java_unbound.loader.resourcepack;

import com.java_unbound.loader.ui.Splashes;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.server.packs.resources.IoSupplier;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public final class FolderResources implements PackResources {
    public final Path ResourcePack;
    private final PackLocationInfo Location;

    public FolderResources(Path ResourcePack, PackLocationInfo Location) {
        this.ResourcePack = ResourcePack;
        this.Location = Location;
    }

    public Path GetResourcePack() {
        return ResourcePack;
    }

    @Override
    public IoSupplier<InputStream> getRootResource(String... Paths) {
        Path File = ResourcePack;

        for (String PathPart : Paths) {
            File = File.resolve(PathPart);
        }

        File = ResolvePath(File);

        if (File != null && Files.isRegularFile(File)) {
            return IoSupplier.create(File);
        }

        return null;
    }

    @Override
    public IoSupplier<InputStream> getResource(PackType Type, Identifier Identifier) {
        if (Type != PackType.CLIENT_RESOURCES) {
            return null;
        }

        if (Identifier.getNamespace().equals("minecraft") && Identifier.getPath().equals("texts/splashes.txt")) {
            return Splashes.GetSplashResource(this.ResourcePack);
        }

        Path File = ResolveResource(Identifier);

        if (File != null && Files.isRegularFile(File)) {
            return IoSupplier.create(File);
        }

        return null;
    }

    @Override
    public void listResources(PackType Type, String Namespace, String Prefix, ResourceOutput Output) {
        if (Type != PackType.CLIENT_RESOURCES) {
            return;
        }

        Path NamespaceFolder = ResolvePath(ResourcePack.resolve("assets").resolve(Namespace));

        if (NamespaceFolder != null && Files.isDirectory(NamespaceFolder)) {
            Path SearchFolder = ResolvePath(NamespaceFolder.resolve(Prefix));

            if (SearchFolder != null && Files.isDirectory(SearchFolder)) {
                try (Stream<Path> FilesStream = Files.walk(SearchFolder)) {
                    FilesStream.filter(Files::isRegularFile).forEach(File -> {
                        Path RelativePath = NamespaceFolder.relativize(File);
                        String ResourcePath = RelativePath.toString().replace('\\', '/').toLowerCase(Locale.ROOT);
                        Identifier ResourceIdentifier = Identifier.tryParse(Namespace + ":" + ResourcePath);

                        if (ResourceIdentifier != null) {
                            Output.accept(ResourceIdentifier, IoSupplier.create(File));
                        }
                    });
                } catch (IOException Exception) {
                    Exception.printStackTrace();
                }
            }
        }

        if (!Namespace.equals("minecraft")) {
            return;
        }

        for (Map.Entry<String, String> Mapping : ResourceMapper.GetMappings().entrySet()) {
            String MinecraftPath = Mapping.getKey();

            if (!MinecraftPath.startsWith(Prefix)) {
                continue;
            }

            Path File = ResolvePath(ResourcePack.resolve(Mapping.getValue()));

            if (File == null || !Files.isRegularFile(File)) {
                continue;
            }

            Identifier ResourceIdentifier = Identifier.tryParse("minecraft:" + MinecraftPath);

            if (ResourceIdentifier == null) {
                continue;
            }

            Output.accept(ResourceIdentifier, IoSupplier.create(File));
        }
    }

    @Override
    public Set<String> getNamespaces(PackType Type) {
        if (Type != PackType.CLIENT_RESOURCES) {
            return Set.of();
        }

        Set<String> Namespaces = new HashSet<>();
        Namespaces.add("minecraft");

        Path Assets = ResolvePath(ResourcePack.resolve("assets"));

        if (Assets != null && Files.isDirectory(Assets)) {
            try (Stream<Path> FilesStream = Files.list(Assets)) {
                FilesStream.filter(Files::isDirectory).map(Path::getFileName).map(Path::toString).map(Value -> Value.toLowerCase(Locale.ROOT)).forEach(Namespaces::add);
            } catch (IOException Exception) {
                Exception.printStackTrace();
            }
        }

        return Namespaces;
    }

    @Override
    public <T> T getMetadataSection(MetadataSectionType<T> Type) {
        return null;
    }

    @Override
    public PackLocationInfo location() {
        return Location;
    }

    @Override
    public String packId() {
        return Location.id();
    }

    @Override
    public void close() {
    }

    private Path ResolveResource(Identifier Identifier) {
        String Namespace = Identifier.getNamespace();
        String Path = Identifier.getPath();

        if (Namespace.equals("minecraft")) {
            String MappedPath = ResourceMapper.GetIdentifier(Path);

            if (MappedPath != null) {
                return ResolvePath(ResourcePack.resolve(MappedPath));
            }
        }

        return ResolvePath(ResourcePack.resolve("assets").resolve(Namespace).resolve(Path));
    }

    public Path ResolvePath(Path File) {
        if (Files.exists(File, new LinkOption[0])) {
            return File;
        }

        Path RelativePath;

        try {
            RelativePath = ResourcePack.relativize(File);
        } catch (IllegalArgumentException Exception) {
            return null;
        }

        Path Current = ResourcePack;

        for (Path Part : RelativePath) {
            String Name = Part.toString();
            Path Exact = Current.resolve(Name);

            if (Files.exists(Exact, new LinkOption[0])) {
                Current = Exact;
                continue;
            }

            Path Match;

            try (Stream<Path> FilesStream = Files.list(Current)) {
                Match = FilesStream.filter(FilePart -> FilePart.getFileName().toString().equalsIgnoreCase(Name)).findFirst().orElse(null);
            } catch (IOException Exception) {
                return null;
            }

            if (Match == null) {
                return null;
            }

            Current = Match;
        }

        return Current;
    }
}