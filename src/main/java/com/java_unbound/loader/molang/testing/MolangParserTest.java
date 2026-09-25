package com.java_unbound.loader.molang.testing;

import com.java_unbound.JavaUnbound;
import com.java_unbound.loader.molang.MolangExpression;
import com.java_unbound.loader.molang.MolangParser;
import com.java_unbound.loader.molang.types.MolangEntityParser;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.level.Level;

public class MolangParserTest {

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

    private static void TestMolangValue(Cow Entity, String Expression) {
        try {
            double Result = MolangParser.Evaluate(Entity, Expression);
            JavaUnbound.LOGGER.error("[MoLang Test] {} -> {}", Expression, Result);
        } catch (Exception e) {
            JavaUnbound.LOGGER.error("[MoLang Test] Failed to evaluate: " + Expression, e);
        }
    }

    private static void TestEntityMolang(Level Level) {
        try {
            Cow TestCow = EntityTypes.COW.create(Level, EntitySpawnReason.COMMAND);

            if (TestCow == null) {
                JavaUnbound.LOGGER.error("[MoLang Test] Failed to create test cow");
                return;
            }

            TestCow.setCustomName(Component.literal("Steve"));

            double Result = MolangEntityParser.Evaluate(
                    TestCow,
                    "q.is_name_any('Alex', 'Steve', 'Herobrine')"
            );

            JavaUnbound.LOGGER.info("[MoLang Test] Cow name: {}", TestCow.getCustomName());
            JavaUnbound.LOGGER.info("[MoLang Test] q.is_name_any -> {}", Result);

            TestMolangValue(TestCow, "q.is_name_any('Steve')");
            TestMolangValue(TestCow, "math.abs(q.is_name_any('Steve'))");
            TestMolangValue(TestCow, "math.abs(math.abs(q.is_name_any('Steve')))");
            TestMolangValue(TestCow, "math.sin(math.abs(q.is_name_any('Steve')))");
            TestMolangValue(TestCow, "math.sin(math.cos(q.is_name_any('Steve')))");
        } catch (Exception e) {
            JavaUnbound.LOGGER.error("[MoLang Test] MolangEntityParser failed", e);
        }
    }
}