package com.java_unbound.molang.testing;

import com.java_unbound.JavaUnbound;
import com.java_unbound.entities.interfaces.EntityVariableInterface;
import com.java_unbound.molang.MolangParser;
import com.java_unbound.molang.enums.MolangExpression;
import com.java_unbound.molang.types.MolangEntityParser;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.level.Level;

public class MolangParserTest {

    private static class TestCow extends Cow {
        public double qhhozj;
        public double ykxvro;
        public double sxlefg;
        public double tsuowj;
        public double hqvvjb;

        public TestCow(Level Level) {
            super(EntityTypes.COW, Level);
        }
    }

    public static void test(Level Level) {
        JavaUnbound.LOGGER.error("========== MOLANG TEST START ==========");

        TestExpression("10");
        TestExpression("1.5");
        TestExpression("-20");
        TestExpression("q.is_on_ground");
        TestExpression("q.is_baby");
        TestExpression("v.speed");
        TestExpression("v.speed = 10");
        TestExpression("math.sin(query.life_time)");

        TestEntityMolang(Level);

        JavaUnbound.LOGGER.error("========== MOLANG TEST END ==========");
    }

    private static void TestExpression(String Expression) {
        try {
            MolangExpression Parsed = MolangParser.Classify(Expression);
            JavaUnbound.LOGGER.info("[MoLang Test] {} -> {}", Expression, Parsed);
        } catch (Exception e) {
            JavaUnbound.LOGGER.error("[MoLang Test] Failed to parse: " + Expression, e);
        }
    }

    private static void TestMolangValue(TestCow Entity, String Expression) {
        try {
            double Result = MolangParser.Evaluate(Entity, Expression);
            JavaUnbound.LOGGER.info("[MoLang Test] {} -> {}", Expression, Result);
        } catch (Exception e) {
            JavaUnbound.LOGGER.error("[MoLang Test] Failed to evaluate: " + Expression, e);
        }
    }

    private static void TestEntityQuery(TestCow Entity, String Expression) {
        try {
            double Result = MolangEntityParser.Evaluate(Entity, Expression);
            JavaUnbound.LOGGER.info("[Entity Query Test] {} -> {}", Expression, Result);
        } catch (Exception e) {
            JavaUnbound.LOGGER.error("[Entity Query Test] Failed: " + Expression, e);
        }
    }

    private static void TestEntityVariables(TestCow Entity) {
        try {
            EntityVariableInterface Variables = (EntityVariableInterface) Entity;

            JavaUnbound.LOGGER.error("========== ENTITY VARIABLE TESTS ==========");

            TestMolangValue(Entity, "v.sdad = 23");

            JavaUnbound.LOGGER.info("[Variable Test] HashMap: {}", Variables.JavaUnbound$getVariables());
            JavaUnbound.LOGGER.info("[Variable Test] sdad: {}", Variables.JavaUnbound$getVariable("sdad"));

            TestMolangValue(Entity, "v.sdad");

            TestMolangValue(Entity, "v.sdad = 42");

            JavaUnbound.LOGGER.info("[Variable Test] HashMap after update: {}", Variables.JavaUnbound$getVariables());
            JavaUnbound.LOGGER.info("[Variable Test] sdad after update: {}", Variables.JavaUnbound$getVariable("sdad"));

            TestMolangValue(Entity, "v.sdad");

            JavaUnbound.LOGGER.error("========== ENTITY VARIABLE TESTS END ==========");
        } catch (Exception e) {
            JavaUnbound.LOGGER.error("[Variable Test] Failed", e);
        }
    }

