package co.wds.testingtools.server;

import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;

import co.wds.testingtools.webdriver.AbstractWebDriverTest;

public class GenericHandlerTest extends AbstractWebDriverTest {
    
    int port;
    MockServer mockServer;

    @Before
    public void init() throws Exception {
        mockServer = new MockServer(getAppPort());
        mockServer.server.start();
    }

    @After
    public void clear() throws Exception {
        mockServer.close();
    }

    private void assertCell(String className, String content) {
        waitFor(visibilityOfElementLocated(By.xpath("//td[contains(@class, '" + className + "') and contains(text(), '" + content + "')]")));
    }

    @Test
    public void testGet() {
        GET("/generic/?key1=value1&key2=value2");
        assertCell("key", "key1");
        assertCell("value", "value1");
        assertCell("key", "key2");
        assertCell("value", "value2");
    }
}
