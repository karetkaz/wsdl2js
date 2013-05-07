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
	public static final String WS_PROPERTY_FILE = "server.properties";

	public static final String WS_PUBLISH_WSDL = "publish.wsdl.address";
	public static final String WS_PUBLISH_HTML = "publish.html.address";
	public static final String WS_CONTENT_ROOT = "content.root.folder";
	public static final String WS_MODULES_PATH = "modules.wsdl.folder";

	public static final String LOGFILE = "log.txt";
	public static final SimpleDateFormat DATEFMT = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");

	public static void log2File(String message) {
		log2File(LOGFILE, null, message);
	}

	public static void log2File(WebServiceContext context, String message, Object... args) {
		log2File(LOGFILE, context, message, args);
	}

	public static void log2File(String path, WebServiceContext context, String message, Object... args) {
		BufferedWriter log = null;
		try {
			log = new BufferedWriter(new FileWriter("log.txt", true));
			log.write("[");
			log.write(DATEFMT.format(new Date()));
			log.write("]");
			if (context != null) {
				log.write("(@");
				log.write(((Headers) context.getMessageContext().get(MessageContext.HTTP_REQUEST_HEADERS)).getFirst("Host"));
				log.write(") ");
			}
			if (args != null && args.length > 0) {
				message = String.format(message, args);
			}
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

	public static Properties properties;

	static {
		try {
			properties = new Properties();
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
			log2File("All modules published");

			// publishing the http endpoint
			Endpoint.create(HTTPBinding.HTTP_BINDING, new HttpEndPoint()).publish(htmlPublish);
			log2File("Access the URL in browser " + htmlPublish + "index.html");

		} catch (Exception e) {
			log2File(e.getMessage());
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
