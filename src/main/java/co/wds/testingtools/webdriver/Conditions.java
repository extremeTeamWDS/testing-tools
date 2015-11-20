package co.wds.testingtools.webdriver;

import static org.apache.commons.lang3.StringUtils.join;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;

import com.google.common.base.Function;

public class Conditions {
    public static interface ContextSupplier<Context, Result> {
        Result get(Context context);
    }

    abstract public static class ContextCondition<Context> implements ExpectedCondition<Object>, ContextSupplier<WebDriver, Context> {
        protected final List<Function<Object,Boolean>> conditions = new ArrayList<Function<Object, Boolean>>();
        public ContextCondition<Context> satisfies(Function<Object, Boolean> condition) {
            conditions.add(condition);
            return this;
        }

        @Override
        final public Object apply(WebDriver webDriver) {
            Context context = this.get(webDriver);
            for (Function<Object, Boolean> condition : conditions) {
                Object result = condition.apply(context);
                if (result == null || result == Boolean.FALSE) {
                    return null;
                }
            }
            return context != null ? context : !conditions.isEmpty();
        }

        @Override
        public String toString() {
            return "should satisfy conditions: [" + join(conditions.toArray()) + "]";
        }
    }

    public static class WebElementCondition extends ContextCondition<WebElement> {
        private final By selector;
        
        /*
         * use element(By)
         */
        private WebElementCondition(By selector) {
            this.selector = selector;
        }

        @Override
        public WebElement get(WebDriver driver) {
            return driver.findElement(selector);
        }
        
        @Override
        public String toString() {
            return "Element located " + selector + " " + super.toString();
        }
        
        public ContextCondition<String> attribute(final String attributeName) {
            return new ContextCondition<String>() {
                String attributeValue;
                @Override
                public String get(WebDriver webDriver) {
                    WebElement element = WebElementCondition.this.get(webDriver);
                    attributeValue = element != null ? element.getAttribute(attributeName) : null;
                    return attributeValue;
                }
                
                @Override
                public String toString() {
                    return "Attribute '" + attributeName + "' of the Elemennt located " + WebElementCondition.this.selector + " " + super.toString() + ", it's latest value is '" + attributeValue + "'";
                }
            };
        }

        public ContextCondition<String> text() {
            return new ContextCondition<String>() {
                String latestText;
                @Override
                public String get(WebDriver webDriver) {
                    WebElement element = WebElementCondition.this.get(webDriver);
                    latestText = element != null ? element.getText() : null;
                    return latestText;
                }
                
                @Override
                public String toString() {
                    return "Text of of the Element located " + WebElementCondition.this.selector + " " + super.toString()
                        + "; it's latest value is '" + latestText + "'";
                }
            };
        }

        public ContextCondition<Integer> count() {
            return new ContextCondition<Integer>() {
                int latestCount;
                
                @Override
                public Integer get(WebDriver webDriver) {
                    return (latestCount = webDriver.findElements(selector).size());
                }

                @Override
                public String toString() {
                    return "Count of the Element located " + WebElementCondition.this.selector + " " + super.toString() + "; latest count is: " + latestCount;
                }
            };
        }
    }
    
    public static WebElementCondition element(By selector) {
        return new WebElementCondition(selector);
    }

    public static ContextCondition<String> pageSource() {
    	return new ContextCondition<String>() {
			@Override
			public String get(WebDriver webdriver) {
				return webdriver.getPageSource();
			}

            @Override
            public String toString() {
                return "Page source " + super.toString();
            }
    	};
    }

    public static ContextCondition<String> url() {
        return new ContextCondition<String>() {
            String latestUrl;
            @Override
            public String get(WebDriver webdriver) {
                return (latestUrl = webdriver.getCurrentUrl());
            }

            @Override
            public String toString() {
                return "Page url " + super.toString() + ", latest value is '" + latestUrl + "'";
            }
        };
    }

    public static ExpectedCondition<String> newWindowIsOpened(final Runnable actionToOpenNewWindow) {
        return new ExpectedCondition<String>() {
            Set<String> currentWindowHandles;
            @Override
            public String apply(WebDriver webDriver) {
                if (currentWindowHandles == null) {
                    currentWindowHandles = new HashSet<String>(webDriver.getWindowHandles());
                    actionToOpenNewWindow.run();
                }

                Set<String> newWindowHandles = webDriver.getWindowHandles();
                if (currentWindowHandles.size() < newWindowHandles.size()) {
                    Set<String> diff = new HashSet<String>(newWindowHandles);
                    diff.removeAll(currentWindowHandles);
                    return diff.iterator().next();
                }
                return null;
            }

            @Override
            public String toString() {
                return "Condition[new window has to be opened after " + actionToOpenNewWindow + "]";
            }
        };
    }
    
    public static <Result> ExpectedCondition<Result> javascript(final String script) {
        return new ExpectedCondition<Result>() {
            @SuppressWarnings("unchecked")
			@Override
            public Result apply(WebDriver driver) {
                return (Result) ((JavascriptExecutor) driver).executeScript(script);
            }
            
            @Override
            public String toString() {
                return "Condition[javascript(" + script + ")]";
            }
        };
    }

    public static ExpectedCondition<Boolean> overlap(final By selector1, final By selector2, final boolean expectedOverlap) {
        return new ExpectedCondition<Boolean>() {
            Point location1;
            Dimension size1;
            Point location2;
            Dimension size2;

            public Boolean apply(WebDriver driver) {
                WebElement element1 = driver.findElement(selector1);
                WebElement element2 = driver.findElement(selector2);
                location1 = element1.getLocation();
                size1 = element2.getSize();
                location2 = element2.getLocation();
                size2 = element2.getSize();
                int x11 = location1.x;
                int x12 = x11 + size1.width;
                int y11 = location1.y;
                int y12 = y11 + size1.height;
                
                int x21 = location2.x;
                int x22 = x21 + size2.width;
                int y21 = location2.y;
                int y22 = y21 + size2.height;

                boolean actualOverlap = !(x11 >= x22 || x12 <= x21 || y11 >= y22 || y12 <= y21);
                return expectedOverlap == actualOverlap;
            }
            
            @Override
            public String toString() {
                return "Condition[" + selector1 + "(with location " + location1 + " and size " + size1 + ")"
                        + " should " + (expectedOverlap ? "" : "not") + " overlap with  " + selector2 + "(with location "
                        + location2+ " and size " + size2 + ")]";
            }
        };
    }
}
