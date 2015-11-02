package co.wds.testingtools;

import org.junit.Rule;

public class FunctionalTestRunningRuleTest extends AbstractRunnerTest {
    @Rule
    public FunctionalTestRunningRule functionalRule = new FunctionalTestRunningRule();
}
