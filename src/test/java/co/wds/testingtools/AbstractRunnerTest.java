package co.wds.testingtools;

import static co.wds.testingtools.annotations.MapperServlet.TestServlet.ANY_FREE_PORT;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.net.HttpURLConnection;
import java.net.URL;

import org.junit.Test;

import co.wds.testingtools.annotations.MapperServlet;
import co.wds.testingtools.annotations.MapperServlet.RespondTo;
import co.wds.testingtools.annotations.MapperServlet.ResponseData;
import co.wds.testingtools.annotations.MapperServlet.TestServlet;

@TestServlet(port=ANY_FREE_PORT)
@RespondTo({
    @ResponseData(url="test", resourceFile="test.html")
})
abstract public class AbstractRunnerTest {
    @Test
    public void shouldResponseToBaseData() throws Exception {
        testServlet("/test", 200);
    }
    
    @Test
    public void shouldNotRespondToSomethingNotDefinedInTheHeader() throws Exception {
        testServlet("/hamlet", 404); 
    }
    
    @Test
    @ResponseData(url="hamlet", resourceFile="hamlet.txt")
    public void shouldRespondToResponseDefinedInTheTestMethodAnnotation() throws Exception {
        testServlet("/hamlet", 200);
    }
    
    protected void testServlet(String path, int expectedHttpStatusCode) throws Exception {
        URL url = new URL("http://localhost:" + MapperServlet.getPort() + path);
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        connection.setRequestMethod("GET");
        assertThat(connection.getResponseCode(), is(expectedHttpStatusCode));
    }
}
