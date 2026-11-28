package com.java_unbound.shared.resourcepack;

import com.java_unbound.JavaUnbound;
//import com.java_unbound.fallback.FallbackUtilities;
import com.java_unbound.fallback.FallbackUtilities;
import com.java_unbound.shared.definitions.resource_pack.ResourcePackDefinition;
//import com.java_unbound.shared.loaders.main_screen.SplashLoader;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.server.packs.resources.IoSupplier;
import org.jspecify.annotations.Nullable;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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

public class FolderResources implements PackResources {
    private final ResourcePackDefinition Definition;
    private final Path Root;
    private final PackLocationInfo Location;

    public FolderResources(ResourcePackDefinition Definition, PackLocationInfo Location) {
        this.Definition = Definition;
        this.Root = Definition.Path;
        this.Location = Location;
    }

    @Override
    public @Nullable IoSupplier<InputStream> getRootResource(String... Paths) {
        if (Paths.length == 1 && "pack.png".equals(Paths[0])) {
            Path PackIcon = ResolvePath(Root.resolve("pack_icon.png"));

            if (PackIcon != null && Files.isRegularFile(PackIcon)) {
                return IoSupplier.create(PackIcon);
            }

            BufferedImage Texture = FallbackUtilities.GetFallbackImage("/pack_icon.png");

            if (Texture == null) {
                JavaUnbound.LOGGER.error("Texture is null");
                return null;
            }

            return () -> {
                ByteArrayOutputStream OutputStream = new ByteArrayOutputStream();

                ImageIO.write(Texture, "png", OutputStream);

                return new ByteArrayInputStream(OutputStream.toByteArray());
            };
        }

        Path File = Root;

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
    public @Nullable IoSupplier<InputStream> getResource(PackType Type, Identifier Identifier) {
        if (Type != PackType.CLIENT_RESOURCES) {return null;}

        if (Identifier.getNamespace().equals("minecraft")) {
            if (Identifier.getPath().equals("texts/splashes.txt")) {
                /*try {
                    return null; SplashLoader.LoadSplashes();
                } catch (IOException Exception) {
                    throw new RuntimeException(Exception);
                }*/

                return null;
            }

            byte[] Virtual = Definition.Virtual.Get(Identifier.getPath());

            if (Virtual != null) {
                return () -> new ByteArrayInputStream(Virtual);
            }
        }

        Path File = ResolveResource(Identifier);

        if (File != null && Files.isRegularFile(File)) {
            return IoSupplier.create(File);
        }

        return null;
    }

    @Override
    public void listResources(PackType Type, String Namespace, String Prefix, ResourceOutput Output) {
        if (Type != PackType.CLIENT_RESOURCES) {return;}

        if (Namespace.equals("minecraft")) {
            for (Map.Entry<String, byte[]> Entry : Definition.Virtual.GetResources().entrySet()) {
                String ResourcePath = Entry.getKey();

                if (!ResourcePath.startsWith(Prefix)) {
                    continue;
                }

                Identifier ResourceIdentifier = Identifier.tryParse("minecraft:" + ResourcePath);

                if (ResourceIdentifier == null) {
                    continue;
                }

                byte[] Data = Entry.getValue();

                Output.accept(ResourceIdentifier, () -> new ByteArrayInputStream(Data));
            }
        }

        Path NamespaceFolder = ResolvePath(Root.resolve("assets").resolve(Namespace));

        if (NamespaceFolder != null && Files.isDirectory(NamespaceFolder)) {
            Path SearchFolder = ResolvePath(NamespaceFolder.resolve(Prefix));

            if (SearchFolder != null && Files.isDirectory(SearchFolder)) {
                try (Stream<Path> FilesStream = Files.walk(SearchFolder)) {
                    FilesStream.filter(Files::isRegularFile).forEach(File -> {
                        String ResourcePath = NamespaceFolder.relativize(File).toString().replace('\\', '/').toLowerCase(Locale.ROOT);
                        Identifier ResourceIdentifier = Identifier.tryParse(Namespace + ":" + ResourcePath);

                        if (ResourceIdentifier != null) {
                            Output.accept(ResourceIdentifier, IoSupplier.create(File));
                        }
                    });
                } catch (IOException Exception) {
                    JavaUnbound.LOGGER.error("Failed to list resources in " + SearchFolder, Exception);
                }
            }
        }

        if (!Namespace.equals("minecraft")) {
            return;
        }

        for (Map.Entry<String, String> Mapping : Definition.Mapper.GetMappings().entrySet()) {
            String MinecraftPath = Mapping.getKey();

            if (!MinecraftPath.startsWith(Prefix)) {
                continue;
            }

            Path File = ResolvePath(Root.resolve(Mapping.getValue()));

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

        Path Assets = ResolvePath(Root.resolve("assets"));

        if (Assets != null && Files.isDirectory(Assets)) {
            try (Stream<Path> FilesStream = Files.list(Assets)) {
                FilesStream.filter(Files::isDirectory).map(Path::getFileName).map(Path::toString).map(Value -> Value.toLowerCase(Locale.ROOT)).forEach(Namespaces::add);
            } catch (IOException Exception) {
                JavaUnbound.LOGGER.error("Failed to list namespaces in " + Assets, Exception);
            }
        }

        return Namespaces;
    }

    @Override
    public @Nullable <T> T getMetadataSection(MetadataSectionType<T> MetadataSerializer) throws IOException {
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
        String ResourcePath = Identifier.getPath();

        if (Namespace.equals("minecraft")) {
            String MappedPath = Definition.Mapper.GetIdentifier(ResourcePath);

            if (MappedPath != null) {
                return ResolvePath(Root.resolve(MappedPath));
            }
        }

        return ResolvePath(Root.resolve("assets").resolve(Namespace).resolve(ResourcePath));
    }

    public Path ResolvePath(Path File) {
        if (Files.exists(File, new LinkOption[0])) {
            return File;
        }

        Path RelativePath;

        try {
            RelativePath = Root.relativize(File);
        } catch (IllegalArgumentException Exception) {
            return null;
        }

        Path Current = Root;

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