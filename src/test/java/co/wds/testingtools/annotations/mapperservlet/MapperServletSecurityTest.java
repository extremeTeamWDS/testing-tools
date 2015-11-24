package co.wds.testingtools.annotations.mapperservlet;

import static co.wds.testingtools.annotations.MapperServlet.TestServlet.ANY_FREE_PORT;

import org.junit.Test;

import co.wds.testingtools.annotations.MapperServlet.RespondTo;
import co.wds.testingtools.annotations.MapperServlet.ResponseData;
import co.wds.testingtools.annotations.MapperServlet.TestServlet;


@TestServlet(port=ANY_FREE_PORT, requiresAuthentication=true, userName="user", password="password")
@RespondTo({
	@ResponseData(url="testing/1234", resourceFile="test1234.json")
})
public class MapperServletSecurityTest extends BaseMapperServletTest {
	@Test
	public void shouldNotAllowAccess_WithoutAuthentication() throws Exception {
		testServlet(getBaseUrl() + "/testing/1234", 401);
	}
	
	@Test
	public void shouldAllowAccess_With_CorrectUsernameAndPassword() throws Exception {
		authenticateTestServlet(getBaseUrl() + "/testing/1234", "user", "password", 200);
	}
	
	@Test
	public void shouldNotAllowAccess_With_CorrectUsername_And_IncorrectPassword() throws Exception {
		authenticateTestServlet(getBaseUrl() + "/testing/1234", "user", "qwerty", 401);
	}
	
	@Test
	public void shouldNotAllowAccess_With_InCorrectUsername_And_CorrectPassword() throws Exception {
		authenticateTestServlet(getBaseUrl() + "/testing/1234", "asdfg", "password", 401);
	}
	
	@Test
	public void shouldNotAllowAccess_With_InCorrectUsername_And_InCorrectPassword() throws Exception {
		authenticateTestServlet(getBaseUrl() + "/testing/1234", "asdfg", "qwerty", 401);
	}
	
	@Test
	public void shouldNotAllowAccess_With_NullUsername_And_CorrectPassword() throws Exception {
		authenticateTestServlet(getBaseUrl() + "/testing/1234", null, "password", 401);
	}
	
	@Test
	public void shouldNotAllowAccess_With_CorrectUsername_And_NullPassword() throws Exception {
		authenticateTestServlet(getBaseUrl() + "/testing/1234", "user", null, 401);
	}
}
