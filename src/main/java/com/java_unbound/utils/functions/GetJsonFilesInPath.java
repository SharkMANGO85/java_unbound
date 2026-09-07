package com.java_unbound.utils.functions;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public class GetJsonFilesInPath {
    public static List<Path> Get(Path SearchPath) {
        try (var Stream = Files.list(SearchPath)) {
            return Stream.filter(Path -> Path.toString().endsWith(".json")).toList();
        } catch (IOException Exception) {
            throw new RuntimeException(Exception);
        }
    }

    public static List<Path> GetDescendant(Path SearchPath) {
        try (Stream<Path> Stream = Files.walk(SearchPath)) {
            return Stream.filter(Files::isRegularFile).filter(Path -> Path.toString().endsWith(".json")).toList();
        } catch (IOException Exception) {
            throw new RuntimeException(Exception);
        }
    }
}