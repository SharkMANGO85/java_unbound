package com.java_unbound.loader.textures;

import com.java_unbound.JavaUnbound;
import com.java_unbound.loader.resourcepack.VirtualResources;
import net.minecraft.client.Minecraft;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public final class AnimatedTexture {
    private static final int MinimumFrameCount = 1;
    private static final Map<String, AnimationData> Animations = new HashMap<>();

    private record AnimationData(int FrameCount, int FrameTime, boolean Enabled, boolean Interpolate) {}

    private AnimatedTexture() {}

    public static void Process(Path TexturePath, String TargetPath) {
        try {
            BufferedImage Image = ImageIO.read(TexturePath.toFile());
            if (Image == null) {return;}

            int Width = Image.getWidth();
            int Height = Image.getHeight();

            if (Width <= 0 || Height <= Width) {return;}

            if (Height % Width != 0) {
                JavaUnbound.LOGGER.warn("Animated texture has an invalid frame size: " + TexturePath + " (" + Width + "x" + Height + ")");
                return;
            }

            int FrameCount = Height / Width;

            //ModConfig.INSTANCE.RegisterAnimatedTexture(TargetPath);

            boolean Enabled = true; //ModConfig.INSTANCE.IsAnimatedTextureEnabled(TargetPath);
            int FrameTime = 1; //ModConfig.INSTANCE.GetAnimatedTextureFrameTime(TargetPath);
            boolean Interpolate = true; //ModConfig.INSTANCE.GetAnimatedTextureInterpolate(TargetPath);

            Animations.put(TargetPath, new AnimationData(Math.max(MinimumFrameCount, FrameCount), FrameTime, Enabled, Interpolate));

            WriteAnimation(TargetPath);
        } catch (IOException E) {
            JavaUnbound.LOGGER.error("Failed to process animated texture: " + TexturePath, E);
        }
    }

    public static void SetTextureSettings(String TargetPath, boolean Enabled, int FrameTime, boolean Interpolate) {
        if (TargetPath == null) {return;}

        //ModConfig.INSTANCE.SetAnimatedTexture(TargetPath, Enabled, FrameTime, Interpolate);

        AnimationData Animation = Animations.get(TargetPath);

        if (Animation != null) {
            Animations.put(TargetPath, new AnimationData(Animation.FrameCount(), Math.max(1, FrameTime), Enabled, Interpolate));

            WriteAnimation(TargetPath);
        }
    }

    public static void ChangeFrameTime(int FrameTime) {
        if (FrameTime <= 0) {
            JavaUnbound.LOGGER.warn("Animated texture frametime must be greater than 0: " + FrameTime);
            return;
        }

        for (String TargetPath : new HashSet<>(Animations.keySet())) {
            AnimationData Animation = Animations.get(TargetPath);

            //ModConfig.INSTANCE.SetAnimatedTexture(TargetPath, Animation.Enabled(), FrameTime, Animation.Interpolate());

            Animations.put(TargetPath, new AnimationData(Animation.FrameCount(), FrameTime, Animation.Enabled(), Animation.Interpolate()));

            WriteAnimation(TargetPath);
        }

        //ConfigManager.Save();
        Minecraft.getInstance().execute(() -> Minecraft.getInstance().reloadResourcePacks());
    }

    private static void WriteAnimation(String TargetPath) {
        AnimationData Animation = Animations.get(TargetPath);
        if (Animation == null) {return;}

        String McmetaPath = TargetPath + ".mcmeta";

        String Json = """
            {
              "animation": {
                "interpolate": %s,
                "frametime": %d,
                "frames": [%s]
              }
            }
            """.formatted(Animation.Interpolate(), Animation.FrameTime(), CreateFrameList(Animation.FrameCount()));

        VirtualResources.Set(McmetaPath, Json);
    }

    private static String CreateFrameList(int FrameCount) {
        StringBuilder Result = new StringBuilder();

        for (int I = 0; I < FrameCount; I++) {
            Result.append("      ").append(I);

            if (I < FrameCount - 1) {
                Result.append(",");
            }

            Result.append("\n");
        }

        return Result.toString();
    }
}