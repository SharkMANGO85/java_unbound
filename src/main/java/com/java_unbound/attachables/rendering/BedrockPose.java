package com.java_unbound.attachables.rendering;

import java.util.HashMap;
import java.util.Map;

public final class BedrockPose {
    public static final class BoneState {
        public final float[] Position = {0F, 0F, 0F};
        public final float[] Rotation = {0F, 0F, 0F};
        public final float[] Scale = {1F, 1F, 1F};

        public boolean Visible = true;
    }

    private final Map<String, BoneState> States = new HashMap<>();

    public BoneState Bone(String Name) {
        return States.computeIfAbsent(Name, Key -> new BoneState());
    }

    BoneState Get(String Name) {
        return States.get(Name);
    }

    public void Reset() {
        States.clear();
    }
}