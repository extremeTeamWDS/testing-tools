package co.wds.testingtools.server;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;

public class MockServer implements AutoCloseable {
    public static final int DEFAULT_PORT = 1090;

    public final Server server;
    public final int port;
    public final ServerContext serverContext = new ServerContext();

    private ContextHandlerCollection handlers = new ContextHandlerCollection();

    Handler createContextHandler(String path, Handler handler) {
        ContextHandler contextHandler = new ContextHandler(path);
        contextHandler.setHandler(handler);
        return contextHandler;
    }

    public MockServer(int port) throws MalformedURLException, IOException {
        this.port = port;
        System.out.println("MockServer(" + port + ")");
        server = new Server(port);

        handlers.addHandler(createContextHandler("/generic", new GenericHandler(serverContext) {
            @Override
            protected void handleGet(HandlingContext context) throws ServletException, IOException {
                try {
                    File template = new File(MockServer.this.getClass().getResource("/data/genericHandlerTest.html").toURI());
                    Map<String, String[]> parameterMap = context.request.getParameterMap();
                    Map<String, String> params = new HashMap<String, String>();
                    for (Map.Entry<String, String[]> param : parameterMap.entrySet()) {
                        params.put(param.getKey(), param.getValue()[0]);
                    }
                    renderTemplate(context, template, Collections.<String, Object>singletonMap("params", params.entrySet()));
                } catch (URISyntaxException e) {
                    throw new ServletException(e);
                }
            }
        }));
        
        server.setHandler(handlers);
    }

    public MockServer() throws MalformedURLException, IOException {
        this(DEFAULT_PORT);
    }

    public void close() throws Exception {
        server.stop();
    }

    public static void main(String[] args) throws Exception {
        final MockServer mockServer = new MockServer();
        mockServer.server.start();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                try {
                    mockServer.close();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }
}