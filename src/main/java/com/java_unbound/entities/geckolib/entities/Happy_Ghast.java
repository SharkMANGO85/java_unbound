package com.java_unbound.entities.geckolib.entities;

import com.java_unbound.entities.geckolib.GetRenderingData;
import com.java_unbound.loader.gecko_lib.entity.EntityRenderPass;

import net.minecraft.world.entity.Entity;

import java.util.List;

public class Happy_Ghast {
    public static List<EntityRenderPass> RegisterEntity(Entity Entity) {
        return GetRenderingData.GetRenderingData(Entity);
    }
}