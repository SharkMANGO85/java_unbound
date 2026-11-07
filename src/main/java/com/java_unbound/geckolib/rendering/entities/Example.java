package com.java_unbound.geckolib.rendering.entities;

import com.java_unbound.entities.rendering.GetRenderingData;
import com.java_unbound.geckolib.entity.EntityRenderPass;

import net.minecraft.world.entity.Entity;

import java.util.List;

public class Example {
    public static List<EntityRenderPass> RegisterEntity(Entity Entity) {
        return GetRenderingData.GetRenderingData(Entity);
    }
}