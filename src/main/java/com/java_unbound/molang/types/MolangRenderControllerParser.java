package com.java_unbound.molang.types;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.java_unbound.molang.MolangParser;
import net.minecraft.world.entity.Entity;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class MolangRenderControllerParser {
    private MolangRenderControllerParser() {}

    private static final Gson GSON = new Gson();
    private static final int MAX_RESOLUTION_DEPTH = 16;


    public static String ResolveGeometry(Entity Entity, String Expression, Object Arrays) {
        return ResolveResource(Entity, Expression, Arrays);
    }

    public static List<String> ResolveTextures(Entity Entity, Object Textures, Object Arrays) {
        List<String> Result = new ArrayList<>();

        JsonElement TextureElement = ToJsonElement(Textures);

        if (TextureElement == null || TextureElement.isJsonNull()) {
            return Result;
        }

        if (TextureElement.isJsonArray()) {
            for (JsonElement Texture : TextureElement.getAsJsonArray()) {
                Result.add(ResolveResource(Entity, GetString(Texture), Arrays));
            }
        } else if (TextureElement.isJsonPrimitive()) {
            Result.add(ResolveResource(Entity, GetString(TextureElement), Arrays));
        }

        return Result;
    }

    public static String ResolveMaterial(Entity Entity, String Expression, Object Arrays) {
        return ResolveResource(Entity, Expression, Arrays);
    }

    public static List<Map<String, String>> ResolveMaterials(Entity Entity, Object Materials, Object Arrays) {
        List<Map<String, String>> Result = new ArrayList<>();

        JsonElement MaterialElement = ToJsonElement(Materials);

        if (MaterialElement == null || MaterialElement.isJsonNull()) {
            return Result;
        }

        if (MaterialElement.isJsonArray()) {
            for (JsonElement Material : MaterialElement.getAsJsonArray()) {
                AddMaterial(Entity, Result, Material, Arrays);
            }
        } else if (MaterialElement.isJsonObject()) {
            AddMaterial(Entity, Result, MaterialElement, Arrays);
        }

        return Result;
    }

    private static void AddMaterial(Entity Entity, List<Map<String, String>> Result, JsonElement Material, Object Arrays) {
        if (!Material.isJsonObject()) {return;}

        Map<String, String> ResolvedMaterials = new LinkedHashMap<>();

        for (Map.Entry<String, JsonElement> Entry : Material.getAsJsonObject().entrySet()) {
            String Expression = GetString(Entry.getValue());

            ResolvedMaterials.put(Entry.getKey(), ResolveMaterial(Entity, Expression, Arrays));
        }

        Result.add(ResolvedMaterials);
    }

    public static String ResolveResource(Entity Entity, String Expression, Object Arrays) {
        if (Entity == null || Expression == null || Expression.isBlank()) {return "0";}

        JsonElement ArrayDefinitions = ToJsonElement(Arrays);

        return ResolveResourceInternal(Entity, Expression.trim(), ArrayDefinitions, 0);
    }

    private static String ResolveResourceInternal(Entity Entity, String Expression, JsonElement Arrays, int Depth) {
        if (Depth > MAX_RESOLUTION_DEPTH || Expression == null || Expression.isBlank()) {return "0";}

        String PreparedExpression = PrepareExpression(Entity, Expression, Arrays, Depth);
        String Result = MolangParser.Evaluate(Entity, PreparedExpression);

        return Result != null ? Result.trim() : "0";
    }

    private static String PrepareExpression(Entity Entity, String Expression, JsonElement Arrays, int Depth) {
        StringBuilder Result = new StringBuilder();

        char Quote = 0;
        boolean Escaped = false;

        for (int Index = 0; Index < Expression.length(); Index++) {
            char Character = Expression.charAt(Index);

            if (Quote != 0) {
                Result.append(Character);

                if (Escaped) {
                    Escaped = false;
                } else if (Character == '\\') {
                    Escaped = true;
                } else if (Character == Quote) {
                    Quote = 0;
                }

                continue;
            }

            if (Character == '\'' || Character == '"') {
                Quote = Character;
                Result.append(Character);
                continue;
            }

            if (MatchesPrefix(Expression, Index, "Math.")) {
                Result.append("math.");

                Index += 4;
                continue;
            }

            if (MatchesPrefix(Expression, Index, "Array.")) {
                int NameStart = Index + 6;
                int NameEnd = FindIdentifierEnd(Expression, NameStart);

                if (NameEnd == NameStart) {
                    Result.append(Character);
                    continue;
                }

                int OpenIndex = NameEnd;

                while (OpenIndex < Expression.length()
                        && java.lang.Character.isWhitespace(Expression.charAt(OpenIndex))) {
                    OpenIndex++;
                }

                if (OpenIndex >= Expression.length() || Expression.charAt(OpenIndex) != '[') {
                    Result.append(Expression, Index, NameEnd);

                    Index = NameEnd - 1;
                    continue;
                }

                int ClosingIndex = FindMatchingSquareBracket(Expression, OpenIndex);

                if (ClosingIndex < 0) {
                    Result.append(QuoteResource("0"));
                    break;
                }

                String ArrayName = Expression.substring(Index, NameEnd);
                String IndexExpression = Expression.substring(OpenIndex + 1, ClosingIndex);
                String ArrayValue = ResolveArray(Entity, ArrayName, IndexExpression, Arrays, Depth);

                Result.append(QuoteResource(ArrayValue));

                Index = ClosingIndex;
                continue;
            }


            if (MatchesPrefix(Expression, Index, "Geometry.") || MatchesPrefix(Expression, Index, "Texture.") || MatchesPrefix(Expression, Index, "Material.")) {

                int PrefixEnd = Expression.indexOf('.', Index) + 1;
                int NameEnd = FindIdentifierEnd(Expression, PrefixEnd);

                if (NameEnd > PrefixEnd) {
                    String Resource = Expression.substring(Index, NameEnd);

                    Result.append(QuoteResource(Resource));

                    Index = NameEnd - 1;
                    continue;
                }
            }

            Result.append(Character);
        }

        return Result.toString();
    }

    private static String ResolveArray(Entity Entity, String ArrayName, String IndexExpression, JsonElement Arrays, int Depth) {
        String NormalizedIndex = NormalizeMathPrefixes(IndexExpression);
        String IndexValue = MolangParser.Evaluate(Entity, NormalizedIndex);

        if (!MolangParser.IsNumber(IndexValue)) {
            return "0";
        }

        double NumericIndex;

        try {
            NumericIndex = Double.parseDouble(IndexValue);
        } catch (NumberFormatException Exception) {
            return "0";
        }

        if (!Double.isFinite(NumericIndex) || NumericIndex < Integer.MIN_VALUE || NumericIndex > Integer.MAX_VALUE) {
            return "0";
        }

        int Index = (int) Math.floor(NumericIndex);

        List<String> Values = FindArrayValues(Arrays, ArrayName);
        if (Values == null || Values.isEmpty()) {return "0";}

        Index = Math.floorMod(Index, Values.size());
        String Value = Values.get(Index);

        if (Value == null || Value.isBlank()) {return "0";}

        return ResolveResourceInternal(Entity, Value, Arrays, Depth + 1);
    }

    private static List<String> FindArrayValues(JsonElement Arrays, String ArrayName) {
        if (Arrays == null || Arrays.isJsonNull()) {return null;}

        if (Arrays.isJsonObject()) {
            JsonObject Object = Arrays.getAsJsonObject();

            String ShortName = ArrayName.regionMatches(true, 0, "Array.", 0, 6) ? ArrayName.substring(6) : ArrayName;

            for (Map.Entry<String, JsonElement> Entry : Object.entrySet()) {
                String Name = Entry.getKey();

                if (Name.equalsIgnoreCase(ArrayName) || Name.equalsIgnoreCase(ShortName)) {
                    return ReadStringArray(Entry.getValue());
                }
            }

            for (Map.Entry<String, JsonElement> Entry : Object.entrySet()) {
                List<String> Values = FindArrayValues(Entry.getValue(), ArrayName);

                if (Values != null) {
                    return Values;
                }
            }
        } else if (Arrays.isJsonArray()) {
            for (JsonElement Element : Arrays.getAsJsonArray()) {
                List<String> Values = FindArrayValues(Element, ArrayName);

                if (Values != null) {
                    return Values;
                }
            }
        }

        return null;
    }

    private static List<String> ReadStringArray(JsonElement Element) {
        if (Element == null || !Element.isJsonArray()) {return null;}

        List<String> Result = new ArrayList<>();

        for (JsonElement Value : Element.getAsJsonArray()) {
            Result.add(GetString(Value));
        }

        return Result;
    }

    private static boolean MatchesPrefix(String Expression, int Index, String Prefix) {
        if (Index + Prefix.length() > Expression.length() || !Expression.regionMatches(true, Index, Prefix, 0, Prefix.length())) {return false;}
        if (Index == 0) {return true;}

        char Previous = Expression.charAt(Index - 1);

        return !Character.isLetterOrDigit(Previous) && Previous != '_' && Previous != '.';
    }

    private static int FindIdentifierEnd(String Expression, int Start) {
        int Index = Start;

        while (Index < Expression.length()) {
            char Character = Expression.charAt(Index);

            if (!java.lang.Character.isLetterOrDigit(Character) && Character != '_') {
                break;
            }

            Index++;
        }

        return Index;
    }

    private static int FindMatchingSquareBracket(String Expression, int OpenIndex) {
        int Depth = 0;
        char Quote = 0;
        boolean Escaped = false;

        for (int Index = OpenIndex; Index < Expression.length(); Index++) {
            char Character = Expression.charAt(Index);

            if (Quote != 0) {
                if (Escaped) {
                    Escaped = false;
                } else if (Character == '\\') {
                    Escaped = true;
                } else if (Character == Quote) {
                    Quote = 0;
                }

                continue;
            }

            if (Character == '\'' || Character == '"') {
                Quote = Character;
                continue;
            }

            if (Character == '[') {
                Depth++;
            } else if (Character == ']') {
                Depth--;

                if (Depth == 0) {
                    return Index;
                }
            }
        }

        return -1;
    }

    private static String NormalizeMathPrefixes(String Expression) {
        StringBuilder Result = new StringBuilder();

        char Quote = 0;
        boolean Escaped = false;

        for (int Index = 0; Index < Expression.length(); Index++) {
            char Character = Expression.charAt(Index);

            if (Quote != 0) {
                Result.append(Character);

                if (Escaped) {
                    Escaped = false;
                } else if (Character == '\\') {
                    Escaped = true;
                } else if (Character == Quote) {
                    Quote = 0;
                }

                continue;
            }

            if (Character == '\'' || Character == '"') {
                Quote = Character;
                Result.append(Character);
                continue;
            }

            if (MatchesPrefix(Expression, Index, "Math.")) {
                Result.append("math.");

                Index += 4;
                continue;
            }

            Result.append(Character);
        }

        return Result.toString();
    }

    private static String QuoteResource(String Value) {
        if (Value == null) {
            Value = "0";
        }

        return "'" + Value.replace("\\", "\\\\").replace("'", "\\'") + "'";
    }

    private static JsonElement ToJsonElement(Object Value) {
        if (Value == null) {return null;}
        if (Value instanceof JsonElement Element) {return Element;}

        return GSON.toJsonTree(Value);
    }

    private static String GetString(JsonElement Element) {
        if (Element == null || Element.isJsonNull()) {return "";}

        if (Element.isJsonPrimitive()) {
            JsonPrimitive Primitive = Element.getAsJsonPrimitive();

            return Primitive.getAsString();
        }

        return "";
    }
}