package co.wds.testingtools.rules;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import co.wds.testingtools.rules.ConditionalIgnoreRule.ConditionalIgnore;
import co.wds.testingtools.rules.ConditionalIgnoreRule.IgnoreCondition;

public class ConditionalIgnoreRuleTest {
    @Rule
    public ConditionalIgnoreRule ignoreRule = new ConditionalIgnoreRule();
    
    public static class AlwaysIgnoreCondition implements IgnoreCondition {
        @Override
        public boolean isSatisfied() {
            return true;
        }
    }

    public static class NeverIgnoreCondition implements IgnoreCondition {
        @Override
        public boolean isSatisfied() {
            return false;
        }
    }

    private static boolean mandatoryTestExecuted;

    @BeforeClass
    public static void init() {
        mandatoryTestExecuted = false;
    }

    @AfterClass
    public static void checkExecuted() {
        if (!mandatoryTestExecuted) {
            throw new RuntimeException();
        }
    }

    @ConditionalIgnore(condition = AlwaysIgnoreCondition.class)
    @Test
    public void testIgnored() {
        fail();
    }

    @ConditionalIgnore(condition = NeverIgnoreCondition.class)
    @Test
    public void testExecuted() {
        mandatoryTestExecuted = true;
    }
}
