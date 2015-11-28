package co.wds.testingtools.webdriver;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.BindException;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.URL;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.firefox.internal.ProfilesIni;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.CommandExecutor;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.HttpCommandExecutor;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.server.RemoteControlConfiguration;
import org.openqa.selenium.server.SeleniumServer;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

/*
 * -Dip.address.lookup.url=$IP_LOOKUP_URL
 * -Dselenium.host=$SELENIUM_HOST
 * -Dwebdrive.timeout=540
 */
public class WebDriverManager {
    public static enum Browser {
        FIREFOX("firefox"), CHROME("chrome");
        public final String id;
        Browser(String id) {
            this.id = id;
        }
    }

    private static <T> T getProperty(String name, Class<T> valueClass, String defaultValue) {
        String value = System.getProperty(name);
        if (value == null) {
            String envName = name.replaceAll("\\W", "_");
            value = System.getenv(envName);
        }
        if (value == null) {
            value = defaultValue;
        }
        if (value != null) {
            if (valueClass == Boolean.class) {
                return (T) Boolean.valueOf(value);
            } else if (valueClass == Integer.class) {
                return (T) Integer.valueOf(value);
            }
        }
        return (T) value;
    }

    public final static int APP_PORT = getProperty("http.port", Integer.class, "3333");

    public static final Boolean WEBDRIVER_NATIVE_EVENTS = getProperty("webdriver.native.events", Boolean.class, null);
    public final static String IP_ADDRESS_LOOKUP_URL = getProperty("ip.address.lookup.url", String.class, null);
    public static final boolean LOGS_ENABLED = getProperty("logs.enabled", Boolean.class, null);
    public static final String WEBDRIVER_BROWSER_VERSION = getProperty("webdriver.browser.version", String.class, "");
    
    /* In order to debug, create custom profile, install required plugins (e.g. firebug) and pass it's name as -Dwebdrier.browser.profile=XXX */
    private static final String WEBDRIVER_BROWSER = getProperty("webdriver.browser", String.class, Browser.FIREFOX.id); // chrome | firefox
    private static final String WEBDRIVER_BROWSER_PROFILE = getProperty("webdriver.browser.profile", String.class, null);
    private static final String SELENIUM_SERVER_URL = getProperty("selenium.host", String.class,
            "http://127.0.0.1:" + RemoteControlConfiguration.DEFAULT_PORT + "/wd/hub");

	private static SeleniumServer seleniumServer;
	private static WebDriver webdriver;

	private static boolean initialized;

    private static String baseUrl;

    private static String externalAddress;

    public static boolean isWebDriverLocal() {
        return IP_ADDRESS_LOOKUP_URL == null;
    }

