package co.wds.testingtools.rules;

import co.wds.testingtools.rules.ConditionalIgnoreRule.ConditionalIgnore;

@ConditionalIgnore(condition = JavaScriptIgnore.class)
public class AbstractConditionalIgnoreRuleTest {
}
