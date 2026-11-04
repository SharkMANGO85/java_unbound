package com.java_unbound.molang.types;

import com.java_unbound.molang.MolangParser;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.cubemob.Slime;
import net.minecraft.world.item.ItemStack;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class MolangEntityQueries {
    private static final Map<String, Optional<Method>> METHODS = new ConcurrentHashMap<>();

    private MolangEntityQueries() {}

    public static String Evaluate(Entity Entity, String Name, List<String> Arguments) {
        return switch (Name) {
            case "equipment_count" -> EquipmentCount(Entity);
            case "target_y_rotation" -> TargetYRotation(Entity);
            case "time_stamp" -> String.valueOf(Entity.level().getGameTime());
            case "is_riding_any_entity_of_type" -> RidingAnyEntityOfType(Entity, Arguments);
            case "is_gliding" -> Entity instanceof LivingEntity Living && Living.isFallFlying() ? "1" : "0";
            case "position" -> Position(Entity, Arguments);
            case "body_y_rotation" -> Entity instanceof LivingEntity Living ? String.valueOf(Living.yBodyRot) : "0";

            case "equipped_item_any_tag" -> EquippedItemAnyTag(Entity, Arguments);
            case "is_item_name_any" -> IsItemNameAny(Entity, Arguments);

            // Variants are entity specific. Until each entity is mapped, read them from entity variables.
            case "variant" -> {
                if (Entity instanceof Slime Slime) {
                    yield String.valueOf(Slime.getSize());
                }

                yield VariableOrZero(Entity, "variant");
            }

            case "mark_variant" -> VariableOrZero(Entity, "mark_variant");

            // Entity specific booleans/numbers, read by method name (the game is unobfuscated in 26.x).
            case "is_powered", "is_shield_powered" -> Bool(CallNoArgs(Entity, "isPowered"));
            case "is_charging" -> Bool(CallNoArgs(Entity, "isCharging"));
            case "is_chested" -> Bool(CallNoArgs(Entity, "hasChest"));
            case "is_eating_mob" -> Bool(CallNoArgs(Entity, "isEating"));
            case "is_shaking" -> Bool(CallNoArgs(Entity, "isShaking"));
            case "is_carrying_block" -> CallNoArgs(Entity, "getCarriedBlock") != null ? "1" : "0";
            case "swell_amount" -> Number(CallFloat(Entity, "getSwelling", 1.0F));

            // Need animation / property context that is not available here.
            case "anim_time", "is_ghost", "property", "armor_texture_slot" -> "0";

            default -> null;
        };
    }

    private static String EquipmentCount(Entity Entity) {
        if (!(Entity instanceof LivingEntity Living)) {return "0";}

        int Count = 0;

        for (EquipmentSlot Slot : new EquipmentSlot[]{
                EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            if (!Living.getItemBySlot(Slot).isEmpty()) {
                Count++;
            }
        }

        return String.valueOf(Count);
    }

    private static String TargetYRotation(Entity Entity) {
        if (!(Entity instanceof Mob Mob) || Mob.getTarget() == null) {return "0";}

        LivingEntity Target = Mob.getTarget();
        double DeltaX = Target.getX() - Mob.getX();
        double DeltaZ = Target.getZ() - Mob.getZ();
        float Wanted = (float) (Math.atan2(DeltaZ, DeltaX) * (180.0 / Math.PI)) - 90.0F;

        return String.valueOf(Mth.wrapDegrees(Wanted - Mob.getYRot()));
    }

    private static String RidingAnyEntityOfType(Entity Entity, List<String> Arguments) {
        Entity Vehicle = Entity.getVehicle();
        if (Vehicle == null) {return "0";}

        String Actual = BuiltInRegistries.ENTITY_TYPE.getKey(Vehicle.getType()).toString();

        for (String Argument : Arguments) {
            if (IdMatches(Actual, Argument)) {
                return "1";
            }
        }

        return "0";
    }

    private static String Position(Entity Entity, List<String> Arguments) {
        if (Arguments.isEmpty()) {return "0";}

        int Axis = (int) MolangParser.ParseDouble(MolangParser.Evaluate(Entity, Arguments.getFirst()));

        return switch (Axis) {
            case 0 -> String.valueOf(Entity.getX());
            case 1 -> String.valueOf(Entity.getY());
            case 2 -> String.valueOf(Entity.getZ());
            default -> "0";
        };
    }

    private static String EquippedItemAnyTag(Entity Entity, List<String> Arguments) {
        ItemStack Stack = GetSlotItem(Entity, Arguments);
        if (Stack == null || Stack.isEmpty()) {return "0";}

        var Holder = BuiltInRegistries.ITEM.wrapAsHolder(Stack.getItem());

        for (int Index = 1; Index < Arguments.size(); Index++) {
            String Wanted = Arguments.get(Index);

            if (Holder.tags().anyMatch(Tag -> IdMatches(Tag.location().toString(), Wanted))) {
                return "1";
            }
        }

        return "0";
    }

    // query.is_item_name_any('slot.weapon.mainhand', 'minecraft:diamond_sword', ...)
    private static String IsItemNameAny(Entity Entity, List<String> Arguments) {
        ItemStack Stack = GetSlotItem(Entity, Arguments);
        if (Stack == null || Stack.isEmpty()) {return "0";}

        String Actual = BuiltInRegistries.ITEM.getKey(Stack.getItem()).toString();

        for (int Index = 1; Index < Arguments.size(); Index++) {
            if (IdMatches(Actual, Arguments.get(Index))) {
                return "1";
            }
        }

        return "0";
    }

    private static ItemStack GetSlotItem(Entity Entity, List<String> Arguments) {
        if (!(Entity instanceof LivingEntity Living) || Arguments.size() < 2) {return null;}

        EquipmentSlot Slot = SlotFrom(MolangParser.Unquote(Arguments.getFirst()));

        return Slot == null ? null : Living.getItemBySlot(Slot);
    }

    private static EquipmentSlot SlotFrom(String Name) {
        return switch (Name.trim().toLowerCase(Locale.ROOT)) {
            case "slot.weapon.mainhand" -> EquipmentSlot.MAINHAND;
            case "slot.weapon.offhand" -> EquipmentSlot.OFFHAND;
            case "slot.armor.head" -> EquipmentSlot.HEAD;
            case "slot.armor.chest" -> EquipmentSlot.CHEST;
            case "slot.armor.legs" -> EquipmentSlot.LEGS;
            case "slot.armor.feet" -> EquipmentSlot.FEET;
            default -> null;
        };
    }

    private static boolean IdMatches(String Actual, String Wanted) {
        String Normalized = MolangParser.Unquote(Wanted).trim().toLowerCase(Locale.ROOT);

        if (!Normalized.contains(":")) {
            Normalized = "minecraft:" + Normalized;
        }

        return Actual.equals(Normalized);
    }

    private static String VariableOrZero(Entity Entity, String Name) {
        String Value = MolangEntityParser.GetVariable(Entity, Name);

        return Value != null ? Value : "0";
    }

    // Reflection helpers

    private static Method FindMethod(Class<?> Type, String Name, Class<?>... Parameters) {
        String Key = Type.getName() + "#" + Name + "/" + Parameters.length;

        return METHODS.computeIfAbsent(Key, Ignored -> {
            try {
                Method Found = Type.getMethod(Name, Parameters);
                Found.setAccessible(true);

                return Optional.of(Found);
            } catch (ReflectiveOperationException | RuntimeException Exception) {
                return Optional.empty();
            }
        }).orElse(null);
    }

    private static Object CallNoArgs(Entity Entity, String Name) {
        Method Found = FindMethod(Entity.getClass(), Name);
        if (Found == null) {return null;}

        try {
            return Found.invoke(Entity);
        } catch (ReflectiveOperationException | RuntimeException Exception) {
            return null;
        }
    }

    private static Object CallFloat(Entity Entity, String Name, float Argument) {
        Method Found = FindMethod(Entity.getClass(), Name, float.class);
        if (Found == null) {return null;}

        try {
            return Found.invoke(Entity, Argument);
        } catch (ReflectiveOperationException | RuntimeException Exception) {
            return null;
        }
    }

    private static String Bool(Object Value) {
        return Value instanceof Boolean Flag && Flag ? "1" : "0";
    }

    private static String Number(Object Value) {
        return Value instanceof Number Number ? String.valueOf(Number.doubleValue()) : "0";
    }
}