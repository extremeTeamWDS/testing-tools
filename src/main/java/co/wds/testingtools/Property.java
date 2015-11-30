package co.wds.testingtools;

import java.io.File;

public class Property {
    public static <T> T getProperty(String name, Class<T> valueClass, String defaultValue) {
        String value = System.getProperty(name);
        try {
            if (value == null) {
                String envName = name.replaceAll("\\W", "_");
                value = System.getenv(envName);
            }
            if (value == null) {
                value = defaultValue;
            }
            if (valueClass == Boolean.class) {
                return (T) Boolean.valueOf(value);
            } else if (value != null) {
                if (valueClass == Integer.class) {
                    return (T) Integer.valueOf(value);
                } else if (valueClass == File.class) {
                    return (T) new File(value);
                }
            }
            return (T) value;
        } catch (RuntimeException e) {
            e.printStackTrace();
            throw e;
        } finally {
            System.out.println("Property " + name + "=" + value);
        }
    }
}