    private static String doHttpGet(String desiredUrl) {
        URL url = null;
        BufferedReader reader = null;
        StringBuilder stringBuilder;

        try {
            url = new URL(desiredUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setReadTimeout((int) SECONDS.toMillis(30));
            connection.connect();
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            stringBuilder = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line + "\n");
            }
            return stringBuilder.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        }
    }

    public static String getExternalAddress() {
        if (externalAddress == null) {
            externalAddress = IP_ADDRESS_LOOKUP_URL != null ? StringUtils.trim(doHttpGet(IP_ADDRESS_LOOKUP_URL)) : "127.0.0.1";
        }
        return externalAddress;
    }

    public static String getBaseUrl() {
        if (baseUrl == null) {
            StringBuilder builder = new StringBuilder("http://");
            builder.append(getExternalAddress());
            builder.append(":");
            builder.append(APP_PORT);
            baseUrl = builder.toString();
        }
        return baseUrl;
    }
    
	private static void init() throws Exception {
	    if (!initialized) {
	        startSeleniumServer();
	        initWebDriver(null);
	        Runtime.getRuntime().addShutdownHook(new ShutdownHook());
	        initialized = true;
	    }
	}

	private static void startSeleniumServer() throws Exception {
	    if (isWebDriverLocal()) {
	        try {
	            ServerSocket serverSocket = new ServerSocket(RemoteControlConfiguration.DEFAULT_PORT);
	            serverSocket.close();
	            
	            seleniumServer = new SeleniumServer();
	            seleniumServer.boot();
	            seleniumServer.start();
	        } catch (BindException e) {
	            System.out.println("Selenium server already up, will reuse...");
	        }
	    }
	}

    public WebDriver getWebdriverForNewTest() throws Exception {
        init();
        try {
            webdriver.manage().deleteAllCookies();
        } catch(Throwable t) {
            initWebDriver(null);
            webdriver.manage().deleteAllCookies();
        }
        return webdriver;
    }

    static void setDataReportEnabled(FirefoxProfile profile, boolean enabled) {
        profile.setPreference("datareporting.healthreport.uploadEnabled", enabled);
        profile.setPreference("datareporting.healthreport.service.enabled", enabled);
        profile.setPreference("datareporting.healthreport.service.firstRun", enabled);
    }

	private static void initWebDriver(String locale) throws IOException {
	    System.out.println("initWebDriver() with settigns:"
            + "\n\tAPP_PORT=" + APP_PORT
            + "\n\tWEBDRIVER_BROWSER=" + WEBDRIVER_BROWSER
            + "\n\tWEBDRIVER_BROWSER_VERSION=" + WEBDRIVER_BROWSER_VERSION
            + "\n\tWEBDRIVER_BROWSER_PROFILE=" + WEBDRIVER_BROWSER_PROFILE
            + "\n\tWEBDRIVER_NATIVE_EVENTS=" + WEBDRIVER_NATIVE_EVENTS
            + "\n\tIP_ADDRESS_LOOKUP_URL=" + IP_ADDRESS_LOOKUP_URL
            + "\n\tLOGS_ENABLED=" + LOGS_ENABLED
            + "\n\tSELENIUM_SERVER_URL=" + SELENIUM_SERVER_URL
            + "\n"
        );

		LoggingPreferences logs = new LoggingPreferences();
		if (LOGS_ENABLED) {
		    logs.enable(LogType.BROWSER, Level.ALL);
		    logs.enable(LogType.CLIENT, Level.ALL);
		    logs.enable(LogType.DRIVER, Level.ALL);
		    logs.enable(LogType.PERFORMANCE, Level.ALL);
		    logs.enable(LogType.PROFILER, Level.ALL);
		    logs.enable(LogType.SERVER, Level.ALL);
		}

		DesiredCapabilities desiredCapabilities = new DesiredCapabilities(WEBDRIVER_BROWSER, WEBDRIVER_BROWSER_VERSION, Platform.ANY); //firefox
		desiredCapabilities.setCapability(CapabilityType.LOGGING_PREFS, logs);

		FirefoxProfile prof = null;
		if (WEBDRIVER_BROWSER_PROFILE != null) {
		    prof = new ProfilesIni().getProfile(WEBDRIVER_BROWSER_PROFILE);
		    if (prof == null) {
		        System.out.println("There is no such profile: " + WEBDRIVER_BROWSER_PROFILE);
		    }
		}
		if (prof == null) {
		    prof = new FirefoxProfile();
		}
		
		prof.setEnableNativeEvents(WEBDRIVER_NATIVE_EVENTS);
		setDataReportEnabled(prof, false);

		if( locale != null ) {
		    prof.setPreference("intl.accept_languages", locale );
		}
		
		desiredCapabilities.setCapability("firefox_profile", prof);
		
		System.out.println("seleniumServerUrl:" + SELENIUM_SERVER_URL);
		
		CommandExecutor executor = new HttpCommandExecutor(new java.net.URL(SELENIUM_SERVER_URL));
		if (webdriver != null) {
		    webdriver.quit();
		}
		webdriver = new RemoteWebDriver(executor, desiredCapabilities);
	}

	public static void close() {
	    System.out.println("\nStopping WebDriverManager\n");
	    if (webdriver != null) {
	        try {
	            webdriver.quit();
	            webdriver = null;
	        } catch (Exception e) {
	        }
	    }
	    
	    if (isWebDriverLocal()) {
	        if (seleniumServer != null) {
	            seleniumServer.stop();
	            seleniumServer = null;
	        }
	    }
	    initialized = false;
	}

    private static class ShutdownHook extends Thread {
        @Override
        public void run() {
            close();
        }
    }
}
