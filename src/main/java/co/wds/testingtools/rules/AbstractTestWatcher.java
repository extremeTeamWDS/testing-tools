package co.wds.testingtools.rules;

import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

abstract public class AbstractTestWatcher extends TestWatcher {
    protected Description description;
    private String testName;

    @Override
    protected void starting(Description description) {
        super.starting(description);
        System.out.println("\n\n\n*** STARTING " + description.getDisplayName() + " ***\n");
        this.description = description;
        testName = description.getClassName() + "." + description.getMethodName();
    }
    
    @Override
    public void failed(Throwable t, Description description) {
        System.out.println("\n*** FAILED " + description.getDisplayName() + " ***\n\n");
        super.failed(t, description);
    }
    
    protected void succeeded(Description description) {
        System.out.println("\n*** SUCCEEDED " + description.getDisplayName() + " ***\n\n");
        super.succeeded(description);
    }
    
    public Description getDescription() {
        return description;
    }
    
    public String getTestName() {
        return testName;
    }
}
