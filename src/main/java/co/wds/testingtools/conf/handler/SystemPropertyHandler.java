package co.wds.testingtools.conf.handler;

import java.util.HashMap;
import java.util.Map;

public class SystemPropertyHandler implements SettingHandler {
    private Map<String, String> savedSettings = new HashMap<String, String>();

    @Override
    public void setConfig(String key, Object value) {
        if (!savedSettings.containsKey(key)) { // a property can be overriden multiple times: per superclass, class, method; initial value has to be restored
            savedSettings.put(key, System.getProperty(key));
        }
        System.setProperty(key, (String) value);
    }

    @Override
    public void restoreConfig() {
        for (Map.Entry<String, String> entry : savedSettings.entrySet()) {
            if (entry.getValue() != null) {
                System.setProperty(entry.getKey(), entry.getValue());
            } else {
                System.clearProperty(entry.getKey());
            }
        }
    }
}