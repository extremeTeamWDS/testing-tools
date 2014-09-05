package co.wds.testingtools.annotations.mapperservlet;

import java.util.Map;

public class Request {
	
	public enum RequestType { PUT, GET, POST };
	
	public String url;	
	public String body;
	public RequestType type;
	public Map<String, String> headers;
}
