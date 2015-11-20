package co.wds.testingtools.conf;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import co.wds.testingtools.conf.ConfWatcher;
import co.wds.testingtools.conf.Setting;
import co.wds.testingtools.conf.With;
import co.wds.testingtools.conf.handler.SettingMapHandler;
import co.wds.testingtools.conf.handler.SystemPropertyHandler;

@With({
    @Setting(key = "class-setting-key-1", value = "class-setting-key-1"),
    @Setting(key = "class-setting-key-2", value = "class-setting-key-2"),
    @Setting(key = "system-setting-key-2", value = "system-setting-value-2.1", forThe = SystemPropertyHandler.class),
    @Setting(key = "system-setting-key-3", value = "system-setting-value-3", forThe = SystemPropertyHandler.class),
})
public class ConfWatcherTest {

    @Rule
    public ConfWatcher confWatcher = new ConfWatcher();
    private static Map<String, Object> initialSystemProperties;

    @BeforeClass
    public static void initOnce() {
        initialSystemProperties = new HashMap(System.getProperties());
        System.setProperty("system-setting-key-1", "system-setting-value-1");
        System.setProperty("system-setting-key-2", "system-setting-value-2");
    }

    @AfterClass
    public static void clearOnce() {
        System.clearProperty("system-setting-key-1");
        System.clearProperty("system-setting-key-2");
        Map<String, Object> actualSystemProperties = new HashMap(System.getProperties());
        
        // timezone is changed somewhere else, ignore if it has been changed
        initialSystemProperties.remove("user.timezone");
        actualSystemProperties.remove("user.timezone");

        assertEquals(initialSystemProperties, actualSystemProperties);
    }

    @Test
    @With({
        @Setting(key = "method-setting-key-1", value = "method-setting-value-1"),
        @Setting(key = "method-setting-key-2", value = "method-setting-value-2"),
        @Setting(key = "system-setting-key-3", value = "system-setting-value-3.1", forThe = SystemPropertyHandler.class),
        @Setting(key = "system-setting-key-4", value = "system-setting-value-4", forThe = SystemPropertyHandler.class),
    })
    public void testConfHandler() throws Exception {
        SettingMapHandler handler = (SettingMapHandler) confWatcher.handlers.get(SettingMapHandler.class);
        Map<String, Object> expectedSettings = new HashMap<String, Object>();
        expectedSettings.put("class-setting-key-1", "class-setting-key-1");
        expectedSettings.put("class-setting-key-2", "class-setting-key-2");
        expectedSettings.put("method-setting-key-1", "method-setting-value-1");
        expectedSettings.put("method-setting-key-2", "method-setting-value-2");
        assertEquals(expectedSettings, handler.configuration);
        
        Properties expectedSystemProperties = new Properties();
        expectedSystemProperties.put("system-setting-key-1", "system-setting-value-1");
        expectedSystemProperties.put("system-setting-key-2", "system-setting-value-2.1");
        expectedSystemProperties.put("system-setting-key-3", "system-setting-value-3.1");
        expectedSystemProperties.put("system-setting-key-4", "system-setting-value-4");
        
        Map<String, Object> actualSystemProperties = new HashMap(System.getProperties());
        for (Object key : initialSystemProperties.keySet()) {
            actualSystemProperties.remove(key);
        }
        assertEquals(expectedSystemProperties, actualSystemProperties);
    }
}
