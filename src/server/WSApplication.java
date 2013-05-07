package server;

import org.xml.sax.SAXException;
import server.modules.WSMath;
import server.modules.WSTest;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.ws.Endpoint;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.http.HTTPBinding;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import com.sun.net.httpserver.Headers;

public class WSApplication {

	public static class Utils {
		public static String getMethodName(final int depth) {
			final StackTraceElement[] ste = Thread.currentThread().getStackTrace();
			try {
				StackTraceElement method = ste[depth + 2];
				return String.format("%s.%s", method.getClassName(), method.getMethodName());
			}
			catch (ArrayIndexOutOfBoundsException e) {
				return "";
			}
		}
	}

	public static final String WS_PROPERTY_FILE = "server.properties";

	public static final String WS_PUBLISH_WSDL = "publish.wsdl.address";
	public static final String WS_MODULES_PATH = "modules.wsdl.folder";

	public static final String WS_PUBLISH_HTML = "publish.html.address";
	public static final String WS_CONTENT_HTML = "content.html.folder";
	public static final String WS_CONTENT_CACHED = "content.html.cached";

	public static final String LOGFILE = "log.txt";
	public static final SimpleDateFormat DATEFMT = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");

	public static void log(String message, Object... args) {
		log(null, message, args);
	}

	public static void log(WebServiceContext context, String message, Object... args) {
		log(LOGFILE, context, message, args);
	}

	public static void log(String path, WebServiceContext context, String message, Object... args) {

		if (args != null && args.length > 0) {
			message = String.format(message, args);
		}
		String date = DATEFMT.format(new Date());
		if (context != null) {
			String host = ((Headers) context.getMessageContext().get(MessageContext.HTTP_REQUEST_HEADERS)).getFirst("Host");
			message = String.format("[%s @%s] %s", date, host, message);
		}
		else {
			message = String.format("[%s] %s", date, message);
		}

		BufferedWriter log = null;
		try {
			log = new BufferedWriter(new FileWriter("log.txt", true));
			log.write(message);
			log.write("\n");
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (log != null)
					log.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		System.out.println(message);
	}

	public static final Properties properties = new Properties();

	static {
		try {
			properties.load(new FileInputStream(WS_PROPERTY_FILE));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] arguments) throws Exception {
		String htmlPublish = properties.getProperty(WS_PUBLISH_HTML);
		String wsdlPublish = properties.getProperty(WS_PUBLISH_WSDL);
		String modulesPath = properties.getProperty(WS_MODULES_PATH);

		try {
			// publishing webservices
			publishWsdlJs(wsdlPublish, modulesPath, new WSTest());
			publishWsdlJs(wsdlPublish, modulesPath, new WSMath());
			log("All modules published");

			// publishing the http endpoint
			Endpoint.create(HTTPBinding.HTTP_BINDING, new HttpEndPoint()).publish(htmlPublish);
			log("Access the URL in browser %sindex.html", htmlPublish);

		} catch (Exception e) {
			log(e.getMessage());
			throw e;
		}// */
	}

	private static void publishWsdlJs(String wsdlURL, String jsPath, Object module) throws IOException, ParserConfigurationException, SAXException {
		String moduleName = module.getClass().getSimpleName();
		wsdlURL += moduleName;
		jsPath += moduleName + ".js";
		Endpoint.publish(wsdlURL, module);
		Wsdl2Js wsdl2js = new Wsdl2Js(wsdlURL, moduleName);
		wsdl2js.parseWsdl().printJsInterface(new PrintStream(jsPath));
	}
}
