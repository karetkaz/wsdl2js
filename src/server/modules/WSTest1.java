package server.modules;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

@WebService(targetNamespace="http://modules.ws.server.com/")
public class WSTest1 {

	@WebMethod
	public String getServerTime() {
		return ("" + System.currentTimeMillis());
	}

	@WebMethod
	public String strcat(
			@WebParam(name = "a") String a,
			@WebParam(name = "b") String b) {
		return a + b;
	}

	@WebMethod
	public WSTestResult[] getArray(
			@WebParam(name = "a") int a,
			@WebParam(name = "b") int b) {
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
		WSTestResults result = new WSTestResults();
		result.offset = a;
		result.limit = 2000;
		result.items = getArray(a, b);
		return result;
	}
}
