package co.wds.testingtools.annotations.mapperservlet;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.commons.codec.binary.Base64;
import org.junit.After;
import org.junit.Before;

import co.wds.testingtools.annotations.MapperServlet;

public class BaseMapperServletTest {
	
	MapperServlet unit = new MapperServlet(this);

	@Before
	public void setupTest() {	
		unit.start();
	}

	@After
	public void teardownTest() {
		unit.stop();
	}
    
    protected int getPort() {
        return unit.getServer().port;
    }

    protected String getBaseUrl() {
        return "http://localhost:" + getPort();
    }

	protected void testServlet(String theUrl, int expectedHttpStatusCode) throws Exception {
		URL url = new URL(theUrl);
		HttpURLConnection connection = (HttpURLConnection)url.openConnection();
		connection.setRequestMethod("GET");
		assertThat(connection.getResponseCode(), is(expectedHttpStatusCode));
	}
	
	protected void testServlet(String theUrl, int expectedHttpStatusCode, String expectedContentType, String expectedContentAsString) throws Exception {
		URL url = new URL(theUrl);
		HttpURLConnection connection = (HttpURLConnection)url.openConnection();
		connection.setRequestMethod("GET");
		
		String responseAsString = getResponseAsString(connection.getInputStream());
		
		assertThat(connection.getResponseCode(), is(expectedHttpStatusCode));
		assertThat(connection.getContentType(), containsString(expectedContentType));
		assertThat(responseAsString, is(expectedContentAsString));
	}
	
	protected void authenticateTestServlet(String theUrl, String username, String password, int expectedHttpStatusCode) throws Exception {
		URL url = new URL(theUrl);
		HttpURLConnection connection = (HttpURLConnection)url.openConnection();
		connection.setRequestMethod("GET");

		String userpass = username + ":" + password;
		String basicAuth = "Basic " + new String(new Base64().encode(userpass.getBytes()));
		connection.setRequestProperty ("Authorization", basicAuth);
		
		assertThat(connection.getResponseCode(), is(expectedHttpStatusCode));
	}

	private String getResponseAsString(InputStream is) throws IOException {
		BufferedReader rd = new BufferedReader(new InputStreamReader(is));
	    String line;
	    StringBuffer response = new StringBuffer(); 
	    boolean firstLine = true;
	    while((line = rd.readLine()) != null) {
			if (!firstLine) {
	    		response.append('\r');
	    	}
	    	response.append(line);
	    	firstLine = false;
	    }
	    rd.close();
	    
		String responseAsString = response.toString();
		return responseAsString;
	}

}