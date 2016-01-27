package co.wds.testingtools.server;

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.net.MediaType;

abstract public class GenericHandler extends AbstractHandler {
    protected static final String ALLOW_ORIGIN_HEADER = "Access-Control-Allow-Origin";
    
    protected final ServerContext serverContext;

    public GenericHandler(ServerContext context) {
        this.serverContext = context;
    }

    protected LoadingCache<File, Pair<Mustache, Long>> templates = CacheBuilder.newBuilder().maximumSize(100).build(
            new CacheLoader<File, Pair<Mustache, Long>>() {
                public Pair<Mustache, Long> load(File templateFile) throws IOException {
                    MustacheFactory mf = new DefaultMustacheFactory();
                    InputStreamReader templateReader = new FileReader(templateFile);
                    try {
                        Mustache m = mf.compile(templateReader, templateFile.getAbsolutePath());
                        return Pair.of(m, templateFile.lastModified());
                    } finally {
                        IOUtils.closeQuietly(templateReader);
                    }
                }
            });

    Mustache getTemplate(File templateFile) throws FileNotFoundException, ExecutionException {
        Pair<Mustache, Long> mustache = templates.get(templateFile);
        if (mustache.getRight().longValue() < templateFile.lastModified()) {
            templates.invalidate(templateFile);
            mustache = templates.get(templateFile);
        }
        return mustache.getLeft();
    }

    protected void setContentType(HandlingContext context, MediaType mediaType) {
        context.response.setHeader("Content-Type", mediaType.toString());
    }

    protected void renderTemplate(HandlingContext context, File templateFile, Map<String, ?> scopes) throws IOException, ServletException {
        try {
            if (templateFile.exists()) {
                Map<String, Object> templateScopes = new HashMap<String, Object>(serverContext.params);
                templateScopes.putAll(scopes);
                Mustache template = getTemplate(templateFile); 
                StringWriter writer = new StringWriter();
                template.execute(writer, templateScopes);
                writer.flush();
                renderText(context, HttpServletResponse.SC_OK, writer.toString());
            } else {
                renderText(context, SC_BAD_REQUEST, "File does not exist: " + templateFile);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServletException(e);
        }
    }

    protected void renderText(HandlingContext context, int status, Object content) throws IOException {
        if (status != HttpServletResponse.SC_OK) {
            new Exception("Error: " + status + ", " + content).printStackTrace();
        }
        if (content instanceof Throwable) {
            ((Throwable) content).printStackTrace(context.response.getWriter());
        } else {
            context.response.getWriter().println(content);
        }
        context.response.setStatus(status);
        context.baseRequest.setHandled(true);
    }

    protected void setCorsHeaders(HandlingContext context) {
        context.response.setHeader("Access-Control-Allow-Origin", "*");
        context.response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
        context.response.setHeader("Access-Control-Allow-Headers", "Authorization,Content-Type,Accept");
    }

    protected void handleGet(HandlingContext context) throws ServletException, IOException {
        renderText(context, SC_BAD_REQUEST, "Unsupported method: " + context.method);
    }

    protected void handlePost(HandlingContext context) throws IOException, ServletException {
        renderText(context, SC_BAD_REQUEST, "Unsupported method: " + context.method);
    }

    protected void handleOptions(HandlingContext context) throws IOException {
        setCorsHeaders(context);
        renderText(context, HttpServletResponse.SC_OK, "");
    }

    @Override
    public final void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        System.out.println("AbstractRestHandler.handle()");
        HandlingContext context = new HandlingContext(target, baseRequest, request, response);
        try {
            String method = baseRequest.getMethod();
            if (method.equals("GET")) {
                handleGet(context);
            } else if (method.equals("POST")) {
                handlePost(context);
            } else if (method.equals("OPTIONS")) {
                handleOptions(context);
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
            renderText(context, SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}
