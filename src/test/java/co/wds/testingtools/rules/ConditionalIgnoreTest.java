package co.wds.testingtools.rules;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

@ConditionalIgnore.Expression("description.getMethodName().equals('testIgnoredOnTheClassLevel');")
public class ConditionalIgnoreTest extends AbstractConditionalIgnoreRuleTest {
    @Rule
    public ConditionalIgnore ignoreRule = new ConditionalIgnore(this);

    private static Set<String> mandatoryTests = new HashSet<String>();

    @BeforeClass
    public static void init() {
        mandatoryTests.clear();
    }
    
    @AfterClass
    public static void checkExecuted() {
        String[] expectedExecutedTests = {
            "testExecuted1",
//            "testExecutionAllowedByTheClassStaticMethod",
            "testExecutionAllowedByTheClassInstanceMethod"
        };
        assertEquals(new HashSet<String>(asList(expectedExecutedTests)), mandatoryTests);
    }
    
    public static boolean shouldIgnore(String methodName) {
        return StringUtils.equals(methodName, "testIgnoredByTheClassStaticMethod");
    }

    public boolean shouldBeIgnored(boolean toIgnore) {
        return toIgnore;
    }

    @Test
    @ConditionalIgnore.Expression("java.lang.System.getProperty('ignore.unstable.tests') === null") // in real test you might want to compare with 'true'
    public void testIgnoreBySystemProperty() {
        fail();
    }

    @Test
    @ConditionalIgnore.Expression("true")
    public void testIgnored() {
        fail();
    }
    
    @Test
    @ConditionalIgnore.Expression("false")
    public void testExecuted1() {
        mandatoryTests.add("testExecuted1");
    }

    @Test
    @ConditionalIgnore.Expression("test.shouldBeIgnored(true)")
    public void testIgnoredByTheClassInstanceMethod() {
        fail();
    }

    @Test
    @ConditionalIgnore.Expression("test.shouldBeIgnored(false)")
    public void testExecutionAllowedByTheClassInstanceMethod() {
        mandatoryTests.add("testExecutionAllowedByTheClassInstanceMethod");
    }

    @Test
    @org.junit.Ignore // TODO fix it (for both java 7 & 8)
    @ConditionalIgnore.Expression("co.wds.testingtools.rules.ConditionalIgnoreTest.shouldIgnore(description.getMethodName())")
    public void testIgnoredByTheClassStaticMethod() {
        fail();
    }

    @Test
    @org.junit.Ignore // TODO fix it
    @ConditionalIgnore.Expression("importPackage('co.wds.testingtools.rules');"
            + "ConditionalIgnoreTest.shouldIgnore(description.getMethodName())")
    public void testExecutionAllowedByTheClassStaticMethod() {
        mandatoryTests.add("testExecutionAllowedByTheClassStaticMethod");
    }

    @Test
    public void testIgnoreBySuperclass() {
        fail("It has to be ignored by the javaScript expression");
    }

    @Test
    public void testIgnoredOnTheClassLevel() {
        fail();
    }
}
