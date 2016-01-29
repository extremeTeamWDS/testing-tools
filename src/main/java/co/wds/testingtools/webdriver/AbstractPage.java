package co.wds.testingtools.webdriver;

import static co.wds.testingtools.webdriver.WebDriverManager.LOGS_BASE_DIR;
import static java.lang.String.format;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.Augmenter;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract public class AbstractPage {
    private static Logger logger = LoggerFactory.getLogger(AbstractPage.class);
    public static final File SCREENSHOTS_DIR_PATH = new File(LOGS_BASE_DIR, "screenshots/");
    protected static final int TIMEOUT = Integer.valueOf(System.getProperty("webdriver.wait.timeout", "12")); // seconds

    abstract public WebDriver getWebDriver();

    public <T> T waitFor(ExpectedCondition<T> condition) {
        return waitFor(condition, TIMEOUT, TimeUnit.SECONDS);
    }

    public <T> T waitFor(ExpectedCondition<T> condition, int timeout, TimeUnit unit) {
        return new WebDriverWait(getWebDriver(), unit.toSeconds(timeout)).until(condition);
    }

    public void select(By selector, String option) {
        WebElement selectElement = waitFor(visibilityOfElementLocated(selector));
        new Select(selectElement).selectByValue(option);
    }

    public void typeText(By inputSelector, String text) {
        WebElement inputElement = waitFor(visibilityOfElementLocated(inputSelector));
        inputElement.clear();
        inputElement.sendKeys(text);
    }

    public void click(final By byLocator) {
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

    public void takeScreenshot(String screenshotName) {
        WebDriver driver = new Augmenter().augment(getWebDriver());
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

    public <Result> Result runJs(String script, Object ... args) {
        return (Result) ((JavascriptExecutor) getWebDriver()).executeScript(script, args);
    }
}
