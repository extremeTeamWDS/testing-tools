package co.wds.testingtools.annotations.mapperservlet;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;

public class TestingServer {

	ServletContextHandler handler;

    public final int port;
	
	public TestingServer(int port) {
	    this.port = port <= 0 ? getFreePort() : port;
		server = new Server(this.port);
        handler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        handler.setContextPath("/"); // technically not required, as "/" is the default
        server.setHandler(handler);
	}

	private Server server;

	private List<String> sessionStartRequests = new ArrayList<String>();

    protected static int getFreePort() {
        ServerSocket s = null;
        try {
            s = new ServerSocket(0);
            return s.getLocalPort();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (s != null) {
                try {
                    s.close();
                } catch (IOException e1) {
                    throw new RuntimeException(e1);
                }
            }
        }
    }

	public void start() throws Exception {
		server.start();
	}

	public void stop() throws Exception {
		server.stop();
	}
	
	public List<String> getSessionStartRequests() {
		return sessionStartRequests ;
	}

	public ServletContextHandler getHandler() {
		return handler;
	}

}
