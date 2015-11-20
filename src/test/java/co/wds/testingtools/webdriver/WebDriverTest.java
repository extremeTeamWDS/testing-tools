package co.wds.testingtools.webdriver;

import static co.wds.testingtools.annotations.MapperServlet.getPort;
import static co.wds.testingtools.annotations.MapperServlet.TestServlet.ANY_FREE_PORT;
import static co.wds.testingtools.webdriver.Conditions.javascript;
import static co.wds.testingtools.webdriver.Conditions.newWindowIsOpened;
import static co.wds.testingtools.webdriver.Conditions.overlap;
import static co.wds.testingtools.webdriver.Functions.comparison;
import static co.wds.testingtools.webdriver.Functions.contains;
import static co.wds.testingtools.webdriver.Functions.isEnabled;
import static co.wds.testingtools.webdriver.Functions.isEqualTo;
import static co.wds.testingtools.webdriver.Functions.isNull;
import static co.wds.testingtools.webdriver.Functions.not;
import static co.wds.testingtools.webdriver.Functions.optionIsSelected;
import static co.wds.testingtools.webdriver.Functions.Operator.EQUAL_TO;
import static co.wds.testingtools.webdriver.Functions.Operator.GREATER_THAN;
import static co.wds.testingtools.webdriver.Functions.Operator.LESS_THAN;
import static co.wds.testingtools.webdriver.WebDriverManager.getExternalAddress;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.ObjectUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import co.wds.testingtools.FunctionalTestRunningRule;
import co.wds.testingtools.annotations.MapperServlet.RespondTo;
import co.wds.testingtools.annotations.MapperServlet.ResponseData;
import co.wds.testingtools.annotations.MapperServlet.TestServlet;
import co.wds.testingtools.webdriver.Conditions.ContextCondition;
import co.wds.testingtools.webdriver.Functions.Operator;

@TestServlet(port=ANY_FREE_PORT)
@RespondTo({
    @ResponseData(url=WebDriverTest.TEST_PAGE_URL, resourceFile="webDriverTest.html"),
    @ResponseData(url="test", resourceFile="test.html")
})
public class WebDriverTest extends AbstractWebDriverTest {
    
    private static final String DATA_HIGHLIGHTED = "data-highlighted";

    public static final String TEST_PAGE_URL = "webDriverTest";

    @Rule
    public FunctionalTestRunningRule functionalRule = new FunctionalTestRunningRule();

    /**
     * assertEquals(new Pair("a", "b"), new Pair("b", "a")) 
     */
    static class Pair<T> {
        public final T value1;
        public final T value2;
        public Pair(T value1, T value2) {
            this.value1 = value1;
            this.value2 = value2;
        }
        
