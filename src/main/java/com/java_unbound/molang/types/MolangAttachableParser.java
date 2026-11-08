package com.java_unbound.molang.types;

import com.java_unbound.molang.MolangParser;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

// Evaluates MoLang for Bedrock attachables (item + slot + owner) the same way MolangParser does for entities.
public final class MolangAttachableParser {
    private MolangAttachableParser() {}

    private static final ThreadLocal<Context> CURRENT = new ThreadLocal<>();
    private static final Map<Entity, Map<String, State>> ENTITY_STATES = Collections.synchronizedMap(new WeakHashMap<>());
    private static final Map<String, State> DETACHED_STATES = new ConcurrentHashMap<>();

    public enum Slot {
        MAIN_HAND("main_hand", EquipmentSlot.MAINHAND),
        OFF_HAND("off_hand", EquipmentSlot.OFFHAND),
        HEAD("head", EquipmentSlot.HEAD);

        public final String Name;
        public final EquipmentSlot Equipment;

        Slot(String Name, EquipmentSlot Equipment) {
            this.Name = Name;
            this.Equipment = Equipment;
        }
    }

    public static final class State {
        public final Map<String, String> Variables = new HashMap<>();
        public final Map<String, String> Temporaries = new HashMap<>();

        private final long CreatedNanos = System.nanoTime();
        private boolean Initialized;

        public String GetVariable(String Name) {return Variables.get(Normalize(Name));}
        public void SetVariable(String Name, String Value) {Variables.put(Normalize(Name), Value);}
        public String GetTemporary(String Name) {return Temporaries.get(Normalize(Name));}
        public void SetTemporary(String Name, String Value) {Temporaries.put(Normalize(Name), Value);}

        public boolean ConsumeInitialization() {
            if (Initialized) {return false;}

            Initialized = true;

            return true;
        }

        public double AgeSeconds() {
            return (System.nanoTime() - CreatedNanos) / 1_000_000_000.0;
        }

        private static String Normalize(String Name) {
            return Name == null ? "" : Name.trim().toLowerCase(Locale.ROOT);
        }
    }

    public static final class Context {
        public final Entity Subject;
        public final Entity Owner;
        public final ItemStack Stack;
        public final String ItemId;
        public final Slot Slot;
        public final boolean FirstPerson;
        public final boolean InUi;
        public final State State;

        public boolean ParentScope;

        private Context(Entity Subject, Entity Owner, ItemStack Stack, Slot Slot, boolean FirstPerson, boolean InUi) {
            this.Subject = Subject;
            this.Owner = Owner;
            this.Stack = Stack;
            this.ItemId = BuiltInRegistries.ITEM.getKey(Stack.getItem()).toString();
            this.Slot = Slot;
            this.FirstPerson = FirstPerson;
            this.InUi = InUi;
            this.State = ResolveState(Subject, ItemId, Slot == null ? "none" : Slot.Name, FirstPerson, InUi);
        }
    }

    public static Context ForHeld(LivingEntity Owner, Slot Slot, boolean FirstPerson) {
        if (Owner == null || Slot == null) {return null;}

        ItemStack Stack = Owner.getItemBySlot(Slot.Equipment);
        if (Stack.isEmpty()) {return null;}

        return new Context(Owner, Owner, Stack, Slot, FirstPerson, false);
    }

    public static Context ForHeld(LivingEntity Owner, String ItemId, Slot Slot, boolean FirstPerson) {
        Context Context = ForHeld(Owner, Slot, FirstPerson);
        if (Context == null || !IdMatches(Context.ItemId, ItemId)) {return null;}

        return Context;
    }

    public static Context ForHeld(Entity Owner, ItemStack Stack, Slot Slot, boolean FirstPerson) {
        if (Owner == null || Stack == null || Stack.isEmpty() || Slot == null) {return null;}

        return new Context(Owner, Owner, Stack, Slot, FirstPerson, false);
    }

    public static Context ForDropped(Entity ItemEntity, ItemStack Stack) {
        if (ItemEntity == null || Stack == null || Stack.isEmpty()) {return null;}

        return new Context(ItemEntity, null, Stack, null, false, false);
    }

    public static Context ForUi(ItemStack Stack) {
        if (Stack == null || Stack.isEmpty()) {return null;}

        return new Context(null, null, Stack, null, false, true);
    }

    public static String Evaluate(Context Context, String Expression) {
        if (Context == null) {return "0";}

        Context Previous = Swap(Context);

        try {
            return MolangParser.Evaluate(Context.Subject, Expression);
        } finally {
            Swap(Previous);
        }
    }

    public static String EvaluateParentSetup(Context Context, String Expression) {
        if (Context == null || Context.Owner == null) {return "0";}

        Context.ParentScope = true;

        try {
            return Evaluate(Context, Expression);
        } finally {
            Context.ParentScope = false;
        }
    }

    public static boolean IsConditionMet(Context Context, String Condition) {
        if (Condition == null || Condition.isBlank()) {return true;}

        String Result = Evaluate(Context, Condition);

        return MolangParser.IsTrue(Result);
    }

