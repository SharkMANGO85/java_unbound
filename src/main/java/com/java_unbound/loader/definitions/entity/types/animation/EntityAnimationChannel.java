package com.java_unbound.loader.definitions.entity.types.animation;

import java.util.List;

public class EntityAnimationChannel {
    public String[] Value;
    public List<EntityAnimationKeyframe> Keyframes;

    public boolean IsKeyframed() {return Keyframes != null;}
}
