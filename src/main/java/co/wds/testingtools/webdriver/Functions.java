package co.wds.testingtools.webdriver;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.Select;

import com.google.common.base.Function;

public class Functions {
    public static enum Operator {
        GREATER_THAN(1),
        EQUAL_TO(0),
        LESS_THAN(-1);

        protected final int comparisonResult;
        Operator(int comparisonResult) {
            this.comparisonResult = comparisonResult;
        }

        public <T extends Comparable<T>> boolean apply(T v1, T v2) {
            return (int) Math.signum(v1.compareTo(v2)) == comparisonResult;
        }
    }

    public static <T extends Comparable<T>> Function<Object, Boolean> comparison(final Operator operator, final T baseValue) {
        return new Function<Object, Boolean>() {
            public Boolean apply(Object actualValue) {
                return operator.apply((T) actualValue, baseValue);
            }

            @Override
            public String toString() {
                return "is " + operator + " '" + baseValue + "'";
            }
        };
    }

    public static Function<Object, Boolean> isEqualTo(final Object expectedValue) {
        return new Function<Object, Boolean>() {
            @Override
            public Boolean apply(Object actualValue) {
                return ObjectUtils.equals(expectedValue, actualValue);
            }

            @Override
            public String toString() {
                return "to be equal to '" + expectedValue + "'";
            }
        };
    }

    public static Function<Object, Boolean> contains(final String substring) {
        return new Function<Object, Boolean>() {
            @Override
            public Boolean apply(Object actualString) {
                return StringUtils.contains((String) actualString, substring);
            }

            @Override
            public String toString() {
                return "should contain '" + substring + "'";
            }
        };
    }

    public static final Function<Object, Boolean> endsWith(final String expectedSuffix) {
        return new Function<Object, Boolean>() {
            @Override
            public Boolean apply(Object str) {
                return ((String) str).endsWith(expectedSuffix);
            }

            @Override
            public String toString() {
                return String.format("should end with '%s']", expectedSuffix);
            }
        };
    }

    public static final Function<Object, Boolean> isNull() {
        return new Function<Object, Boolean>() {
            @Override
            public Boolean apply(Object value) {
                return value == null;
            }

            @Override
            public String toString() {
                return "has to be null";
            }
        };
    }

    public static final Function<Object, Boolean> not(final Function<Object, Boolean> predicate) {
        return new Function<Object, Boolean>() {
            @Override
            public Boolean apply(Object value) {
                return value != null ? !predicate.apply(value) : null;
            }
            
            @Override
            public String toString() {
                return "not (" + predicate.toString() + ")";
            }
        };
    }

    public static Function<Object, Boolean> isEnabled() {
        return new Function<Object, Boolean>() {
            @Override
            public Boolean apply(Object element) {
                return element != null && ((WebElement) element).getAttribute("disabled") == null;
            }

            @Override
            public String toString() {
                return "has to be enabled";
            }
        };
    }

    protected static Function<Object, Boolean> optionIsSelected(final String expectedSelectedOption) {
        return new Function<Object, Boolean>() {
            private String actualSelectedOption;

            @Override
            public Boolean apply(Object element) {
                if (element != null) {
                    Select selectBox = new Select((WebElement) element);
                    actualSelectedOption = selectBox.getFirstSelectedOption().getAttribute("value");
                    return StringUtils.equals(actualSelectedOption, expectedSelectedOption);
                } else {
                    return false;
                }
            }

            @Override
            public String toString() {
                return "Option '" + expectedSelectedOption + "' should be selected, latest selected option is '" + actualSelectedOption + "']";
            }
        };
    }

    public static final Function<Object, Boolean> isVisible() {
        return new Function<Object, Boolean>() {
            @Override
            public Boolean apply(Object element) {
                return element != null && ((WebElement) element).isDisplayed();
            }

            @Override
            public String toString() {
                return "has to be visible";
            }
        };
    }
}
