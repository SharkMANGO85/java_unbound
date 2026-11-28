package com.java_unbound.fallback;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.java_unbound.JavaUnbound;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public class RemoteFallbackSource {
    public static volatile boolean Enabled = true;

    private static final String Refrence = "main";
    private static final String Repository = "Mojang/bedrock-samples";
    private static final String Root = "resource_pack";

    private static final HttpClient Client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).followRedirects(HttpClient.Redirect.NORMAL).build();
    private static final Set<String> Missing = ConcurrentHashMap.newKeySet();
    private static volatile boolean Unreachable;

    public static Path GetCacheRoot() {
        return FabricLoader.getInstance().getConfigDir().resolve("java_unbound").resolve("remote_fallback").resolve(Refrence);
    }

    public static void ClearMissing() {
        Missing.clear();
        Unreachable = false;
    }

    public static List<String> ListFolder(String Folder) {
        Path Target = GetCacheRoot().resolve(Folder);
        Path Index = Target.resolve(".index");

        try {
            if (Files.isRegularFile(Index)) {
                return Files.readAllLines(Index);
            }
        } catch (IOException Exception) {
            JavaUnbound.LOGGER.debug("Could not read remote fallback index for {}", Folder, Exception);
        }

        if (Enabled && !Unreachable) {
            try {
                Files.createDirectories(Target);

                String ApiUrl = "https://api.github.com/repos/" + Repository + "/contents/" + Root + "/" + Folder + "?ref=" + Refrence;
                JsonArray Listing = JsonParser.parseString(GetString(ApiUrl)).getAsJsonArray();

                List<String> Names = new ArrayList<>();

                for (JsonElement Element : Listing) {
                    JsonObject Entry = Element.getAsJsonObject();

                    if (!Entry.get("type").getAsString().equals("file")) continue;

                    String Name = Entry.get("name").getAsString();

                    if (Name.endsWith(".json")) Names.add(Name);
                }

                Files.write(Index, Names);

                return Names;
            } catch (Exception Exception) {
                JavaUnbound.LOGGER.warn("Could not list {} from GitHub, using what is available locally", Folder, Exception);
            }
        }

        List<String> Local = new ArrayList<>();

        if (Files.isDirectory(Target)) {
            try (Stream<Path> Paths = Files.list(Target)) {
                Paths.filter(Files::isRegularFile)
                        .map(Entry -> Entry.getFileName().toString())
                        .filter(Name -> Name.endsWith(".json"))
                        .sorted()
                        .forEach(Local::add);
            } catch (IOException Exception) {
                JavaUnbound.LOGGER.debug("Could not list local remote fallback folder {}", Folder, Exception);
            }
        }

        return Local;
    }

    public static Path GetFile(String RelativePath) {
        String Relative = RelativePath.replace('\\', '/');

        while (Relative.startsWith("/")) {
            Relative = Relative.substring(1);
        }

        Path Target = GetCacheRoot().resolve(Relative);

        if (Files.isRegularFile(Target)) {
            return Target;
        }
        if (!Enabled || Unreachable || Missing.contains(Relative)) {
            return null;
        }

        try {
            Files.createDirectories(Target.getParent());

            URI Uri = new URI("https", "raw.githubusercontent.com", "/" + Repository + "/" + Refrence + "/" + Root + "/" + Relative, null);

            if (Download(Uri, Target)) {
                return Target;
            }
        } catch (Exception Exception) {
            JavaUnbound.LOGGER.debug("Remote fallback file failed: {}", Relative, Exception);
        }

        Missing.add(Relative);

        return null;
    }

    public static boolean SyncFolder(String Folder) {
        Path Target = GetCacheRoot().resolve(Folder);
        Path Marker = Target.resolve(".synced");

        if (Files.exists(Marker)) {
            return true;
        }

        if (!Enabled || Unreachable) {
            return false;
        }

        try {
            Files.createDirectories(Target);

            String ApiUrl = "https://api.github.com/repos/" + Repository + "/contents/" + Root + "/" + Folder + "?ref=" + Refrence;
            JsonArray Listing = JsonParser.parseString(GetString(ApiUrl)).getAsJsonArray();

            for (JsonElement Element : Listing) {
                JsonObject Entry = Element.getAsJsonObject();

                if (!Entry.get("type").getAsString().equals("file")) continue;

                String Name = Entry.get("name").getAsString();
                if (!Name.endsWith(".json")) continue;

                JsonElement DownloadUrl = Entry.get("download_url");
                if (DownloadUrl == null || DownloadUrl.isJsonNull()) continue;

                if (!Download(URI.create(DownloadUrl.getAsString()), Target.resolve(Name))) {
                    throw new IOException("Could not download " + Name);
                }
            }

            Files.writeString(Marker, "ok");

            return true;
        } catch (Exception Exception) {
            JavaUnbound.LOGGER.warn("Could not sync {} from GitHub, using what is available locally", Folder, Exception);

            return false;
        }
    }

    private static String GetString(String Url) throws IOException, InterruptedException {
        try {
            HttpResponse<String> Response = Client.send(HttpRequest.newBuilder(URI.create(Url)).header("Accept", "application/vnd.github+json").GET().build(), HttpResponse.BodyHandlers.ofString());

            if (Response.statusCode() != 200) {
                throw new IOException("HTTP " + Response.statusCode() + " for " + Url);
            }

            return Response.body();
        } catch (java.net.ConnectException | java.net.http.HttpTimeoutException Exception) {
            Unreachable = true;

            throw Exception;
        }
    }

    private static boolean Download(URI Uri, Path Target) throws IOException, InterruptedException {
        Path Temp = Target.resolveSibling(Target.getFileName() + ".part");

        try {
            HttpResponse<Path> Response = Client.send(HttpRequest.newBuilder(Uri).GET().build(), HttpResponse.BodyHandlers.ofFile(Temp));

            if (Response.statusCode() != 200) {
                Files.deleteIfExists(Temp);

                return false;
            }

            Files.move(Temp, Target, StandardCopyOption.REPLACE_EXISTING);

            return true;
        } catch (java.net.ConnectException | java.net.http.HttpTimeoutException Exception) {
            Unreachable = true;

            Files.deleteIfExists(Temp);

            throw Exception;
        } catch (InterruptedException Exception) {
            Thread.currentThread().interrupt();
            Files.deleteIfExists(Temp);

            throw Exception;
        }
    }
}