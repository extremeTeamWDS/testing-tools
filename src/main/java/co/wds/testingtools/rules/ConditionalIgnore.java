package co.wds.testingtools.rules;

import static org.junit.Assume.assumeFalse;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class ConditionalIgnore extends TestWatcher {

    private final Object test;
    public ConditionalIgnore(Object target) {
        this.test = target;
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.METHOD, ElementType.TYPE})
    public @interface Expression {
        String value();
    }

    private ScriptEngine getEngine() throws ScriptException {
        ScriptEngineManager factory = new ScriptEngineManager();
        ScriptEngine engine = factory.getEngineByName("JavaScript");
        if (engine.getClass().getSimpleName().equals("NashornScriptEngine")) {
            try {
                engine.eval("load('nashorn:mozilla_compat.js');");
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
        return engine;
    }

    private boolean isSatisfied(Description description, String script) throws ScriptException {
        ScriptEngine engine = getEngine();
        engine.put("description", description);
        engine.put("test", test);
        Boolean result = (Boolean) engine.eval(script);
        System.out.println("isSatisfied(" + script + ") -> " + result);
        return result;
    }

    void checkIgnored(Expression expression, Description description) throws Exception {
        String script = expression != null ? expression.value() : null;
        assumeFalse("Ignored by " + script, script != null && isSatisfied(description, script));
    }

    private void checkIgnored(Class<?> testClass, Description description) throws Exception {
        if (testClass != null) {
            checkIgnored(testClass.getAnnotation(Expression.class), description);
            checkIgnored(testClass.getSuperclass(), description);
        }
    }
    
    void checkIgnored(Description description) throws Exception {
        checkIgnored(description.getAnnotation(Expression.class), description);
    }

    public static void main(String[] args) throws Exception {
        System.out.println(new ConditionalIgnore("").getEngine().eval(
                "importPackage('co.wds.testingtools.rules');JavaScriptIgnore.getTestValue()"));
    }

    public static String getTestValue() {
        return "test";
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