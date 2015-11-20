package co.wds.testingtools.annotations.mapperservlet;

import org.junit.Test;

import co.wds.testingtools.annotations.MapperServlet.TestServlet;

@TestServlet(port=12345)
public class MapperServletAdditionsTest extends BaseMapperServletTest {
	@Test
	public void shouldBeAbleToAddANewMapping() throws Exception {
		unit.addMapping("hamlet", "hamlet.txt", "text/plain", 200, false,"any");
		unit.addMapping("test", "test.html", "text/html", 200, false,"any");
		unit.addMapping("data", "data.xml", "application/xml", 200, false,"any");
		unit.addMapping("fake", "fake.json", "application/json", 200, false,"any");
		
		testServlet("http://localhost:12345/hamlet", 200, "text/plain", "To be, or not to be.");
		testServlet("http://localhost:12345/test", 200, "text/html", "<html>\r<head>\r<title>Test</title>\r</head>\r<body>\r<!-- test comment -->\r<h1>This is a test html</h1>\r</body>\r</html>");
		testServlet( "http://localhost:12345/data", 200, "application/xml", "<?xml version=\"1.0\"?>\r<notes>\r\t<note to=\"Ted\" from=\"Bill\">\r\t\t<heading>Reminder</heading>\r\t\t<body>Ted! Don't forget to wind your watch!</body>\r\t</note>\r</notes>");
		testServlet("http://localhost:12345/fake", 200,	"application/json", "{\r\t\"fake\":\"true\"\r}");
	}
}
