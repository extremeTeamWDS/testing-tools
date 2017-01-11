package co.wds.testingtools;

import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import co.wds.testingtools.annotations.MapperServlet;
import co.wds.testingtools.annotations.MapperServlet.RespondTo;

/*
 * Looks like Custom TestRunner doesn't work with Play 2.2-Java and TestRule does.
 * Just put an instance of this rule into a class:
 *  @Rule
 *  public FunctionalTestRunningRule functionalRule = new FunctionalTestRunningRule();
 *
 *  Do not mix it with MapperServletTestRunner
 */
public class FunctionalTestRunningRule extends TestWatcher {
    private void addMapping(MapperServlet.RespondTo respondTo) {
        if (respondTo != null) {
            addMapping(respondTo.value());
        }
    }

    private void addMapping(MapperServlet.ResponseData ... entries) {
        if (entries != null) {
            for (MapperServlet.ResponseData entry : entries) {
                if (entry != null) {
                    MapperServlet.addNewMapping(entry);
                }
            }
        }
    }

    @Override
    public Statement apply(final Statement base, final Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                MapperServlet.startMapperServlet(description.getTestClass());
                try {
                    addMapping(description.getTestClass().getAnnotation(MapperServlet.ResponseData.class));
                    addMapping(description.getTestClass().getAnnotation(RespondTo.class));
                    addMapping(description.getAnnotation(MapperServlet.ResponseData.class));
                    addMapping(description.getAnnotation(RespondTo.class));
                    
                    base.evaluate();
                } finally {
                    MapperServlet.stopMapperServlet();
                }
            }
        };
    }

}
