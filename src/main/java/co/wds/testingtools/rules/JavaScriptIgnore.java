package co.wds.testingtools.rules;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.script.*;

import org.junit.runner.Description;

import co.wds.testingtools.rules.ConditionalIgnoreRule.IgnoreCondition;

public class JavaScriptIgnore implements IgnoreCondition {
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.METHOD})
    public @interface Expression {
        String value();
    }

    private String script;

    @Override
    public boolean isSatisfied(Description description) throws ScriptException {
        Expression expression = description.getAnnotation(Expression.class);
        if (expression != null) {
            script = expression.value();
            ScriptEngineManager factory = new ScriptEngineManager();
            ScriptEngine engine = factory.getEngineByName("JavaScript");
            engine.put("description", description);
            Boolean result = (Boolean) engine.eval(script);
            System.out.println("isSatisfied(" + script + ") -> " + result);
            return result;
        }
        return false;
    }
    
    @Override
    public String toString() {
        return "JavaScript [" + script + "]";
    }
}