package co.wds.testingtools.webdriver;

import static co.wds.testingtools.annotations.mapperservlet.TestingServer.SERVER_MAX_PORT;
import static co.wds.testingtools.annotations.mapperservlet.TestingServer.SERVER_MIN_PORT;
import static co.wds.testingtools.annotations.mapperservlet.TestingServer.getFreePort;
import static co.wds.testingtools.webdriver.Conditions.javascript;
import static co.wds.testingtools.webdriver.WebDriverManager.LOGS_BASE_DIR;
import static co.wds.testingtools.webdriver.WebDriverManager.getBaseUrl;
import static java.lang.String.format;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.runner.Description;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.Logs;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import co.wds.testingtools.rules.AbstractTestWatcher;
import co.wds.testingtools.rules.ConditionalIgnore;

public abstract class AbstractWebDriverTest extends AbstractPage {
	
	private static Logger logger = LoggerFactory.getLogger(AbstractWebDriverTest.class);
    
	private static final File WEBDRIVER_LOGS_DIR_PATH = new File(LOGS_BASE_DIR, "webdriver_logs/");

	protected WebDriver webdriver;
	private int appPort;
	protected String baseUrl;

	protected static WebDriverManager lifecycle = new WebDriverManager();

    @Rule // rule name should be unique otherwise parent rule with same name will be skipped (just tried with JUnit 4.12)
    public ConditionalIgnore abstractWebDriverTestConditionalIgnoreRule = new ConditionalIgnore(this);

    @Rule // rule name should be unique otherwise parent rule with same name will be skipped (just tried with JUnit 4.12)
    public AbstractTestWatcher abstractWebDriverTestAbstractTestWatcher = new AbstractTestWatcher() {
        @Override
        public void failed(Throwable t, Description description) {
            super.failed(t, description);
            takeScreenshot(getTestName());
            dumpBrowserLogs(getTestName());
        }
    };

	@BeforeClass
	public static void logBrowserVersion() throws Exception {
	    WebDriver driver = lifecycle.getWebdriverForNewTest();
	    Object userAgent = new WebDriverWait(driver, 10).until(javascript("return navigator.userAgent;"));
	    System.out.println("User Browser: " + userAgent);
	}

	@Before
	public void beforeEachTest() throws Exception {
	    appPort = getFreePort(SERVER_MIN_PORT, SERVER_MAX_PORT);
        System.out.println("\n\tappPort=" + appPort);
	    baseUrl = getBaseUrl(appPort);
		webdriver = lifecycle.getWebdriverForNewTest();
	}

	protected int getAppPort() {
	    return appPort;
	}

	@AfterClass
	public static void clearOnce() {
	    WebDriverManager.close();
	}

    protected void GET(String uri) {
        try {
            String url = new URI(uri).isAbsolute() ? uri : baseUrl + uri;
            System.out.println("Getting url: " + url);
            webdriver.get(url);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

	protected void dumpBrowserLogs(String testName) {
		try {
			Logs logs = webdriver.manage().logs();
			
			for(String type : logs.getAvailableLogTypes()) {
				String fileName = testName + "_" + type;
				List<LogEntry> allLogs = logs.get(type).getAll();
				if (allLogs.size() > 0) {
				    WEBDRIVER_LOGS_DIR_PATH.mkdirs();
					writeLines(new File(WEBDRIVER_LOGS_DIR_PATH, fileName), allLogs);
				}
			}
		} catch (Exception e) {
			logger.error("Cannot dumpBrowserLogs('" + testName + "')", e);
		}
	}

	private void writeLines(File file, Collection<?> lines) {
		try {
			FileUtils.writeLines(file, lines, false);
		} catch (Exception e) {
			logger.error(format("Error happened during saving to %s", file), e);
		}
	}
	
	@Override
	public WebDriver getWebDriver() {
	    return webdriver;
	}
}
