package co.wds.testingtools.conf.handler;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SettingMapHandler implements SettingHandler {
	private static Logger logger = LoggerFactory.getLogger(SettingMapHandler.class);
	public final Map<String, Object> configuration = new HashMap<String, Object>();

	@Override
	public void setConfig(String key, Object value) {
		logger.warn("Overriding play.configuration: '" + key + "=" + value + "')");
		if (!configuration.containsKey(key)) {
			configuration.put(key, value);
		}
	}

	@Override
	public void restoreConfig() {
	    configuration.clear();
	}
}
