package co.wds.testingtools.webdriver;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openqa.selenium.By;

public class XPath {
    /*
     * Transforms xpath function html::hasClass('className') into valid xpath expression, e.g.:
     * //a[html::hasClass('link')]
     * into:
     * //a[contains(concat(' ', normalize-space(@class), ' '), ' className ')]
     */
    private static String resolveHasClassFunc(String xpath) {
        Pattern p = Pattern.compile("html::hasClass\\('([^']+)'\\)");
        Matcher m = p.matcher(xpath);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            m.appendReplacement(sb, "contains(concat(' ', normalize-space(@class), ' '), ' ");
            sb.append(m.group(1));
            sb.append(" ')");
        }
        m.appendTail(sb);
        return sb.toString();
    }

    public static By getSelector(String xpath) {
        xpath = resolveHasClassFunc(xpath);        
        return By.xpath(xpath);
    }
}