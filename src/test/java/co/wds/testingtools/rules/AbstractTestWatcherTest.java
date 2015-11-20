package co.wds.testingtools.rules;

import static org.junit.Assert.assertEquals;

import org.junit.Rule;
import org.junit.Test;

public class AbstractTestWatcherTest {
    @Rule
    public AbstractTestWatcher watcher = new AbstractTestWatcher() {};
    
    @Test
    public void testDescription() {
        assertEquals(this.getClass().getName(), watcher.description.getClassName());
        assertEquals("testDescription", watcher.description.getMethodName());
        assertEquals(this.getClass().getName() + ".testDescription", watcher.getTestName());
    }
}
