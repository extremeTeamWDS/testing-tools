package co.wds.testingtools.rules;

import static org.junit.Assume.assumeFalse;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class ConditionalIgnoreRule extends TestWatcher {

    public interface IgnoreCondition {
        boolean isSatisfied(Description description) throws Exception;
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.METHOD, ElementType.TYPE})
    public @interface ConditionalIgnore {
        Class<? extends IgnoreCondition> condition();
    }
    
    void checkIgnored(ConditionalIgnore conditionalIgnore, Description description) throws Exception {
        IgnoreCondition condition = conditionalIgnore != null ? conditionalIgnore.condition().newInstance() : null;
        boolean assumptionIsSatisfied = condition != null && condition.isSatisfied(description);
        assumeFalse("Ignored by " + condition, assumptionIsSatisfied);
    }

    private void checkIgnored(Class<?> testClass, Description description) throws Exception {
        if (testClass != null) {
            checkIgnored(testClass.getAnnotation(ConditionalIgnore.class), description);
            checkIgnored(testClass.getSuperclass(), description);
        }
    }
    
    void checkIgnored(Description description) throws Exception {
        checkIgnored(description.getAnnotation(ConditionalIgnore.class), description);
    }

    @Override
    public Statement apply(final Statement base, final Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                checkIgnored(description);
                checkIgnored(description.getTestClass(), description);
                base.evaluate();
            }
        };
    }

}