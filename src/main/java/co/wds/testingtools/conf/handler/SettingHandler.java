package co.wds.testingtools.conf.handler;

public interface SettingHandler {

	void setConfig(String key, Object value);

	void restoreConfig();

}
