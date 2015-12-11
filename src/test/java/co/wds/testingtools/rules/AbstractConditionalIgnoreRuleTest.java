package co.wds.testingtools.rules;

@ConditionalIgnore.Expression("description.getMethodName().contains('IgnoreBySuperclass')")
public class AbstractConditionalIgnoreRuleTest {
}
