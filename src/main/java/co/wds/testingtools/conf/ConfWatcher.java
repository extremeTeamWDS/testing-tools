package co.wds.testingtools.conf;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.junit.runner.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

import co.wds.testingtools.rules.AbstractTestWatcher;
import co.wds.testingtools.conf.handler.SettingHandler;

/**
 * Allows declarative changing of play config properties:
 * 
 * @Rule
 * public ConfWatcher confWatcher = new ConfWatcher();
 * 
 * @Test
 * @With({
 *   @Setting(key = "k1", value = "{{mockServerAddress}}/gmapi" // put more additional variables in FunctionalTestRunningRule.context
 * })
 * public void test() {}
 */
public class ConfWatcher extends AbstractTestWatcher {
	private static Logger logger = LoggerFactory.getLogger(ConfWatcher.class);
	
	private static boolean started;

	private boolean restore;

	public ConfWatcher() {
		this(true);
	}

	public final Map<String, Object> context = new HashMap<String, Object>();
	MustacheFactory mf = new DefaultMustacheFactory();

	/*
	 * For debugging, set to false
	 */
	public ConfWatcher(boolean restore) {
		this.restore = restore;
	}

	String eval(String settingName, String settingExpression) {
	    if (description == null) {
	        new NullPointerException().printStackTrace();
	    }
	    settingName = description.getTestClass() + "." + description.getMethodName() + "/" + settingName;
	    Mustache template = mf.compile(new StringReader(settingExpression), settingName);
	    StringWriter writer = new StringWriter();
	    template.execute(writer, context);
	    writer.flush();
	    return writer.toString();
	}

	public final Map<Class<? extends SettingHandler>, SettingHandler> handlers = new HashMap<Class<? extends SettingHandler>, SettingHandler>();
	private void changeConfig(With with) throws InstantiationException, IllegalAccessException {
		if (with != null) {
			Setting[] configs = with.value();
			for (Setting conf : configs) {
				Class<? extends SettingHandler> handlerClass = conf.forThe();
				SettingHandler handler = handlers.get(handlerClass);
				if (handler == null) {
					handler = handlerClass.newInstance();
					handlers.put(handlerClass, handler);
				}
				String key = conf.key();
				String value = conf.value();
                handler.setConfig(key, value.contains("{{") ? eval(key, value) : value);
			}
		}
	}

    private void changeConfig(Class<?> testClass) throws InstantiationException, IllegalAccessException {
        Class<?> superclass = testClass.getSuperclass();
        if (superclass != null) {
            changeConfig(superclass);
        }
        changeConfig(testClass.getAnnotation(With.class));
    }

    protected void applyConfChanges(Description description) {
        if (!started) {
            started = true;
            try {
                handlers.clear();
                changeConfig(description.getTestClass());
                changeConfig(description.getAnnotation(With.class));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            logger.warn("ConfWatcher rule already applied for test " + description.getTestClass() + ", maybe duplicate @Rule ConfWatcher?");
        }
    }

    protected void restoreConfChanges(Description description) {
        try {
            if (!handlers.isEmpty() && restore) {
                for (SettingHandler handler : handlers.values()) {
                    handler.restoreConfig();
                }
            }
        } finally {
            started = false;
        }
    }

	@Override
	protected void starting(Description description) {
	    super.starting(description);
	    applyConfChanges(description);
	}
	
	@Override
	protected void finished(Description description) {
	    restoreConfChanges(description);
		super.finished(description);
	}
}