    public static Context Current() {
        return CURRENT.get();
    }

    public static Context Swap(Context Context) {
        Context Previous = CURRENT.get();

        if (Previous != null && Context != null && Previous.ItemId.equals(Context.ItemId)) return Previous;

        if (Context == null) {
            CURRENT.remove();
        } else {
            CURRENT.set(Context);
        }

        return Previous;
    }

    public static String EvaluateLeaf(Context Context, String Expression) {
        String LowerExpression = Expression.toLowerCase(Locale.ROOT);
        String Name = Expression.substring(Expression.indexOf('.') + 1).trim();

        if (LowerExpression.startsWith("c.") || LowerExpression.startsWith("context.")) {
            return EvaluateContextValue(Context, Name.toLowerCase(Locale.ROOT));
        }

        if (LowerExpression.startsWith("q.") || LowerExpression.startsWith("query.")) {
            return EvaluateQuery(Context, Expression);
        }

        if (Context.ParentScope) {return null;}

        if (LowerExpression.startsWith("v.") || LowerExpression.startsWith("variable.")) {
            String Value = Context.State.GetVariable(Name);

            return Value != null ? Value : "0";
        }

        if (LowerExpression.startsWith("t.") || LowerExpression.startsWith("temp.")) {
            String Value = Context.State.GetTemporary(Name);

            return Value != null ? Value : "0";
        }

        return null;
    }

    private static String EvaluateContextValue(Context Context, String Name) {
        return switch (Name) {
            case "item_slot" -> Context.Slot == null ? "" : Context.Slot.Name;
            case "is_first_person" -> Context.FirstPerson ? "1" : "0";
            case "owning_entity" -> Context.Owner != null ? "1" : "0";

            default -> "0";
        };
    }

    public static String EvaluateQuery(Context Context, String Expression) {
        int PrefixLength = Expression.toLowerCase(Locale.ROOT).startsWith("query.") ? 6 : 2;
        int OpenIndex = Expression.indexOf('(');
        String Name;
        List<String> Arguments;

        if (OpenIndex < 0) {
            Name = Expression.substring(PrefixLength).trim().toLowerCase(Locale.ROOT);
            Arguments = List.of();
        } else {
            if (!Expression.endsWith(")") || !MolangParser.IsBalanced(Expression)) {return "0";}

            Name = Expression.substring(PrefixLength, OpenIndex).trim().toLowerCase(Locale.ROOT);
            Arguments = MolangParser.SplitArguments(Expression.substring(OpenIndex + 1, Expression.length() - 1));
        }

        return switch (Name) {
            case "is_attached" -> Context.Owner != null ? "1" : "0";
            case "is_in_ui" -> Context.InUi ? "1" : "0";
            case "life_time" -> String.valueOf(Context.State.AgeSeconds());
            case "is_name_any" -> IsNameAny(Context, Arguments);
            case "is_owner_identifier_any" -> IsOwnerIdentifierAny(Context, Arguments);
            case "is_item_identifier_any" -> IsItemIdentifierAny(Context, Arguments);

            default -> MolangEntityParser.EvaluateQuery(Context.Subject, Name, Arguments);
        };
    }

    private static String IsNameAny(Context Context, List<String> Arguments) {
        Component Name = Context.Stack.get(DataComponents.CUSTOM_NAME);
        if (Name == null) {return "0";}

        String Actual = Name.getString();

        for (String Argument : Arguments) {
            if (Actual.equalsIgnoreCase(MolangParser.Unquote(Argument).trim())) {return "1";}
        }

        return "0";
    }

    private static String IsItemIdentifierAny(Context Context, List<String> Arguments) {
        for (String Argument : Arguments) {
            if (IdMatches(Context.ItemId, Argument)) {return "1";}
        }

        return "0";
    }

    private static String IsOwnerIdentifierAny(Context Context, List<String> Arguments) {
        if (Context.Owner == null) {return "0";}

        String Actual = BuiltInRegistries.ENTITY_TYPE.getKey(Context.Owner.getType()).toString();

        for (String Argument : Arguments) {
            if (IdMatches(Actual, Argument)) {
                return "1";
            }
        }

        return "0";
    }

    private static boolean IdMatches(String Actual, String Wanted) {
        String Normalized = MolangParser.Unquote(Wanted).trim().toLowerCase(Locale.ROOT);

        if (!Normalized.contains(":")) {
            Normalized = "minecraft:" + Normalized;
        }

        return Actual.equals(Normalized);
    }

    private static State ResolveState(Entity Subject, String ItemId, String SlotName, boolean FirstPerson, boolean InUi) {
        String Key = ItemId + "|" + SlotName + "|" + (FirstPerson ? "fp" : "tp") + (InUi ? "|ui" : "");

        if (Subject == null) {
            return DETACHED_STATES.computeIfAbsent(Key, Ignored -> new State());
        }

        synchronized (ENTITY_STATES) {
            return ENTITY_STATES.computeIfAbsent(Subject, Ignored -> new HashMap<>()).computeIfAbsent(Key, Ignored -> new State());
        }
    }
}