        @Override
        public int hashCode() {
            return ObjectUtils.hashCode(value1) + ObjectUtils.hashCode(value2);
        }
        
        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || !(obj instanceof Pair)) {
                return false;
            }
            Pair<?> that = (Pair<?>) obj;
            return ObjectUtils.equals(this.value1, that.value1) && ObjectUtils.equals(this.value2, that.value2)
                    || ObjectUtils.equals(this.value2, that.value1) && ObjectUtils.equals(this.value1, that.value2);
        }
    }
    
    private void setHighlighted(By selector, boolean highlighted) {
        WebElement element = webdriver.findElement(selector);
        runJs("arguments[0].setAttribute(arguments[1], arguments[2]);", 
                element, DATA_HIGHLIGHTED, String.valueOf(highlighted));
        ContextCondition<String> condition = Conditions.element(selector).attribute(DATA_HIGHLIGHTED).satisfies(isEqualTo(String.valueOf(highlighted)));
        waitFor(condition);
    }

    private void testOverlap(By selector1, By selector2, boolean overlap) {
        setHighlighted(selector1, true);
        setHighlighted(selector2, true);
        waitFor(overlap(selector1, selector2, overlap));
        setHighlighted(selector1, false);
        setHighlighted(selector2, false);
    }

    protected void GET(String uri) {
        String baseUrl = "http://" + getExternalAddress() + ":" + getPort();
        super.GET(baseUrl + uri);
    }

    @Before
    public void init() {
        GET("/" + TEST_PAGE_URL);
        waitFor(Conditions.element(By.tagName("h1")).text().satisfies(isEqualTo("Web Driver Utilities Test")));
    }

    @Test
    public void testAttributes() {
        By targetElementSelector = By.id("attributeChangeTarget");
        waitFor(Conditions.element(targetElementSelector)
                .attribute(DATA_HIGHLIGHTED).satisfies(isNull()));

        click(By.id("setAttributeButton"));
        String actualAttribute = (String) waitFor(Conditions.element(targetElementSelector).attribute(DATA_HIGHLIGHTED)
                .satisfies(isEqualTo("true"))
                .satisfies(not(contains("false")))
                .satisfies(not(isNull())));
        assertEquals("true", actualAttribute);

        typeText(By.id("attributeValueInput"), "false");
        click(By.id("setAttributeButton"));
        actualAttribute = (String) waitFor(Conditions.element(targetElementSelector).attribute(DATA_HIGHLIGHTED)
                .satisfies(isEqualTo("false"))
                .satisfies(not(contains("true"))));
        assertEquals("false", actualAttribute);
    }

    @Test
    public void testPopup() {
        String currentWindow = webdriver.getWindowHandle();
        try {
            String newWindow = waitFor(newWindowIsOpened(new Runnable() {
                public void run() {
                    click(By.id("openNewWindowButton"));
                }
            }));
            webdriver.switchTo().window(newWindow);

            waitFor(Conditions.element(By.tagName("h1")).text()
                    .satisfies(isEqualTo("This is a test html")));
            waitFor(Conditions.pageSource().satisfies(contains("test comment")));
        } finally {
            webdriver.switchTo().window(currentWindow);
        }
    }

    @Test
    public void testXPath() {
        for (int i = 1; i <= 3; i++) {
            waitFor(Conditions.element(XPath.getSelector(
                    "//div[html::hasClass('xpath-test')]//div[html::hasClass('abc" + i +"')]")).count().satisfies(isEqualTo(1)));
        }
    }

    @Test
    public void testOverlap() {
        String overlapPrefix = ".overlap .rect";
        By centralRectSelector = By.cssSelector(overlapPrefix + 22);
        int[] rectIndices = {11, 12, 13, 23, 33, 32, 31, 21};
        for (int i = 0; i < rectIndices.length; i++) {
            int thisIndex = rectIndices[i];
            By thisSelector = By.cssSelector(overlapPrefix + thisIndex);
            if (i > 0) {
                int previousIndex = rectIndices[i - 1];
                By previousSelector = By.cssSelector(overlapPrefix + previousIndex);
                testOverlap(previousSelector, thisSelector, true);
            }
            int row = thisIndex / 10;
            int column = thisIndex % 10;
            testOverlap(centralRectSelector, thisSelector, (row + column) % 2 == 0);
        }
    }

    @Test
    public void testDoesNotOverlap() {
        String noOverlapPrefix = ".no-overlap .rect";
        String[] rectIndices = {"11", "12", "13", "21", "22", "23", "31", "32", "33"};
        Set<Pair<By>> allRectPairs = new HashSet<Pair<By>>(rectIndices.length * (rectIndices.length - 1));
        for (int i = 0; i < rectIndices.length; i++) {
            for (int j = 0; j < rectIndices.length; j++) {
                if (i != j) {
                    By selector1 = By.cssSelector(noOverlapPrefix + rectIndices[i]);
                    By selector2 = By.cssSelector(noOverlapPrefix + rectIndices[j]);
                    Pair<By> pair = new Pair<By>(selector1, selector2);
                    if (!allRectPairs.contains(pair)) {
                        allRectPairs.add(pair);
                        testOverlap(selector1, selector2, false);
                    }
                }
            }
        }
    }

    @Test
    public void testConditions() {
        waitFor(Conditions.element(By.id("runButton")).satisfies(not(isEnabled())));
        select(By.id("browserSelectBox"), "firefox");
        waitFor(Conditions.element(By.id("runButton")).satisfies(isEnabled()));
        typeText(By.id("versionInput"), "111");
        click(By.id("runButton"));

        waitFor(Conditions.element(By.id("outputList")).text().satisfies(isEqualTo("Your tests passed successfully on firefox v.111")));
        By itemSelector = By.cssSelector("ul#outputList li");
        waitFor(Conditions.element(itemSelector).count().satisfies(isEqualTo(1))); // Operator.EQUAL_TO, 
 
        click(By.id("runButton"));
        int actualCount = (Integer) waitFor(Conditions.element(itemSelector).count()
                .satisfies(isEqualTo(2))
                .satisfies(comparison(GREATER_THAN,  1))
                .satisfies(comparison(EQUAL_TO, 2))
                .satisfies(comparison(LESS_THAN, 3)));
        assertEquals(2, actualCount);

        click(By.id("resetButton"));
        waitFor(Conditions.element(By.id("browserSelectBox")).satisfies(optionIsSelected("")));
        waitFor(Conditions.element(By.id("versionInput")).attribute("value").satisfies(isEqualTo("")));
        waitFor(javascript("return 1 + 1 === 2;"));
        assertEquals("ab", runJs("return 'a' + 'b';"));
    }
}
