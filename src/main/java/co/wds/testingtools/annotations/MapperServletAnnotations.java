package co.wds.testingtools.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;

import org.eclipse.jetty.servlet.ServletHolder;

import co.wds.testingtools.annotations.mapperservlet.AnnotationMapperServlet;
import co.wds.testingtools.annotations.mapperservlet.Request;
import co.wds.testingtools.annotations.mapperservlet.TestingServer;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

public class MapperServletAnnotations {

	private static TestingServer server;
	private static AnnotationMapperServlet mapperServlet;

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE)
	public @interface TestServlet {
		int port() default 80;

		String contentType() default "application/json";

		boolean requiresAuthentication() default false;

		String userName() default "";

		String password() default "";
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE)
	public @interface RespondTo {
		ResponseData[] value();
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE)
	public @interface ResponseData {
		String url() default "/";

		String resourceFile();

		String contentType() default "";

		int status() default 200;

		boolean ignoreParams() default false;
	}

	public static void startMapperServlet(Object testObject) {
		try {
			processAnnotations(Class.forName(testObject.getClass().getName()), testObject);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void stopMapperServlet() {
		try {
			if (server != null) {
				server.stop();
				server = null;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static Request mostRecentRequest() {
		return mapperServlet.getRequests().get(mapperServlet.getRequests().size() - 1);
	}

	public static List<Request> getRequests(String forPath) {
		List<Request> result = Lists.newArrayList();
		String path;
		Iterable<String> split;
		for (Request request : mapperServlet.getRequests()) {
			split = Splitter.on('?').omitEmptyStrings().split(request.url);
			path = split.iterator().next();
			if (!Strings.isNullOrEmpty(path) && path.equals(forPath)) {
				result.add(request);
			}
		}
		return result;
	}

	public static void addMapping(String uri, String resourceFile, String contentType, int status, boolean ignoreParams) {
		if (!uri.startsWith("/")) {
			uri = "/" + uri;
		}

		if (mapperServlet != null) {
			mapperServlet.bindReponse(uri, resourceFile, contentType, status, ignoreParams);
		}
	}

	private static void processAnnotations(final Class<? extends Object> testClass, Object testObject) throws Exception {
		Class<? extends Object> localTestClass = findAnnotatedSuperClass(testClass);

		if (localTestClass == null) {
			throw new IllegalStateException("Annotation not found");
		}

		TestServlet testServlet = localTestClass.getAnnotation(TestServlet.class);
		createServer(testServlet);

		RespondTo respondTo = localTestClass.getAnnotation(RespondTo.class);
		addResponsesFrom(respondTo, testServlet);

		startServer();
	}

	private static Class<? extends Object> findAnnotatedSuperClass(final Class<? extends Object> testClass) {
		Class<? extends Object> curClass = testClass;
		while (curClass.getAnnotation(TestServlet.class) == null) {
			curClass = curClass.getSuperclass();
			if (curClass == Object.class) {
				return null;
			}
		}

		Class<? extends Object> localTestClass = curClass;
		return localTestClass;
	}

	private static void createServer(TestServlet testServlet) {
		if (testServlet != null) {
			server = new TestingServer(testServlet.port());
		}
	}

	private static void addResponsesFrom(RespondTo respondTo, TestServlet testServlet) {
		mapperServlet = new AnnotationMapperServlet();
		mapperServlet.setRequiresAuthentication(testServlet.requiresAuthentication());
		mapperServlet.setAuthenticationAllowedUserName(testServlet.userName());
		mapperServlet.setAuthenticationAllowedPassword(testServlet.password());

		if (server != null && respondTo != null) {
			for (ResponseData response : respondTo.value()) {
				String contentType = response.contentType();
				String uri = response.url();
				String resourceFile = response.resourceFile();
				int status = response.status();
				boolean ignoreParams = response.ignoreParams();
				if ("".equals(contentType)) {
					contentType = testServlet.contentType();
				}

				addMapping(uri, resourceFile, contentType, status, ignoreParams);
			}
		}

		ServletHolder holder = new ServletHolder(mapperServlet);
		server.getHandler().addServlet(holder, "/");
	}

	private static void startServer() throws Exception {
		if (server != null) {
			server.start();
		}
	}
}