    private static void TestEntityMolang(Level Level) {
        try {
            TestCow TestCow = new TestCow(Level);

            TestCow.setCustomName(Component.literal("Steve"));

            JavaUnbound.LOGGER.error("========== ENTITY QUERY TESTS ==========");
            JavaUnbound.LOGGER.info("[MoLang Test] Cow name: {}", TestCow.getCustomName());

            TestEntityQuery(TestCow, "q.is_name_any('Steve')");
            TestEntityQuery(TestCow, "q.is_name_any('Alex')");
            TestEntityQuery(TestCow, "q.is_name_any('Alex', 'Steve', 'Herobrine')");
            TestEntityQuery(TestCow, "q.is_name_any('steve')");
            TestEntityQuery(TestCow, "q.is_name_any('Steve', 'Alex')");

            TestEntityQuery(TestCow, "q.is_baby");
            TestEntityQuery(TestCow, "q.is_alive");
            TestEntityQuery(TestCow, "q.is_on_ground");
            TestEntityQuery(TestCow, "q.is_in_water");
            TestEntityQuery(TestCow, "q.is_in_lava");
            TestEntityQuery(TestCow, "q.is_on_fire");
            TestEntityQuery(TestCow, "q.is_invisible");
            TestEntityQuery(TestCow, "q.is_sneaking");
            TestEntityQuery(TestCow, "q.is_sprinting");
            TestEntityQuery(TestCow, "q.is_swimming");
            TestEntityQuery(TestCow, "q.is_sleeping");
            TestEntityQuery(TestCow, "q.is_riding");
            TestEntityQuery(TestCow, "q.is_riding_any_entity");
            TestEntityQuery(TestCow, "q.is_attached");

            TestEntityQuery(TestCow, "q.entity_biome_has_any_identifier('minecraft:plains')");
            TestEntityQuery(TestCow, "q.entity_biome_has_any_identifier('minecraft:cherry_grove')");
            TestEntityQuery(TestCow, "q.entity_biome_has_any_identifier('minecraft:plains', 'minecraft:forest')");

            TestEntityQuery(TestCow, "q.is_in_ui");
            TestEntityQuery(TestCow, "q.graphics_mode_is_any('simple', 'fancy')");
            TestEntityQuery(TestCow, "q.is_pack_setting_selected('oreville_ans:fxwjdy', 'enabled')");

            JavaUnbound.LOGGER.error("========== NESTED MOLANG TESTS ==========");

            TestMolangValue(TestCow, "q.is_name_any('Steve')");
            TestMolangValue(TestCow, "math.abs(q.is_name_any('Steve'))");
            TestMolangValue(TestCow, "math.abs(math.abs(q.is_name_any('Steve')))");
            TestMolangValue(TestCow, "math.sin(math.abs(q.is_name_any('Steve')))");
            TestMolangValue(TestCow, "math.sin(math.cos(q.is_name_any('Steve')))");
            TestMolangValue(TestCow, "q.is_name_any('Steve') || q.is_baby");
            TestMolangValue(TestCow, "q.is_name_any('Alex') || q.is_baby");
            TestMolangValue(TestCow, "!q.is_name_any('Alex')");
            TestMolangValue(TestCow, "q.is_name_any('Steve') && q.is_alive");

            JavaUnbound.LOGGER.error("========== STATEMENT BLOCK TESTS ==========");

            TestMolangValue(TestCow, "v.qhhozj = 0");
            TestMolangValue(TestCow, "q.is_name_any('Steve') ? { v.qhhozj = 1; }");
            TestMolangValue(TestCow, "q.is_name_any('Alex') ? { v.qhhozj = 2; }");
            TestMolangValue(TestCow, "v.qhhozj");
            TestMolangValue(TestCow, "q.is_name_any('Steve') ? { v.qhhozj = 3; v.ykxvro = 5; }");
            TestMolangValue(TestCow, "v.qhhozj");
            TestMolangValue(TestCow, "v.ykxvro");

            TestMolangValue(TestCow, "v.qhhozj = 0");
            TestMolangValue(TestCow, "q.is_name_any('Steve') ? { q.is_alive ? { v.qhhozj = 4; }; }");
            TestMolangValue(TestCow, "v.qhhozj");

            TestEntityVariables(TestCow);

            JavaUnbound.LOGGER.error("========== ENTITY QUERY TESTS END ==========");
        } catch (Exception e) {
            JavaUnbound.LOGGER.error("[MoLang Test] MolangEntityParser failed", e);
        }
    }
}