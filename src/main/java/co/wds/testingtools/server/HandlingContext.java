package co.wds.testingtools.server;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;

//TODO this is a copy-paste from demo-wds. Put it into testing-tools and reuse
public class HandlingContext {
    public final String target;
    public final Request baseRequest;
    public final HttpServletRequest request;
    public final HttpServletResponse response;
    public final String pathInfo;
    public final String method;

    public HandlingContext(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) {
        this.target = target;
        this.baseRequest = baseRequest;
        this.request = request;
        this.response = response;
        this.pathInfo = baseRequest.getPathInfo();
        this.method = baseRequest.getMethod();
    }
    
    public Cookie getCookie(String cookieName) {
        Cookie[] cookies = baseRequest.getCookies();
        for (Cookie cookie : cookies) {
            if (cookieName.equals(cookie.getName())) {
                return cookie;
            }
        }
        return null;
    }
}
