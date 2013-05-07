package server.modules;

import com.sun.net.httpserver.Headers;
import server.WSApplication;
import server.modules.types.WSTestResult;
import server.modules.types.WSTestResults;

import javax.annotation.Resource;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;
import java.io.File;
import java.io.FileInputStream;

import static server.WSApplication.Utils.getMethodName;

@WebService(targetNamespace = "http://modules.ws.server.com/")
public class WSTest {

	@Resource
	private WebServiceContext wsContext;

	//Map<String, WSTestResult> sessions = new HashMap<String, WSTestResult>();

	private final String sessionKey = "sessionid=";
	private final String Cookie = "Cookie";

	private String getSession() {
		MessageContext mc = wsContext.getMessageContext();
		Headers requestHeaders = (Headers) mc.get(MessageContext.HTTP_REQUEST_HEADERS);
		for (String cookie : requestHeaders.get(Cookie)) {
			if (cookie.startsWith(sessionKey)) {
				return sessionKey.substring(sessionKey.length());
			}
		}
		return null;
	}

	/*private void setSession(String sessionId) {
		MessageContext mc = wsContext.getMessageContext();
		mc.put(MessageContext.HTTP_RESPONSE_HEADERS, Collections.singletonMap("Set-Cookie", Collections.singletonList(sessionKey + sessionId + "; expires=200")));
		//Headers responseHeaders = (Headers)mc.get(MessageContext.HTTP_RESPONSE_HEADERS);
		//responseHeaders.put("set-cookie", Arrays.asList(new String[]{sessionKey + sessionId + "; expires=20000"}));
	}*/

	@WebMethod
	public String login(
			@WebParam(name = "username") String username,
			@WebParam(name = "password") String password) throws Exception {
		WSApplication.log(wsContext, "%s('%s', '%s')", getMethodName(0), username, password);
		if (password.equals("invalid")) {
			new FileInputStream(new File("c:/System Volume Information/alma.txt"));
			//int i = new int[]{1,2}[5];
			throw new Exception("you can not login with this password");
		}

		String sessionId = username + password;
		//sessions.put(sessionId, new User(...));
		return sessionId;
	}

	@WebMethod
	public String session() {
		WSApplication.log(wsContext, "%s()", getMethodName(0));
		return getSession();
	}

	@WebMethod
	public String getServerTime() {
		WSApplication.log(wsContext, "%s()", getMethodName(0));
		return ("" + System.currentTimeMillis());
	}

	@WebMethod
	public String strcat(
			@WebParam(name = "a") String a,
			@WebParam(name = "b") String b) {
		WSApplication.log(wsContext, "%s('%s', '%s')", getMethodName(0), a, b);
		return a + b;
	}


	@WebMethod
	public WSTestResult[] getArray(
			@WebParam(name = "a") int a,
			@WebParam(name = "b") int b) {
		WSApplication.log(wsContext, "%s(%d, %d)", getMethodName(0), a, b);

		WSTestResult result[] = new WSTestResult[b - a];
		for (int i = 0; i < result.length; i += 1) {
			result[i] = new WSTestResult();
			result[i].id = i + a;
			result[i].title = "title " + i;
			result[i].subtitle = "subtitle " + (i + a);
			result[i].live = (i - b) % 2 != 0;
		}
		return result;
	}

	@WebMethod
	public WSTestResults getArray2(
			@WebParam(name = "a") int a,
			@WebParam(name = "b") int b) {
		WSApplication.log(wsContext, "%s(%d, %d)", getMethodName(0), a, b);
		WSTestResults result = new WSTestResults();
		result.offset = a;
		result.limit = 2000;
		result.items = getArray(a, b);
		return result;
	}
}
