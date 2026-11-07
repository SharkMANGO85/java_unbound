package com.java_unbound.loader.definitions.entity.resolvers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.java_unbound.JavaUnbound;
import com.java_unbound.loader.definitions.entity.types.geometry.EntityGeometryBone;
import com.java_unbound.loader.definitions.entity.types.geometry.EntityGeometryCube;
import com.java_unbound.loader.definitions.entity.types.geometry.EntityGeometryDefinition;
import com.java_unbound.loader.definitions.entity.types.geometry.EntityGeometryDescription;
import com.java_unbound.loader.definitions.entity.types.geometry.EntityGeometryDisplayTransform;
import com.java_unbound.loader.definitions.entity.types.geometry.EntityGeometryItemDisplayTransforms;
import com.java_unbound.loader.definitions.entity.types.geometry.EntityGeometryLocator;
import com.java_unbound.loader.definitions.entity.types.geometry.EntityGeometryPolyMesh;
import com.java_unbound.loader.definitions.entity.types.geometry.EntityGeometryPolyMeshPolys;
import com.java_unbound.loader.definitions.entity.types.geometry.EntityGeometryTextureMesh;
import com.java_unbound.loader.definitions.entity.types.geometry.EntityGeometryUvData;
import com.java_unbound.loader.definitions.entity.types.geometry.EntityGeometryUvFace;
import com.java_unbound.loader.resourcepack.Folder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class EntityGeometryResolver {
    private static HashMap<String, EntityGeometryDefinition> CachedAvailableGeometries;
    private static final HashMap<Path, HashMap<String, EntityGeometryDefinition>> CachedEntityGeometries = new HashMap<>();
    private static HashMap<String, JsonObject> CachedGeometryJson;

    public static List<Path> GetGeometryFolders() {
        Path ConfigFolder = Folder.GetConfigFolder();
        List<Path> Geometries = new ArrayList<>();

        if (ConfigFolder.resolve("subpacks").resolve(JavaUnbound.SUB_PACK).resolve("models").toFile().exists()) {
            Geometries.add(ConfigFolder.resolve("subpacks").resolve(JavaUnbound.SUB_PACK).resolve("models"));
        }

        if (ConfigFolder.resolve("models").resolve("entity").toFile().exists()) {
            Geometries.add(ConfigFolder.resolve("models").resolve("entity"));
        }

        return Geometries;
    }

    private static JsonObject ReadJson(Path JsonFile) {
        try {
            return JsonParser.parseString(Files.readString(JsonFile)).getAsJsonObject();
        } catch (Exception Exception) {
            JavaUnbound.LOGGER.error("Failed to read JSON file: {}", JsonFile, Exception);
            return null;
        }
    }

    private static HashMap<String, JsonObject> LoadGeometryJsonFiles() {
        HashMap<String, JsonObject> Geometries = new HashMap<>();

        for (Path GeometryFolder : GetGeometryFolders()) {
            try (Stream<Path> FilesStream = Files.walk(GeometryFolder)) {
                FilesStream
                        .filter(Files::isRegularFile)
                        .filter(File -> File.toString().endsWith(".json"))
                        .forEach(GeometryFile -> {
                            JsonObject Root = ReadJson(GeometryFile);
                            if (Root == null) {return;}

                            JsonElement GeometryElement = Root.get("minecraft:geometry");
                            if (GeometryElement == null || !GeometryElement.isJsonArray()) {return;}

                            for (JsonElement Element : GeometryElement.getAsJsonArray()) {
                                if (!Element.isJsonObject()) {continue;}

                                JsonObject GeometryObject = Element.getAsJsonObject();
                                String Identifier = GetGeometryIdentifier(GeometryObject);

                                if (Identifier == null || Identifier.isBlank()) {
                                    continue;
                                }

                                Geometries.putIfAbsent(Identifier, GeometryObject);
                            }
                        });
            } catch (IOException Exception) {
                JavaUnbound.LOGGER.error("Failed to search geometry folder: {}", GeometryFolder, Exception);
            }
        }

        JavaUnbound.LOGGER.debug("Loaded raw geometry JSON cache | Geometries: {}", Geometries.size());

        return Geometries;
    }

    public static synchronized JsonObject GetGeometryJson(String GeometryIdentifier) {
        if (GeometryIdentifier == null || GeometryIdentifier.isBlank()) {
            return null;
        }

        if (CachedGeometryJson == null) {
            CachedGeometryJson = LoadGeometryJsonFiles();
        }

        JsonObject Geometry = CachedGeometryJson.get(GeometryIdentifier);

        if (Geometry == null) {
            JavaUnbound.LOGGER.debug("Could not find geometry JSON: {}", GeometryIdentifier);
        }

        return Geometry;
    }

    private static JsonObject GetGeometryMappings(Path EntityFile) {
        JsonObject Root = ReadJson(EntityFile);
        if (Root == null) {return null;}

        JsonObject ClientEntity = ReadObject(Root, "minecraft:client_entity");
        if (ClientEntity == null) {
            JavaUnbound.LOGGER.error("No minecraft:client_entity found: {}", EntityFile);
            return null;
        }

        JsonObject Description = ReadObject(ClientEntity, "description");
        if (Description == null) {
            JavaUnbound.LOGGER.error("No entity description found: {}", EntityFile);
            return null;
        }

        JsonElement GeometryElement = Description.get("geometry");

        if (GeometryElement == null) {
            JavaUnbound.LOGGER.error("No geometry mappings found: {}", EntityFile);
            return null;
        }

        if (GeometryElement.isJsonPrimitive()) {
            JsonObject Wrapped = new JsonObject();
            Wrapped.add("default", GeometryElement);
            return Wrapped;
        }

        if (!GeometryElement.isJsonObject()) {
            JavaUnbound.LOGGER.error("Invalid geometry mappings: {}", EntityFile);
            return null;
        }

        return GeometryElement.getAsJsonObject();
    }

    private static String GetGeometryIdentifier(JsonObject GeometryObject) {
        if (GeometryObject == null) {return null;}

        JsonElement DescriptionElement = GeometryObject.get("description");
        if (DescriptionElement == null || !DescriptionElement.isJsonObject()) {return null;}

        JsonElement IdentifierElement = DescriptionElement.getAsJsonObject().get("identifier");
        if (IdentifierElement == null || !IdentifierElement.isJsonPrimitive()) {return null;}

        return IdentifierElement.getAsString();
    }

    private static EntityGeometryDefinition ParseGeometryDefinition(JsonObject Json) {
        EntityGeometryDefinition Definition = new EntityGeometryDefinition();

        JsonObject DescriptionObject = ReadObject(Json, "description");
        if (DescriptionObject != null) {
            Definition.Description = ParseDescription(DescriptionObject);
        }

        JsonArray BonesArray = ReadArray(Json, "bones");
        if (BonesArray != null) {
            for (JsonElement BoneElement : BonesArray) {
                if (!BoneElement.isJsonObject()) {continue;}
                Definition.Bones.add(ParseBone(BoneElement.getAsJsonObject()));
            }
        }

        JsonObject DisplayTransformsObject = ReadObject(Json, "item_display_transforms");
        if (DisplayTransformsObject != null) {
            Definition.ItemDisplayTransforms = ParseItemDisplayTransforms(DisplayTransformsObject);
        }

        return Definition;
    }

    private static EntityGeometryDescription ParseDescription(JsonObject Json) {
        EntityGeometryDescription Description = new EntityGeometryDescription();

        Description.Identifier = ReadString(Json, "identifier", Description.Identifier);
        Description.VisibleBoundsWidth = ReadFloat(Json, "visible_bounds_width", Description.VisibleBoundsWidth);
        Description.VisibleBoundsHeight = ReadFloat(Json, "visible_bounds_height", Description.VisibleBoundsHeight);
        Description.TextureWidth = ReadInt(Json, "texture_width", Description.TextureWidth);
        Description.TextureHeight = ReadInt(Json, "texture_height", Description.TextureHeight);
        Description.VisibleBoundsOffset = ReadFloatArray(Json.get("visible_bounds_offset"));

        return Description;
    }

    private static EntityGeometryBone ParseBone(JsonObject Json) {
        EntityGeometryBone Bone = new EntityGeometryBone();

        Bone.Name = ReadString(Json, "name", Bone.Name);
        Bone.Parent = ReadString(Json, "parent", null);
        Bone.Pivot = ReadFloatArray(Json.get("pivot"));
        Bone.Rotation = ReadFloatArray(Json.get("rotation"));
        Bone.BindPoseRotation = ReadFloatArray(Json.get("bind_pose_rotation"));
        Bone.Mirror = ReadBoolean(Json, "mirror", Bone.Mirror);
        Bone.Debug = ReadBoolean(Json, "debug", Bone.Debug);
        Bone.Binding = ReadString(Json, "binding", null);
        Bone.RenderGroupId = ReadInt(Json, "render_group_id", Bone.RenderGroupId);
        Bone.Reset = ReadBoolean(Json, "reset", Bone.Reset);
        Bone.NeverRender = ReadBoolean(Json, "neverrender", ReadBoolean(Json, "neverRender", Bone.NeverRender));

        JsonArray CubesArray = ReadArray(Json, "cubes");
        if (CubesArray != null) {
            for (JsonElement CubeElement : CubesArray) {
                if (!CubeElement.isJsonObject()) {continue;}
                Bone.Cubes.add(ParseCube(CubeElement.getAsJsonObject()));
            }
        }

        JsonObject LocatorsObject = ReadObject(Json, "locators");
        if (LocatorsObject != null) {
            for (Map.Entry<String, JsonElement> Entry : LocatorsObject.entrySet()) {
                EntityGeometryLocator Locator = ParseLocator(Entry.getValue());
                if (Locator != null) {
                    Bone.Locators.put(Entry.getKey(), Locator);
                }
            }
        }

        JsonObject PolyMeshObject = ReadObject(Json, "poly_mesh");
        if (PolyMeshObject != null) {
            Bone.PolyMesh = ParsePolyMesh(PolyMeshObject);
        }

        JsonArray TextureMeshesArray = ReadArray(Json, "texture_meshes");
        if (TextureMeshesArray != null) {
            for (JsonElement TextureMeshElement : TextureMeshesArray) {
                if (!TextureMeshElement.isJsonObject()) {continue;}
                Bone.TextureMeshes.add(ParseTextureMesh(TextureMeshElement.getAsJsonObject()));
            }
        }

        return Bone;
    }

    private static EntityGeometryCube ParseCube(JsonObject Json) {
        EntityGeometryCube Cube = new EntityGeometryCube();

        Cube.Origin = ReadFloatList(Json.get("origin"));
        Cube.Size = ReadFloatList(Json.get("size"));
        Cube.Pivot = ReadFloatList(Json.get("pivot"));
        Cube.Rotation = ReadFloatList(Json.get("rotation"));
        Cube.Inflate = ReadNullableFloat(Json, "inflate");
        Cube.Mirror = ReadNullableBoolean(Json, "mirror");
        Cube.UV = ParseUvData(Json.get("uv"));

        return Cube;
    }

    /** "uv" is either [u, v] (box UV) or an object with per-face entries. */
    private static EntityGeometryUvData ParseUvData(JsonElement Json) {
        if (Json == null || Json.isJsonNull()) {return null;}

        EntityGeometryUvData UvData = new EntityGeometryUvData();

        if (Json.isJsonArray()) {
            UvData.Uv = ReadFloatArray(Json);
            return UvData;
        }

        if (!Json.isJsonObject()) {return null;}

        JsonObject Object = Json.getAsJsonObject();

        UvData.Uv = ReadFloatArray(Object.get("uv"));
        UvData.North = ParseUvFace(Object.get("north"));
        UvData.South = ParseUvFace(Object.get("south"));
        UvData.East = ParseUvFace(Object.get("east"));
        UvData.West = ParseUvFace(Object.get("west"));
        UvData.Up = ParseUvFace(Object.get("up"));
        UvData.Down = ParseUvFace(Object.get("down"));

        return UvData;
    }

    private static EntityGeometryUvFace ParseUvFace(JsonElement Json) {
        if (Json == null || !Json.isJsonObject()) {return null;}

        JsonObject Object = Json.getAsJsonObject();
        EntityGeometryUvFace Face = new EntityGeometryUvFace();

        Face.Uv = ReadFloatArray(Object.get("uv"));
        Face.UvSize = ReadFloatArray(Object.get("uv_size"));
        Face.UvRotation = ReadInt(Object, "uv_rotation", Face.UvRotation);
        Face.MaterialInstance = ReadString(Object, "material_instance", null);

        return Face;
    }

    /** A locator is either [x, y, z] or an object with offset / rotation / ignore_inherited_scale. */
    private static EntityGeometryLocator ParseLocator(JsonElement Json) {
        if (Json == null || Json.isJsonNull()) {return null;}

        EntityGeometryLocator Locator = new EntityGeometryLocator();

        if (Json.isJsonArray()) {
            Locator.Offset = ReadFloatArray(Json);
            return Locator;
        }

        if (!Json.isJsonObject()) {return null;}

        JsonObject Object = Json.getAsJsonObject();

        Locator.Offset = ReadFloatArray(Object.get("offset"));
        Locator.Rotation = ReadFloatArray(Object.get("rotation"));
        Locator.IgnoreInheritedScale = ReadBoolean(Object, "ignore_inherited_scale", Locator.IgnoreInheritedScale);

        return Locator;
    }

    private static EntityGeometryPolyMesh ParsePolyMesh(JsonObject Json) {
        EntityGeometryPolyMesh PolyMesh = new EntityGeometryPolyMesh();

        PolyMesh.NormalizedUvs = ReadBoolean(Json, "normalized_uvs", PolyMesh.NormalizedUvs);
        PolyMesh.Positions = ReadFloatMatrix(Json.get("positions"));
        PolyMesh.Normals = ReadFloatMatrix(Json.get("normals"));
        PolyMesh.Uvs = ReadFloatMatrix(Json.get("uvs"));
        PolyMesh.Polys = ParsePolyMeshPolys(Json.get("polys"));

        return PolyMesh;
    }

    /** "polys" is either "tri_list" / "quad_list" or an explicit [polygon][vertex][position, normal, uv] index array. */
    private static EntityGeometryPolyMeshPolys ParsePolyMeshPolys(JsonElement Json) {
        if (Json == null || Json.isJsonNull()) {return null;}

        EntityGeometryPolyMeshPolys Polys = new EntityGeometryPolyMeshPolys();

        if (Json.isJsonPrimitive() && Json.getAsJsonPrimitive().isString()) {
            Polys.PolyType = Json.getAsString();
            return Polys;
        }

        if (Json.isJsonArray()) {
            Polys.Polygons = ReadIntCube(Json);
            return Polys;
        }

        return null;
    }

    private static EntityGeometryTextureMesh ParseTextureMesh(JsonObject Json) {
        EntityGeometryTextureMesh TextureMesh = new EntityGeometryTextureMesh();

        TextureMesh.Texture = ReadString(Json, "texture", TextureMesh.Texture);
        TextureMesh.Position = ReadFloatArray(Json.get("position"));
        TextureMesh.Rotation = ReadFloatArray(Json.get("rotation"));
        TextureMesh.Scale = ReadFloatArray(Json.get("scale"));
        TextureMesh.LocalPivot = ReadFloatArray(Json.get("local_pivot"));
        TextureMesh.UsePixelDepth = ReadBoolean(Json, "use_pixel_depth", TextureMesh.UsePixelDepth);

        return TextureMesh;
    }

    private static EntityGeometryItemDisplayTransforms ParseItemDisplayTransforms(JsonObject Json) {
        EntityGeometryItemDisplayTransforms Transforms = new EntityGeometryItemDisplayTransforms();

        Transforms.Gui = ParseDisplayTransform(Json.get("gui"));
        Transforms.FirstpersonRighthand = ParseDisplayTransform(Json.get("firstperson_righthand"));
        Transforms.FirstpersonLefthand = ParseDisplayTransform(Json.get("firstperson_lefthand"));
        Transforms.ThirdpersonRighthand = ParseDisplayTransform(Json.get("thirdperson_righthand"));
        Transforms.ThirdpersonLefthand = ParseDisplayTransform(Json.get("thirdperson_lefthand"));
        Transforms.Ground = ParseDisplayTransform(Json.get("ground"));
        Transforms.Fixed = ParseDisplayTransform(Json.get("fixed"));
        Transforms.Head = ParseDisplayTransform(Json.get("head"));
        Transforms.Embedded = ParseDisplayTransform(Json.get("embedded"));
        Transforms.Shelf = ParseDisplayTransform(Json.get("shelf"));

        return Transforms;
    }

    private static EntityGeometryDisplayTransform ParseDisplayTransform(JsonElement Json) {
        if (Json == null || !Json.isJsonObject()) {return null;}

        JsonObject Object = Json.getAsJsonObject();
        EntityGeometryDisplayTransform Transform = new EntityGeometryDisplayTransform();

        Transform.Rotation = ReadFloatArray(Object.get("rotation"));
        Transform.RotationPivot = ReadFloatArray(Object.get("rotation_pivot"));
        Transform.Scale = ReadFloatArray(Object.get("scale"));
        Transform.ScalePivot = ReadFloatArray(Object.get("scale_pivot"));
        Transform.Translation = ReadFloatArray(Object.get("translation"));
        Transform.FitToFrame = ReadBoolean(Object, "fit_to_frame", Transform.FitToFrame);

        return Transform;
    }

    // ----------------------------- JSON readers -----------------------------

    private static JsonObject ReadObject(JsonObject Parent, String Key) {
        JsonElement Element = Parent.get(Key);
        return Element != null && Element.isJsonObject() ? Element.getAsJsonObject() : null;
    }

    private static JsonArray ReadArray(JsonObject Parent, String Key) {
        JsonElement Element = Parent.get(Key);
        return Element != null && Element.isJsonArray() ? Element.getAsJsonArray() : null;
    }

    private static String ReadString(JsonObject Parent, String Key, String Default) {
        JsonElement Element = Parent.get(Key);
        return Element != null && Element.isJsonPrimitive() ? Element.getAsString() : Default;
    }

    private static boolean ReadBoolean(JsonObject Parent, String Key, boolean Default) {
        Boolean Value = ReadNullableBoolean(Parent, Key);
        return Value != null ? Value : Default;
    }

    private static Boolean ReadNullableBoolean(JsonObject Parent, String Key) {
        JsonElement Element = Parent.get(Key);
        if (Element == null || !Element.isJsonPrimitive()) {return null;}

        JsonPrimitive Primitive = Element.getAsJsonPrimitive();
        if (Primitive.isBoolean()) {return Primitive.getAsBoolean();}
        if (Primitive.isString()) {
            String Text = Primitive.getAsString();
            if (Text.equalsIgnoreCase("true")) {return true;}
            if (Text.equalsIgnoreCase("false")) {return false;}
        }

        return null;
    }

    private static int ReadInt(JsonObject Parent, String Key, int Default) {
        JsonElement Element = Parent.get(Key);
        if (Element == null || !Element.isJsonPrimitive()) {return Default;}

        try {
            return Element.getAsJsonPrimitive().isNumber() ? Element.getAsNumber().intValue() : Integer.parseInt(Element.getAsString().trim());
        } catch (NumberFormatException Exception) {
            return Default;
        }
    }

    private static float ReadFloat(JsonObject Parent, String Key, float Default) {
        Float Value = ReadNullableFloat(Parent, Key);
        return Value != null ? Value : Default;
    }

    private static Float ReadNullableFloat(JsonObject Parent, String Key) {
        JsonElement Element = Parent.get(Key);
        return ReadFloatValue(Element);
    }

    private static Float ReadFloatValue(JsonElement Element) {
        if (Element == null || !Element.isJsonPrimitive()) {return null;}

        try {
            return Element.getAsJsonPrimitive().isNumber() ? Element.getAsNumber().floatValue() : Float.parseFloat(Element.getAsString().trim());
        } catch (NumberFormatException Exception) {
            return null;
        }
    }

    /** Returns null if the element is not an array or any entry is not a number. */
    private static float[] ReadFloatArray(JsonElement Element) {
        if (Element == null || !Element.isJsonArray()) {return null;}

        JsonArray Array = Element.getAsJsonArray();
        float[] Values = new float[Array.size()];

        for (int Index = 0; Index < Values.length; Index++) {
            Float Value = ReadFloatValue(Array.get(Index));
            if (Value == null) {return null;}
            Values[Index] = Value;
        }

        return Values;
    }

    private static List<Float> ReadFloatList(JsonElement Element) {
        float[] Values = ReadFloatArray(Element);
        if (Values == null) {return null;}

        List<Float> Result = new ArrayList<>(Values.length);
        for (float Value : Values) {Result.add(Value);}

        return Result;
    }

    private static float[][] ReadFloatMatrix(JsonElement Element) {
        if (Element == null || !Element.isJsonArray()) {return null;}

        JsonArray Array = Element.getAsJsonArray();
        float[][] Rows = new float[Array.size()][];

        for (int Index = 0; Index < Rows.length; Index++) {
            Rows[Index] = ReadFloatArray(Array.get(Index));
            if (Rows[Index] == null) {return null;}
        }

        return Rows;
    }

    private static int[] ReadIntArray(JsonElement Element) {
        if (Element == null || !Element.isJsonArray()) {return null;}

        JsonArray Array = Element.getAsJsonArray();
        int[] Values = new int[Array.size()];

        for (int Index = 0; Index < Values.length; Index++) {
            Float Value = ReadFloatValue(Array.get(Index));
            if (Value == null) {return null;}
            Values[Index] = Value.intValue();
        }

        return Values;
    }

    private static int[][][] ReadIntCube(JsonElement Element) {
        if (Element == null || !Element.isJsonArray()) {return null;}

        JsonArray Polygons = Element.getAsJsonArray();
        int[][][] Result = new int[Polygons.size()][][];

        for (int PolygonIndex = 0; PolygonIndex < Result.length; PolygonIndex++) {
            JsonElement PolygonElement = Polygons.get(PolygonIndex);
            if (!PolygonElement.isJsonArray()) {return null;}

            JsonArray Vertices = PolygonElement.getAsJsonArray();
            Result[PolygonIndex] = new int[Vertices.size()][];

            for (int VertexIndex = 0; VertexIndex < Vertices.size(); VertexIndex++) {
                Result[PolygonIndex][VertexIndex] = ReadIntArray(Vertices.get(VertexIndex));
                if (Result[PolygonIndex][VertexIndex] == null) {return null;}
            }
        }

        return Result;
    }

    private static EntityGeometryDefinition ParseGeometry(JsonObject GeometryObject, Path GeometryFile) {
        if (GeometryObject == null) {return null;}

        String GeometryIdentifier = GetGeometryIdentifier(GeometryObject);

        try {
            EntityGeometryDefinition Definition = ParseGeometryDefinition(GeometryObject);

            if (Definition == null || Definition.Description == null || Definition.Bones == null) {
                JavaUnbound.LOGGER.error("Invalid geometry definition: {} | File: {}", GeometryIdentifier, GeometryFile);
                return null;
            }

            Definition.Identifier = GeometryIdentifier;

            return Definition;
        } catch (Exception Exception) {
            JavaUnbound.LOGGER.error("Failed to deserialize geometry: {} | File: {}", GeometryIdentifier, GeometryFile, Exception);
            return null;
        }
    }

    private static HashMap<String, EntityGeometryDefinition> LoadGeometryFiles() {
        HashMap<String, EntityGeometryDefinition> Geometries = new HashMap<>();

        List<Path> GeometryFolders = GetGeometryFolders();

        if (GeometryFolders.isEmpty()) {
            JavaUnbound.LOGGER.error("No geometry folders exist.");
            return Geometries;
        }

        for (Path GeometryFolder : GeometryFolders) {
            LoadGeometryFolder(GeometryFolder, Geometries);
        }

        JavaUnbound.LOGGER.debug("Finished geometry search | Found: {}", Geometries.size());
        return Geometries;
    }

    private static void LoadGeometryFolder(Path GeometryFolder, HashMap<String, EntityGeometryDefinition> Geometries) {
        if (!Files.isDirectory(GeometryFolder)) {
            JavaUnbound.LOGGER.error("Geometry folder does not exist: {}", GeometryFolder);
            return;
        }

        JavaUnbound.LOGGER.debug("Searching geometry files in: {}", GeometryFolder);

        try (Stream<Path> FilesStream = Files.walk(GeometryFolder)) {
            FilesStream
                    .filter(Files::isRegularFile)
                    .filter(File -> File.toString().endsWith(".json"))
                    .forEach(GeometryFile -> {
                        JsonObject Root = ReadJson(GeometryFile);
                        if (Root == null) {return;}

                        JsonElement GeometryElement = Root.get("minecraft:geometry");
                        if (GeometryElement == null || !GeometryElement.isJsonArray()) {return;}

                        for (JsonElement Element : GeometryElement.getAsJsonArray()) {
                            if (!Element.isJsonObject()) {continue;}

                            JsonObject GeometryObject = Element.getAsJsonObject();
                            String Identifier = GetGeometryIdentifier(GeometryObject);

                            if (Identifier == null || Identifier.isBlank()) {continue;}

                            if (Geometries.containsKey(Identifier)) {
                                JavaUnbound.LOGGER.debug("Skipping geometry (already loaded from higher priority folder): {} | File: {}", Identifier, GeometryFile);
                                continue;
                            }

                            EntityGeometryDefinition Definition = ParseGeometry(GeometryObject, GeometryFile);
                            if (Definition == null) {continue;}

                            Geometries.put(Identifier, Definition);

                            JavaUnbound.LOGGER.debug("Found geometry: {} | File: {} | Bones: {}", Identifier, GeometryFile, Definition.Bones.size());
                        }
                    });
        } catch (IOException Exception) {
            JavaUnbound.LOGGER.error("Failed to search geometry files: {}", GeometryFolder, Exception);
        }
    }

    private static synchronized HashMap<String, EntityGeometryDefinition> GetAvailableGeometries() {
        if (CachedAvailableGeometries == null) {
            JavaUnbound.LOGGER.debug("Loading geometry cache...");

            CachedAvailableGeometries = LoadGeometryFiles();

            JavaUnbound.LOGGER.debug("Geometry cache loaded | Geometries: {}", CachedAvailableGeometries.size());
        }

        return CachedAvailableGeometries;
    }

    private static HashMap<String, EntityGeometryDefinition> ResolveEntityGeometries(Path EntityFile) {
        Path CacheKey = EntityFile.toAbsolutePath().normalize();

        HashMap<String, EntityGeometryDefinition> CachedGeometries = CachedEntityGeometries.get(CacheKey);

        if (CachedGeometries != null) {
            return CachedGeometries;
        }

        JavaUnbound.LOGGER.debug("Resolving entity geometries from: {}", EntityFile);

        JsonObject GeometryMappings = GetGeometryMappings(EntityFile);

        HashMap<String, EntityGeometryDefinition> Geometries = new HashMap<>();

        if (GeometryMappings == null) {
            CachedEntityGeometries.put(CacheKey, Geometries);
            return Geometries;
        }

        HashMap<String, EntityGeometryDefinition> AvailableGeometries = GetAvailableGeometries();

        for (Map.Entry<String, JsonElement> Entry : GeometryMappings.entrySet()) {
            String GeometryAlias = Entry.getKey();
            JsonElement IdentifierElement = Entry.getValue();

            if (!IdentifierElement.isJsonPrimitive()) {continue;}

            String GeometryIdentifier = IdentifierElement.getAsString();
            EntityGeometryDefinition Definition = AvailableGeometries.get(GeometryIdentifier);

            if (Definition == null) {
                JavaUnbound.LOGGER.debug("Could not resolve geometry: {} | Alias: {} | Entity: {}", GeometryIdentifier, GeometryAlias, EntityFile);
                continue;
            }

            Geometries.put(GeometryAlias, Definition);
            JavaUnbound.LOGGER.debug("Resolved geometry: {} -> {} | Bones: {}", GeometryAlias, GeometryIdentifier, Definition.Bones.size());
        }

        JavaUnbound.LOGGER.debug("Finished resolving entity geometries: {} | Resolved: {}/{}", EntityFile, Geometries.size(), GeometryMappings.size());
        CachedEntityGeometries.put(CacheKey, Geometries);

        return Geometries;
    }

    public static synchronized HashMap<String, EntityGeometryDefinition> GetEntityGeometries(Path EntityFile) {
        return new HashMap<>(ResolveEntityGeometries(EntityFile));
    }

    public static synchronized EntityGeometryDefinition GetEntityGeometry(Path EntityFile, String GeometryAlias) {
        if (GeometryAlias == null || GeometryAlias.isBlank()) {
            JavaUnbound.LOGGER.error("Cannot resolve geometry: alias is null or blank.");
            return null;
        }

        HashMap<String, EntityGeometryDefinition> Geometries = ResolveEntityGeometries(EntityFile);

        EntityGeometryDefinition Definition = Geometries.get(GeometryAlias);

        if (Definition == null) {
            JavaUnbound.LOGGER.debug("Geometry alias not found: {} | Entity: {}", GeometryAlias, EntityFile);
        }

        return Definition;
    }

    public static synchronized void ClearCache() {
        CachedAvailableGeometries = null;
        CachedGeometryJson = null;
        CachedEntityGeometries.clear();

        JavaUnbound.LOGGER.debug("Cleared entity geometry cache.");
    }
}