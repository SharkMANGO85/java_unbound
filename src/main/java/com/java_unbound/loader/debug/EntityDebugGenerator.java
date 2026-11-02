package com.java_unbound.loader.debug;

import com.mojang.brigadier.Command;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class EntityDebugGenerator {
    private static final int SPACING = 8;
    private static final int COLUMNS = 12;
    private static final int PLATFORM_SIZE = 5;
    private static final int PLATFORM_Y = -50;

    public static int Generate(ServerLevel Level, double OriginX, double OriginZ, Consumer<Component> Message) {
        List<EntityType<?>> EntityTypes = new ArrayList<>();

        for (EntityType<?> Type : BuiltInRegistries.ENTITY_TYPE) {
            EntityTypes.add(Type);
        }

        AtomicInteger Successful = new AtomicInteger();
        AtomicInteger Failed = new AtomicInteger();
        AtomicInteger BabyCount = new AtomicInteger();

        int Index = 0;

        for (EntityType<?> Type : EntityTypes) {
            int X = Index % COLUMNS;
            int Z = Index / COLUMNS;

            int BlockX = (int) OriginX + X * SPACING;
            int BlockZ = (int) OriginZ + Z * SPACING;

            BlockPos Platform = new BlockPos(BlockX, PLATFORM_Y, BlockZ);

            try {
                GeneratePlatform(Level, Platform);

                Entity Entity = Type.create(Level, EntitySpawnReason.COMMAND);

                if (Entity == null) {
                    Failed.incrementAndGet();
                    Index++;
                    continue;
                }

                Entity.absSnapTo(BlockX + 2.5, PLATFORM_Y + 1, BlockZ + 2.5, 180.0F, 0.0F);
                Entity.setCustomName(Component.literal(BuiltInRegistries.ENTITY_TYPE.getKey(Type) + " (Adult)"));
                Entity.setCustomNameVisible(true);
                Entity.setInvulnerable(true);

                if (Entity instanceof Mob MobEntity) {
                    MobEntity.setNoAi(true);
                    MobEntity.setPersistenceRequired();
                }

                if (Level.addFreshEntity(Entity)) {
                    Successful.incrementAndGet();
                } else {
                    Failed.incrementAndGet();
                }

                if (Entity instanceof AgeableMob) {
                    AgeableMob Baby = (AgeableMob) Type.create(Level, EntitySpawnReason.COMMAND);

                    if (Baby != null) {
                        Baby.setBaby(true);
                        Baby.absSnapTo(BlockX + 2.5, PLATFORM_Y + 1, BlockZ + 5.5, 180.0F, 0.0F);
                        Baby.setCustomName(Component.literal(BuiltInRegistries.ENTITY_TYPE.getKey(Type) + " (Baby)"));
                        Baby.setCustomNameVisible(true);
                        Baby.setInvulnerable(true);

                        if (Baby instanceof Mob BabyMob) {
                            BabyMob.setNoAi(true);
                            BabyMob.setPersistenceRequired();
                        }

                        if (Level.addFreshEntity(Baby)) {
                            BabyCount.incrementAndGet();
                        } else {
                            Failed.incrementAndGet();
                        }
                    }
                }

            } catch (Exception Exception) {
                Failed.incrementAndGet();

                System.err.println("[EntityDebug] Failed: " + BuiltInRegistries.ENTITY_TYPE.getKey(Type) + " | " + Exception.getMessage());
            }

            Index++;
        }

        Message.accept(Component.literal("Entity Debug Generation Finished!\n" + "Registered types: " + EntityTypes.size() + "\n" + "Adult entities: " + Successful.get() + "\n" + "Baby entities: " + BabyCount.get() + "\n" + "Failed: " + Failed.get()));

        return Command.SINGLE_SUCCESS;
    }

    private static void GeneratePlatform(ServerLevel Level, BlockPos Origin) {
        for (int X = 0; X < PLATFORM_SIZE; X++) {
            for (int Z = 0; Z < PLATFORM_SIZE; Z++) {
                Level.setBlock(Origin.offset(X, 0, Z), Blocks.STONE_BRICKS.defaultBlockState(), 3);
            }
        }
    }
}