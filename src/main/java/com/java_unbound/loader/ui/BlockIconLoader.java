package com.java_unbound.loader.ui;

import com.java_unbound.JavaUnbound;
import com.java_unbound.loader.resourcepack.Folder;
import net.minecraft.core.registries.BuiltInRegistries;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class BlockIconLoader {
    private static final Path SubpackFolder = Folder.GetResourceFolder().resolve("subpacks").resolve(JavaUnbound.SUBPACK);
    private static final Path SubpackBlockTextureFolder = SubpackFolder.resolve("textures").resolve("blocks");
    private static final Path ReportFolder = Folder.GetResourceFolder().resolve("logs");
    private static final Path UnusedTexturesFile = ReportFolder.resolve("unused_block_textures.txt");
    private static final Path ReportFile = ReportFolder.resolve("block_texture_report.txt");

    private static final Map<String, String> ResolvedCache = new ConcurrentHashMap<>();
    private static final Map<String, String> ManualMappings = new HashMap<>();
    private static final Set<String> UsedSourceTextures = new HashSet<>();
    private static final Set<String> UnusedSourceTextures = new LinkedHashSet<>();
    private static final Map<String, List<String>> TargetCollisions = new LinkedHashMap<>();

    private static final double MinimumScore = 0.82;
    private static final double MinimumScoreDifference = 0.08;

    private static int TotalTextures;
    private static int UsedTextures;
    private static int UnusedTextures;

    static {
        ManualMappings.put("activator_rail_carried", "activator_rail");
        ManualMappings.put("detector_rail_carried", "detector_rail");
        ManualMappings.put("powered_rail_carried", "powered_rail");
        ManualMappings.put("rail_carried", "rail");
        ManualMappings.put("golden_dandelion_carried", "golden_dandelion");
    }

    private BlockIconLoader() {
    }

    public static void LoadBlockIcons() {
        ResetCounters();

        if (!Files.exists(SubpackBlockTextureFolder)) {
            SaveReport();
            return;
        }

        Set<String> JavaBlockNames = GetMinecraftBlockNames();
        Map<String, String> NormalizedJavaNames = BuildNormalizedNames(JavaBlockNames);

        try (var Stream = Files.walk(SubpackBlockTextureFolder)) {
            Stream.filter(Files::isRegularFile).filter(BlockIconLoader::IsPng).forEach(Path -> ProcessTexture(Path, JavaBlockNames, NormalizedJavaNames));
        } catch (IOException E) {
            E.printStackTrace();
        }

        SaveReport();

        System.out.println("========== Block Texture Loader ==========");
        System.out.println("Subpack: " + JavaUnbound.SUBPACK);
        System.out.println("Total textures: " + TotalTextures);
        System.out.println("Used textures: " + UsedTextures);
        System.out.println("Unused textures: " + UnusedTextures);
        System.out.println("Target collisions: " + TargetCollisions.size());
        System.out.println("==========================================");
    }

    private static void ProcessTexture(Path Path, Set<String> JavaBlockNames, Map<String, String> NormalizedJavaNames) {
        TotalTextures++;

        String SourceFileName = Path.getFileName().toString();
        String SourceName = RemoveExtension(SourceFileName);
        String RelativePath = GetRelativePath(Path);

        if (ShouldIgnore(SourceName)) {
            UnusedSourceTextures.add(RelativePath);
            UnusedTextures++;

            return;
        }

        String TargetFileName = ResolveTargetName(SourceName, JavaBlockNames, NormalizedJavaNames);

        if (TargetFileName == null) {
            UnusedSourceTextures.add(RelativePath);
            UnusedTextures++;

            JavaUnbound.LOGGER.warn("Unused block texture: " + RelativePath);

            return;
        }

        String SourcePath = "subpacks/" + JavaUnbound.SUBPACK + "/textures/blocks/" + RelativePath;
        String TargetPath = "textures/block/" + TargetFileName + ".png";

        List<String> Sources = TargetCollisions.computeIfAbsent(TargetPath, Key -> new ArrayList<>());

        Sources.add(RelativePath);

        boolean IsManualMapping = ManualMappings.containsKey(SourceName);

        if (!IsManualMapping && Sources.size() > 1) {
            UnusedSourceTextures.add(RelativePath);
            UnusedTextures++;

            return;
        }

        Folder.SetIdentifier(TargetPath, SourcePath);

        UsedSourceTextures.add(RelativePath);
        UsedTextures++;
    }

    private static String ResolveTargetName(String SourceName, Set<String> JavaBlockNames, Map<String, String> NormalizedJavaNames) {
        String Cached = ResolvedCache.get(SourceName);

        if (Cached != null) return Cached;

        if (JavaBlockNames.contains(SourceName)) {
            ResolvedCache.put(SourceName, SourceName);

            return SourceName;
        }

        String Manual = ManualMappings.get(SourceName);

        if (Manual != null) {
            ResolvedCache.put(SourceName, Manual);

            return Manual;
        }

        String Family = ApplyFamilyTransformation(SourceName, JavaBlockNames);

        if (Family != null) {
            ResolvedCache.put(SourceName, Family);

            return Family;
        }

        String Generic = ApplyGenericTransformation(SourceName);

        if (Generic != null) {
            if (JavaBlockNames.contains(Generic)) {
                ResolvedCache.put(SourceName, Generic);

                return Generic;
            }
        }

        String NormalizedExact = NormalizedJavaNames.get(Normalize(SourceName));

        if (NormalizedExact != null) {
            ResolvedCache.put(SourceName, NormalizedExact);

            return NormalizedExact;
        }

        String BestMatch = FindBestMatch(SourceName, JavaBlockNames);

        if (BestMatch != null) {
            ResolvedCache.put(SourceName, BestMatch);

            return BestMatch;
        }

        return null;
    }

    private static String ApplyFamilyTransformation(String Name, Set<String> JavaBlockNames) {
        if (Name.endsWith("_side")) {
            String Base = Name.substring(0, Name.length() - 5);

            if (JavaBlockNames.contains(Base)) {
                return Base;
            }
        }

        if (Name.endsWith("_top")) {
            String Base = Name.substring(0, Name.length() - 4);

            if (JavaBlockNames.contains(Base)) {
                return Base;
            }
        }

        if (Name.endsWith("_bottom")) {
            String Base = Name.substring(0, Name.length() - 7);

            if (JavaBlockNames.contains(Base)) {
                return Base;
            }
        }

        return null;
    }

    private static String ApplyGenericTransformation(String Name) {
        if (Name.startsWith("wood_")) {
            return "wooden_" + Name.substring(5);
        }

        if (Name.startsWith("stone_")) {
            return Name;
        }

        if (Name.startsWith("grass_")) {
            return "grass_block" + Name.substring(5);
        }

        if (Name.startsWith("dirt_")) {
            return "dirt" + Name.substring(4);
        }

        if (Name.startsWith("sand_")) {
            return "sand" + Name.substring(4);
        }

        if (Name.startsWith("red_sand_")) {
            return "red_sand" + Name.substring(8);
        }

        if (Name.startsWith("cobblestone_")) {
            return "cobblestone" + Name.substring(12);
        }

        return null;
    }

    private static String FindBestMatch(String SourceName, Set<String> JavaBlockNames) {
        String NormalizedSource = Normalize(SourceName);

        String BestMatch = null;

        double BestScore = 0.0;
        double SecondBestScore = 0.0;

        for (String JavaName : JavaBlockNames) {
            double Score = CalculateScore(SourceName, NormalizedSource, JavaName);

            if (Score > BestScore) {
                SecondBestScore = BestScore;
                BestScore = Score;
                BestMatch = JavaName;
            } else if (Score > SecondBestScore) {
                SecondBestScore = Score;
            }
        }

        if (BestMatch == null) return null;
        if (BestScore < MinimumScore) return null;
        if (BestScore - SecondBestScore < MinimumScoreDifference) return null;

        return BestMatch;
    }

    private static double CalculateScore(String SourceName, String NormalizedSource, String JavaName) {
        String NormalizedJava = Normalize(JavaName);

        Set<String> SourceTokens = Tokenize(SourceName);
        Set<String> JavaTokens = Tokenize(JavaName);

        double TokenScore = TokenSimilarity(SourceTokens, JavaTokens);
        double NormalizedScore = Similarity(NormalizedSource, NormalizedJava);
        double RawScore = Similarity(SourceName, JavaName);
        double PrefixScore = PrefixSimilarity(SourceName, JavaName);
        double SemanticScore = SemanticTokenSimilarity(SourceTokens, JavaTokens);

        return TokenScore * 0.40 + NormalizedScore * 0.25 + SemanticScore * 0.20 + RawScore * 0.10 + PrefixScore * 0.05;
    }

    private static double TokenSimilarity(Set<String> A, Set<String> B) {
        if (A.isEmpty() || B.isEmpty()) return 0.0;

        Set<String> Intersection = new HashSet<>(A);
        Intersection.retainAll(B);

        Set<String> Union = new HashSet<>(A);
        Union.addAll(B);

        return (double) Intersection.size() / Union.size();
    }

    private static double SemanticTokenSimilarity(Set<String> A, Set<String> B) {
        return TokenSimilarity(ExpandTokens(A), ExpandTokens(B));
    }

    private static Set<String> ExpandTokens(Set<String> Tokens) {
        Set<String> Result = new HashSet<>(Tokens);

        for (String Token : Tokens) {
            switch (Token) {
                case "wood" -> Result.add("wooden");
                case "wooden" -> Result.add("wood");
                case "brick" -> Result.add("bricks");
                case "bricks" -> Result.add("brick");
                case "smooth" -> Result.add("polished");
                case "log" -> Result.add("stem");
                case "stem" -> Result.add("log");
                case "plank" -> Result.add("planks");
                case "planks" -> Result.add("plank");
                case "grass" -> Result.add("block");
                case "rail" -> Result.add("track");
                case "track" -> Result.add("rail");
            }
        }

        return Result;
    }

    private static Set<String> Tokenize(String Name) {
        String Value = Name.toLowerCase(Locale.ROOT).replace(".png", "");

        return new HashSet<>(Arrays.asList(Value.split("_")));
    }

    private static String Normalize(String Name) {
        String Value = Name.toLowerCase(Locale.ROOT).replace(".png", "");

        Value = Value.replace("wooden", "wood");
        Value = Value.replace("polished", "smooth");
        Value = Value.replace("planks", "plank");
        Value = Value.replace("bricks", "brick");

        return Value;
    }

    private static double Similarity(String A, String B) {
        if (A.equals(B)) return 1.0;
        if (A.isEmpty() || B.isEmpty()) return 0.0;

        int Distance = LevenshteinDistance(A, B);

        return 1.0 - ((double) Distance / Math.max(A.length(), B.length()));
    }

    private static double PrefixSimilarity(String A, String B) {
        int Length = Math.min(A.length(), B.length());

        if (Length == 0) return 0.0;

        int Same = 0;

        for (int I = 0; I < Length; I++) {
            if (A.charAt(I) != B.charAt(I)) break;

            Same++;
        }

        return (double) Same / Length;
    }

    private static int LevenshteinDistance(String A, String B) {
        int[] Previous = new int[B.length() + 1];
        int[] Current = new int[B.length() + 1];

        for (int J = 0; J <= B.length(); J++) {
            Previous[J] = J;
        }

        for (int I = 1; I <= A.length(); I++) {
            Current[0] = I;

            for (int J = 1; J <= B.length(); J++) {
                int Cost = A.charAt(I - 1) == B.charAt(J - 1) ? 0 : 1;

                Current[J] = Math.min(Math.min(Current[J - 1] + 1, Previous[J] + 1), Previous[J - 1] + Cost);
            }

            int[] Swap = Previous;

            Previous = Current;
            Current = Swap;
        }

        return Previous[B.length()];
    }

    private static Set<String> GetMinecraftBlockNames() {
        Set<String> Names = new HashSet<>();

        BuiltInRegistries.BLOCK.keySet().forEach(Key -> {
            String Value = Key.toString();

            if (Value.startsWith("minecraft:")) {
                Names.add(Value.substring("minecraft:".length()));
            }
        });

        return Names;
    }

    private static Map<String, String> BuildNormalizedNames(Set<String> Names) {
        Map<String, String> Result = new HashMap<>();

        for (String Name : Names) {
            Result.putIfAbsent(Normalize(Name), Name);
        }

        return Result;
    }

    private static boolean ShouldIgnore(String Name) {
        return Name.startsWith("icon_") || Name.startsWith("atlas_") || Name.startsWith("gui_");
    }

    private static boolean IsPng(Path Path) {
        return Path.toString().toLowerCase(Locale.ROOT).endsWith(".png");
    }

    private static String RemoveExtension(String Name) {
        return Name.toLowerCase(Locale.ROOT).endsWith(".png") ? Name.substring(0, Name.length() - 4) : Name;
    }

    private static String GetRelativePath(Path Path) {
        return SubpackBlockTextureFolder.relativize(Path).toString().replace(File.separator, "/");
    }

    private static void ResetCounters() {
        TotalTextures = 0;
        UsedTextures = 0;
        UnusedTextures = 0;

        ResolvedCache.clear();
        UsedSourceTextures.clear();
        UnusedSourceTextures.clear();
        TargetCollisions.clear();
    }

    private static void SaveReport() {
        try {
            Files.createDirectories(ReportFolder);

            List<String> Report = new ArrayList<>();

            Report.add("Java Unbound Block Texture Report");
            Report.add("=================================");
            Report.add("Subpack: " + JavaUnbound.SUBPACK);
            Report.add("");

            Report.add("Total textures: " + TotalTextures);
            Report.add("Used textures: " + UsedTextures);
            Report.add("Unused textures: " + UnusedTextures);

            Report.add("Usage: " + (TotalTextures == 0 ? 0.0 : (double) UsedTextures / TotalTextures * 100.0) + "%");

            Report.add("");
            Report.add("Unused textures:");
            Report.add("");

            if (UnusedSourceTextures.isEmpty()) {
                Report.add("None");
            } else {
                Report.addAll(UnusedSourceTextures);
            }

            Report.add("");
            Report.add("Target collisions:");
            Report.add("");

            boolean HasCollision = false;

            for (Map.Entry<String, List<String>> Entry : TargetCollisions.entrySet()) {
                if (Entry.getValue().size() <= 1) continue;

                HasCollision = true;

                Report.add(Entry.getKey());

                for (String Source : Entry.getValue()) {
                    Report.add("  <- " + Source);
                }

                Report.add("");
            }

            if (!HasCollision) {
                Report.add("None");
            }

            Files.write(UnusedTexturesFile, UnusedSourceTextures, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
            Files.write(ReportFile, Report, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
        } catch (IOException E) {
            E.printStackTrace();
        }
    }
}