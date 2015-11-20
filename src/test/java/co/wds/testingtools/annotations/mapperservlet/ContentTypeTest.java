package co.wds.testingtools.annotations.mapperservlet;



import org.junit.Test;

import co.wds.testingtools.annotations.MapperServlet.RespondTo;
import co.wds.testingtools.annotations.MapperServlet.ResponseData;
import co.wds.testingtools.annotations.MapperServlet.TestServlet;


@TestServlet(port=54321, contentType="text/plain")
@RespondTo({
	@ResponseData(url="hamlet", resourceFile="hamlet.txt"),
	@ResponseData(url="test", resourceFile="test.html", contentType="text/html"),
	@ResponseData(url="data", resourceFile="data.xml", contentType="application/xml"),
	@ResponseData(url="fake", resourceFile="fake.json", contentType="application/json")
	})
public class ContentTypeTest extends BaseMapperServletTest {
	@Test
	public void shouldServeDefaultContentTypeWhenNoneSpecified() throws Exception {
		testServlet("http://localhost:54321/hamlet", 200, "text/plain", "To be, or not to be.");
	}
	
	@Test
	public void shouldServeOverriddenContentTypeHtml() throws Exception {
		testServlet("http://localhost:54321/test", 200, "text/html", 
				"<html>\r<head>\r<title>Test</title>\r</head>\r<body>\r<!-- test comment -->\r<h1>This is a test html</h1>\r</body>\r</html>");
	}
	
	@Test
	public void shouldServeOverriddenContentTypeXml() throws Exception {
		testServlet( "http://localhost:54321/data", 200, "application/xml",
				"<?xml version=\"1.0\"?>\r<notes>\r\t<note to=\"Ted\" from=\"Bill\">\r\t\t<heading>Reminder</heading>\r\t\t<body>Ted! Don't forget to wind your watch!</body>\r\t</note>\r</notes>");
	}
	
	@Test
	public void shouldServeOverriddenContentTypeJson() throws Exception {
		testServlet("http://localhost:54321/fake", 200,	"application/json", 
				"{\r\t\"fake\":\"true\"\r}");
	}
}
