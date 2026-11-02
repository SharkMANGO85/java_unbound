package com.java_unbound.loader.definitions.entity.reolvers;

import com.java_unbound.JavaUnbound;
import com.java_unbound.loader.definitions.entity.EntityDefinition;
import com.java_unbound.loader.definitions.entity.types.geometry.EntityGeometryDefinition;
import com.java_unbound.loader.definitions.entity.types.render_controller.EntityRenderControllerArrays;
import com.java_unbound.loader.definitions.entity.types.render_controller.EntityRenderControllerDefinition;
import com.java_unbound.loader.definitions.entity.types.render_controller.EntityRenderControllerReference;
import com.java_unbound.loader.definitions.entity.types.render_controller.EntityRenderPass;
import com.java_unbound.loader.definitions.entity.types.texture.EntityTextureDefinition;
import com.java_unbound.molang.MolangParser;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class EntityRenderPassResolver {
    private static final Pattern ArrayReferencePattern = Pattern.compile(
            "(?i)\\bArray\\.([A-Za-z0-9_]+)\\s*\\[([^\\]]+)\\]"
    );

    private static final Pattern IdentifierReferencePattern = Pattern.compile(
            "(?i)\\b(?:Geometry|Texture|Material)\\.([A-Za-z0-9_]+)\\b"
    );

    private EntityRenderPassResolver() {
    }

    public static List<EntityRenderPass> GetActiveRenderPasses(Entity Entity) {
        if (Entity == null) {
            return List.of();
        }

        EntityDefinition Definition = EntityDefinition.GetEntityDefinition(Entity);

        if (Definition == null || Definition.RenderControllers == null) {
            return List.of();
        }

        LinkedHashSet<EntityRenderPass> Passes = new LinkedHashSet<>();

        for (EntityRenderControllerReference Reference : Definition.RenderControllers) {
            if (Reference == null || Reference.Definition == null) {
                continue;
            }

            if (!MolangParser.IsConditionMet(Entity, Reference.Condition)) {
                continue;
            }

            EntityRenderControllerDefinition Controller = Reference.Definition;

            String GeometryAlias = EvaluateExpression(
                    Entity,
                    Controller.Arrays,
                    Controller.Geometry
            );

            if (GeometryAlias == null || Definition.Geometries == null) {
                continue;
            }

            EntityGeometryDefinition Geometry = Definition.Geometries.get(GeometryAlias);

            if (Geometry == null || Geometry.Identifier == null) {
                JavaUnbound.LOGGER.warn(
                        "Could not resolve geometry alias: {} | Controller: {}",
                        GeometryAlias,
                        Reference.Identifier
                );

                continue;
            }

            if (Controller.Textures == null) {
                continue;
            }

            for (String TextureExpression : Controller.Textures) {
                String TextureAlias = EvaluateExpression(
                        Entity,
                        Controller.Arrays,
                        TextureExpression
                );

                if (TextureAlias == null || Definition.Textures == null) {
                    continue;
                }

                EntityTextureDefinition Texture = Definition.Textures.get(TextureAlias);

                if (Texture == null || Texture.ResourcePath == null) {
                    JavaUnbound.LOGGER.warn(
                            "Could not resolve texture alias: {} | Controller: {}",
                            TextureAlias,
                            Reference.Identifier
                    );

                    continue;
                }

                Identifier TextureIdentifier = Identifier.fromNamespaceAndPath(
                        "minecraft",
                        Texture.ResourcePath
                );

                Passes.add(new EntityRenderPass(
                        Geometry.Identifier,
                        TextureIdentifier
                ));
            }
        }

        return new ArrayList<>(Passes);
    }

    private static String EvaluateExpression(
            Entity Entity,
            EntityRenderControllerArrays Arrays,
            String Expression
    ) {
        if (Expression == null || Expression.isBlank()) {
            return null;
        }

        String ExpandedExpression = ResolveArrayReferences(
                Entity,
                Arrays,
                Expression,
                0
        );

        if (ExpandedExpression == null) {
            return null;
        }

        Matcher Matcher = IdentifierReferencePattern.matcher(ExpandedExpression);
        StringBuffer ResolvedExpression = new StringBuffer();

        while (Matcher.find()) {
            String Alias = Matcher.group(1);
            String Replacement = "\"" + Alias + "\"";

            Matcher.appendReplacement(
                    ResolvedExpression,
                    Matcher.quoteReplacement(Replacement)
            );
        }

        Matcher.appendTail(ResolvedExpression);

        String Result = MolangParser.Evaluate(
                Entity,
                ResolvedExpression.toString()
        );

        if (Result == null || Result.isBlank() || Result.equals("0")) {
            return null;
        }

        return Result;
    }

    private static String ResolveArrayReferences(
            Entity Entity,
            EntityRenderControllerArrays Arrays,
            String Expression,
            int Depth
    ) {
        if (Expression == null || Depth > 16) {
            return null;
        }

        Matcher Matcher = ArrayReferencePattern.matcher(Expression);

        if (!Matcher.find()) {
            return Expression;
        }

        StringBuffer Result = new StringBuffer();

        do {
            String ArrayName = Matcher.group(1);
            String IndexExpression = ResolveArrayReferences(
                    Entity,
                    Arrays,
                    Matcher.group(2),
                    Depth + 1
            );

            if (IndexExpression == null) {
                return null;
            }

            String IndexValue = MolangParser.Evaluate(Entity, IndexExpression);

            if (!MolangParser.IsNumber(IndexValue)) {
                return null;
            }

            int Index;

            try {
                Index = (int) Double.parseDouble(IndexValue);
            } catch (NumberFormatException Exception) {
                return null;
            }

            List<String> Values = FindArray(Arrays, ArrayName);

            if (Values == null || Index < 0 || Index >= Values.size()) {
                return null;
            }

            String ArrayValue = ResolveArrayReferences(
                    Entity,
                    Arrays,
                    Values.get(Index),
                    Depth + 1
            );

            if (ArrayValue == null) {
                return null;
            }

            Matcher.appendReplacement(
                    Result,
                    Matcher.quoteReplacement(ArrayValue)
            );

        } while (Matcher.find());

        Matcher.appendTail(Result);

        return Result.toString();
    }

    private static List<String> FindArray(
            EntityRenderControllerArrays Arrays,
            String Name
    ) {
        if (Arrays == null) {
            return null;
        }

        List<String> Values = FindArray(Arrays.Geometries, Name);

        if (Values != null) {
            return Values;
        }

        Values = FindArray(Arrays.Textures, Name);

        if (Values != null) {
            return Values;
        }

        return FindArray(Arrays.Materials, Name);
    }

    private static List<String> FindArray(
            Map<String, List<String>> Arrays,
            String Name
    ) {
        if (Arrays == null) {
            return null;
        }

        List<String> Values = Arrays.get(Name);

        if (Values == null) {
            Values = Arrays.get("Array." + Name);
        }

        if (Values != null) {
            return Values;
        }

        for (Map.Entry<String, List<String>> Entry : Arrays.entrySet()) {
            if (Entry.getKey().equalsIgnoreCase(Name)
                    || Entry.getKey().equalsIgnoreCase("Array." + Name)) {
                return Entry.getValue();
            }
        }

        return null;
    }
}