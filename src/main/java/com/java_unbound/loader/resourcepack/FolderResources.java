package com.java_unbound.loader.resourcepack;

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
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

public final class FolderResources implements PackResources {
    private final Path ResourcePack;
    private final PackLocationInfo Location;

    public FolderResources(Path ResourcePack, PackLocationInfo Location) {
        this.ResourcePack = ResourcePack;
        this.Location = Location;
    }

    @Override
    public IoSupplier<InputStream> getRootResource(String... Paths) {
        if (Paths.length == 1 && "pack.png".equals(Paths[0])) {
            Path PackIcon = ResolvePath(ResourcePack.resolve("pack_icon.png"));

            if (PackIcon != null && Files.isRegularFile(PackIcon, new LinkOption[0])) {
                return IoSupplier.create(PackIcon);
            }
        }

        Path File = ResourcePack;

        for (String PathPart : Paths) {
            File = File.resolve(PathPart);
        }

        File = ResolvePath(File);

        if (File != null && Files.isRegularFile(File, new LinkOption[0])) {
            return IoSupplier.create(File);
        }

        return null;
    }

    public static void CreateFolder(Path ResourcePack, String FolderPath) throws IOException {
        if (FolderPath == null || FolderPath.isBlank()) {
            throw new IllegalArgumentException("Path cannot be empty");
        }

        Path RelativePath = Path.of(FolderPath.replace('\\', '/')).normalize();

        if (RelativePath.isAbsolute() || RelativePath.startsWith("..")) {
            throw new IllegalArgumentException("Path must stay inside the resource pack");
        }

        Path Current = ResourcePack;

        for (Path Part : RelativePath) {
            String Name = Part.toString();

            Path Exact = Current.resolve(Name);

            if (Files.exists(Exact, new LinkOption[0])) {
                if (!Files.isDirectory(Exact, new LinkOption[0])) {
                    throw new IOException("Path part is not a directory: " + Exact);
                }

                Current = Exact;
                continue;
            }

            Path Match = null;

            if (Files.isDirectory(Current, new LinkOption[0])) {
                try (Stream<Path> FilesStream = Files.list(Current)) {
                    Match = FilesStream
                            .filter(File -> Files.isDirectory(File, new LinkOption[0]))
                            .filter(File -> File.getFileName().toString().equalsIgnoreCase(Name))
                            .findFirst()
                            .orElse(null);
                }
            }

            if (Match != null) {
                Current = Match;
                continue;
            }

            Current = Files.createDirectory(Exact);
        }
    }

    @Override
    public IoSupplier<InputStream> getResource(PackType Type, Identifier Identifier) {
        if (Type != PackType.CLIENT_RESOURCES) {
            return null;
        }

        Path File = ResourcePack.resolve("assets").resolve(Identifier.getNamespace()).resolve(Identifier.getPath());

        File = ResolvePath(File);

        if (File != null && Files.isRegularFile(File, new LinkOption[0])) {
            return IoSupplier.create(File);
        }

        return null;
    }

    @Override
    public void listResources(PackType Type, String Namespace, String Path, ResourceOutput Output) {
        if (Type != PackType.CLIENT_RESOURCES) {
            return;
        }

        Path NamespaceFolder = ResolvePath(ResourcePack.resolve("assets").resolve(Namespace));

        if (NamespaceFolder == null || !Files.isDirectory(NamespaceFolder, new LinkOption[0])) {
            return;
        }

        Path SearchFolder = ResolvePath(NamespaceFolder.resolve(Path));

        if (SearchFolder == null || !Files.isDirectory(SearchFolder, new LinkOption[0])) {
            return;
        }

        try (Stream<Path> FilesStream = Files.walk(SearchFolder)) {
            FilesStream.filter(Files::isRegularFile)
                    .forEach(File -> {
                        Path RelativePath = NamespaceFolder.relativize(File);
                        String ResourcePath = RelativePath.toString().replace('\\', '/').toLowerCase(Locale.ROOT);

                        Identifier identifier = Identifier.tryParse(Namespace + ":" + ResourcePath);

                        if (identifier != null) {
                            Output.accept(identifier, IoSupplier.create(File));
                        }
                    });
        } catch (IOException ignored) {
        }
    }

    @Override
    public Set<String> getNamespaces(PackType Type) {
        if (Type != PackType.CLIENT_RESOURCES) {
            return Set.of();
        }

        Path Assets = ResolvePath(ResourcePack.resolve("assets"));

        if (Assets == null || !Files.isDirectory(Assets, new LinkOption[0])) {
            return Set.of();
        }

        Set<String> Namespaces = new HashSet<>();

        try (Stream<Path> FilesStream = Files.list(Assets)) {
            FilesStream.filter(Files::isDirectory).map(Path::getFileName).map(Path::toString).map(Value -> Value.toLowerCase(Locale.ROOT)).forEach(Namespaces::add);
        } catch (IOException ignored) {
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

    private Path ResolvePath(Path File) {
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

            Path Match = null;

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