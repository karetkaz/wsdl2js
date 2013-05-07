package server.modules;

import server.WSApplication;

import javax.annotation.Resource;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.xml.ws.WebServiceContext;

import static server.WSApplication.Utils.getMethodName;

@WebService(targetNamespace = "http://modules.ws.server.com/")
public class WSMath {
	@Resource
	private WebServiceContext wsContext;


	@WebMethod
	public double PI() {
		WSApplication.log(wsContext, getMethodName(0));
		return Math.PI;
	}

	@WebMethod
	public double sin(
			@WebParam(name = "x") double x) {
		WSApplication.log(wsContext, "%s(%f)", getMethodName(0), x);
		return Math.sin(x);
	}

	@WebMethod
	public double pow(
			@WebParam(name = "x") double x,
			@WebParam(name = "y") double y) {
		WSApplication.log(wsContext, "%s(%f, %f)", getMethodName(0), x, y);
		return Math.pow(x, y);
	}
	// */
}
