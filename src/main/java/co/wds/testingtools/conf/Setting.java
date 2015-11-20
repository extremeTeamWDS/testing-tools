package co.wds.testingtools.conf;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;

import co.wds.testingtools.conf.handler.SettingMapHandler;
import co.wds.testingtools.conf.handler.SettingHandler;

@Retention(RUNTIME)
public @interface Setting {
	String NULL = "${null}";

	Class<? extends SettingHandler> forThe() default SettingMapHandler.class;
	String key();
	String value();
}
