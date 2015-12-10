package co.wds.testingtools.rules;

import static org.junit.Assert.fail;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.Description;

import co.wds.testingtools.rules.ConditionalIgnoreRule.ConditionalIgnore;
import co.wds.testingtools.rules.ConditionalIgnoreRule.IgnoreCondition;
import co.wds.testingtools.rules.ConditionalIgnoreRuleTest.IgnoreByTestName;

@ConditionalIgnore(condition = IgnoreByTestName.class)
public class ConditionalIgnoreRuleTest extends AbstractConditionalIgnoreRuleTest {
    @Rule
    public ConditionalIgnoreRule ignoreRule = new ConditionalIgnoreRule();
    
    public static class AlwaysIgnoreCondition implements IgnoreCondition {
        @Override
        public boolean isSatisfied(Description d) {
            return true;
        }
    }

    public static class NeverIgnoreCondition implements IgnoreCondition {
        @Override
        public boolean isSatisfied(Description d) {
            return false;
        }
    }

    public static class IgnoreByTestName implements IgnoreCondition {
        @Override
        public boolean isSatisfied(Description description) {
            return description.getMethodName().equals("testIgnoredOnTheClassLevel");
        }
    }

    private static boolean mandatoryTestExecuted;

    @BeforeClass
    public static void init() {
        mandatoryTestExecuted = false;
    }

    @Test
    @JavaScriptIgnore.Expression("description.getTestClass().getSuperclass().getSimpleName() === 'AbstractConditionalIgnoreRuleTest'")
    public void testIgnoreOnSuperclassLevel() {
        fail("It has to be ignored by the javaScript expression");
    }

    @Test
    public void testIgnoredOnTheClassLevel() {
        fail();
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
