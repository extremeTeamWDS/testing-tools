package co.wds.testingtools.annotations.mapperservlet;

import static co.wds.testingtools.Property.getProperty;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;

public class TestingServer {
    public final static int SERVER_MIN_PORT = getProperty("testing.server.min.port", Integer.class, "-1");
    public final static int SERVER_MAX_PORT = getProperty("testing.server.max.port", Integer.class, "-1");

	ServletContextHandler handler;
    public final int port;
	
	public TestingServer(int port) {
	    if (port <= 0) {
	        this.port = getFreePort(SERVER_MIN_PORT, SERVER_MAX_PORT);
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

    public static int getFreePort() {
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

    public static int getFreePort(int minPort, int maxPort) {
        if (minPort > 0 && maxPort > 0) {
            for (int i = minPort; i<= maxPort; i++) {
                if (isPortAvailable(i)) {
                    return i;
                }
            }
            return -1;
        } else {
            return getFreePort();
        }
    }

    public static boolean isPortAvailable(final int port) {
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
