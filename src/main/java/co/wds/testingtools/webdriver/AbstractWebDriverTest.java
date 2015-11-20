package co.wds.testingtools.webdriver;

import static co.wds.testingtools.webdriver.Conditions.javascript;
import static java.lang.String.format;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.runner.Description;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.Logs;
import org.openqa.selenium.remote.Augmenter;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import co.wds.testingtools.rules.AbstractTestWatcher;
import co.wds.testingtools.rules.ConditionalIgnoreRule;

public abstract class AbstractWebDriverTest {
	
	private static Logger logger = LoggerFactory.getLogger(AbstractWebDriverTest.class);
    
	public static final File SCREENSHOTS_DIR_PATH = new File("./logs/screenshots/");
	private static final File WEBDRIVER_LOGS_DIR_PATH = new File("./logs/webdriver_logs/");
	protected WebDriver webdriver;
	protected static String baseUrl;

	protected static WebDriverManager lifecycle = new WebDriverManager();

	protected static final int TIMEOUT = Integer.valueOf(System.getProperty("webdriver.wait.timeout", "12")); // seconds

    @Rule
    public ConditionalIgnoreRule rule = new ConditionalIgnoreRule(); // TODO merge it with FunctionalTestRunningRule?

    @Rule
    public AbstractTestWatcher functionalRule = new AbstractTestWatcher() {
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
	    baseUrl = WebDriverManager.getBaseUrl();
		webdriver = lifecycle.getWebdriverForNewTest();
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

	protected <T> T waitFor(ExpectedCondition<T> condition) {
	    return waitFor(condition, TIMEOUT, TimeUnit.SECONDS);
	}

    protected <T> T waitFor(ExpectedCondition<T> condition, int timeout, TimeUnit unit) {
        return new WebDriverWait(webdriver, unit.toSeconds(timeout)).until(condition);
    }

    protected void select(By selector, String option) {
        WebElement selectElement = waitFor(visibilityOfElementLocated(selector));
        new Select(selectElement).selectByValue(option);
    }

    protected void typeText(By inputSelector, String text) {
        WebElement inputElement = waitFor(visibilityOfElementLocated(inputSelector));
        inputElement.clear();
        inputElement.sendKeys(text);
    }

    protected void click(final By byLocator) {
        waitFor(new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver driver) {
                WebElement element = driver.findElement(byLocator);
                if (element.isDisplayed()) {
                    try {
                        element.click();
                        return true;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return false;
            }

            @Override
            public String toString() {
                return "Element located " + byLocator + " clicked";
            }
        });
    }

	protected void takeScreenshot(String screenshotName) {
		WebDriver driver = new Augmenter().augment(webdriver);
		if (driver instanceof TakesScreenshot) {
			File tempFile = null;
			SCREENSHOTS_DIR_PATH.mkdirs();
			File destFile = new File(SCREENSHOTS_DIR_PATH, screenshotName + ".png");
			try {
				tempFile = ((TakesScreenshot)driver).getScreenshotAs(OutputType.FILE);
				FileUtils.copyFile(tempFile, destFile);
			} catch (IOException e) {
				String message = format("Unable to copy file %s to %s", tempFile != null ? tempFile.getAbsolutePath() : null,
						destFile.getAbsolutePath());
                logger.error(message, e);
			} catch (Exception e) {
				logger.error("Cannot take screenshot", e);
			}
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

	protected <Result> Result runJs(String script, Object ... args) {
	    return (Result) ((JavascriptExecutor) webdriver).executeScript(script, args);
	}
}
