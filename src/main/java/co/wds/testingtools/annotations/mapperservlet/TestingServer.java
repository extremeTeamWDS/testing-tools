package co.wds.testingtools.annotations.mapperservlet;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.openqa.selenium.io.IOUtils;

public class TestingServer {

	ServletContextHandler handler;
    public final int port;
	
	public TestingServer(int port) {
	    if (port <= 0) {
	        int minPort = Integer.parseInt(System.getProperty("testing.server.min.port", "-1"));
	        int maxPort = Integer.parseInt(System.getProperty("testing.server.max.port", "-1"));
	        if (minPort > 0 && maxPort > 0) {
	            this.port = getFreePort(minPort, maxPort);
	        } else {
	            this.port = getFreePort();
	        }
	    } else {
	        this.port = port;
	    }
	    
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

    protected static int getFreePort(int minPort, int maxPort) {
        for (int i = minPort; i<= maxPort; i++) {
            if (isPortAvailable(i)) {
                return i;
            }
        }
        return -1;
    }

    private static boolean isPortAvailable(final int port) {
        ServerSocket ss = null;
        try {
            ss = new ServerSocket(port);
            ss.setReuseAddress(true);
            return true;
        } catch (final IOException e) {
        } finally {
            if (ss != null) {
                try {
                    ss.close();
                } catch (Exception e) {
                }
            }
        }

        return false;
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
