package com.java_unbound.attachables.rendering;

import com.java_unbound.JavaUnbound;
import com.java_unbound.attachables.rendering.BedrockBone.ModelQuad;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

final class BedrockTextureMeshes {
    private BedrockTextureMeshes() {}

    private static final int MaxPixels = 128 * 128;
    private static final Map<Identifier, Optional<List<ModelQuad>>> Cache = new ConcurrentHashMap<>();

    static List<ModelQuad> Get(Identifier Texture) {
        return Cache.computeIfAbsent(Texture, Location -> {
            try {
                return Optional.of(Build(Location));
            } catch (Exception Exception) {
                JavaUnbound.LOGGER.error("Failed to build texture mesh for {}", Location, Exception);
                return Optional.<List<ModelQuad>>empty();
            }
        }).orElse(null);
    }

    static void ClearCache() {
        Cache.clear();
    }

    private static List<ModelQuad> Build(Identifier Texture) throws Exception {
        Resource Res = Minecraft.getInstance().getResourceManager().getResource(Texture).orElseThrow(() -> new IllegalStateException("Texture not found: " + Texture));

        try (InputStream Stream = Res.open(); NativeImage Image = NativeImage.read(Stream)) {
            int W = Image.getWidth();
            int H = Image.getHeight();

            if (W * H > MaxPixels) {
                throw new IllegalStateException("Texture " + Texture + " is too large for a texture mesh (" + W + "x" + H + ")");
            }

            boolean[][] Opaque = new boolean[W][H];

            for (int X = 0; X < W; X++) {
                for (int Y = 0; Y < H; Y++) {
                    Opaque[X][Y] = ((Image.getPixel(X, Y) >>> 24) & 0xFF) > 0;
                }
            }

            List<ModelQuad> Out = new ArrayList<>();
            float HalfW = W / 2F;
            float HalfH = H / 2F;
            float Z0 = -0.5F;
            float Z1 = 0.5F;

            for (int Y = 0; Y < H; Y++) {
                for (int X = 0; X < W; X++) {
                    if (!Opaque[X][Y]) {continue;}

                    float X1 = HalfW - X;
                    float X0 = X1 - 1F;
                    float Y1 = HalfH - Y;
                    float Y0 = Y1 - 1F;

                    float UL = X / (float) W, UR = (X + 1) / (float) W;
                    float VT = Y / (float) H, VB = (Y + 1) / (float) H;
                    float UC = (X + 0.5F) / W, VC = (Y + 0.5F) / H;
                    float[] Center = {UC, VC, UC, VC, UC, VC, UC, VC};

                    Add(Out, 0, 0, -1, new float[]{UL, VT, UL, VB, UR, VB, UR, VT}, X1, Y1, Z0, X1, Y0, Z0, X0, Y0, Z0, X0, Y1, Z0);
                    Add(Out, 0, 0, 1, new float[]{UR, VT, UR, VB, UL, VB, UL, VT}, X0, Y1, Z1, X0, Y0, Z1, X1, Y0, Z1, X1, Y1, Z1);

                    if (Y == 0 || !Opaque[X][Y - 1]) {
                        Add(Out, 0, 1, 0, Center, X0, Y1, Z0, X0, Y1, Z1, X1, Y1, Z1, X1, Y1, Z0);
                    }

                    if (Y == H - 1 || !Opaque[X][Y + 1]) {
                        Add(Out, 0, -1, 0, Center, X0, Y0, Z1, X0, Y0, Z0, X1, Y0, Z0, X1, Y0, Z1);
                    }

                    if (X == 0 || !Opaque[X - 1][Y]) {
                        Add(Out, 1, 0, 0, Center, X1, Y1, Z1, X1, Y0, Z1, X1, Y0, Z0, X1, Y1, Z0);
                    }

                    if (X == W - 1 || !Opaque[X + 1][Y]) {
                        Add(Out, -1, 0, 0, Center, X0, Y1, Z0, X0, Y0, Z0, X0, Y0, Z1, X0, Y1, Z1);
                    }
                }
            }

            return List.copyOf(Out);
        }
    }

    private static void Add(List<ModelQuad> Out, float NX, float NY, float NZ, float[] Uvs, float... Positions) {
        float[] Normals = new float[12];

        for (int I = 0; I < 4; I++) {
            Normals[I * 3] = NX;
            Normals[I * 3 + 1] = NY;
            Normals[I * 3 + 2] = NZ;
        }

        Out.add(new ModelQuad(Positions, Normals, Uvs, null));
    }
}