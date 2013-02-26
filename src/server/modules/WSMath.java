package server.modules;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

@WebService(targetNamespace = "http://modules.ws.server.com/")
public class WSMath {

	@WebMethod
	public double PI() {
		return Math.PI;
	}

	@WebMethod
	public double sin(
			@WebParam(name = "x") double x) {
		return Math.sin(x);
	}

	@WebMethod
	public double pow(
			@WebParam(name = "x") double x,
			@WebParam(name = "y") double y) {
		return Math.pow(x, y);
	}
	// */
}
