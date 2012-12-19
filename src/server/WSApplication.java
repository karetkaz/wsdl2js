package server;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.ws.Endpoint;
import javax.xml.ws.http.HTTPBinding;

import org.xml.sax.SAXException;

import server.modules.WSMath;
import server.modules.WSTest1;
import server.utils.wsdl2js.Wsdl2JS;

public class WSApplication {
	public static final String WS_PROPERTY_FILE = "server.properties";

	public static final String WS_PUBLISH_WSDL = "publish.wsdl.address";
	public static final String WS_PUBLISH_HTML = "publish.html.address";

	public static final String WS_CONTENT_ROOT = "content.root.folder";
	public static final String WS_MODULES_PATH = "modules.wsdl.folder";

	public static final String LOGFILE = "log.txt";

	public static void log2File(String message) {
		log2File(LOGFILE, message);
		System.out.println(message);
	}

	public static void log2File(String path, String message) {
		BufferedWriter log = null;
		try {
			log = new BufferedWriter(new FileWriter("log.txt", true));
			log.write("[");
			log.write(new Date().toString());
			log.write("]");
			log.write(message);
			log.write("\n");
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (log != null)
					log.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
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
			publishWsdlJs(wsdlPublish, modulesPath, new WSTest1());
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

		URL u = new URL(wsdlURL + "?xsd=1");
		InputStream xsd = u.openStream();
		FileOutputStream js = new FileOutputStream(jsPath);
		
		Wsdl2JS wsdl2js = new Wsdl2JS(wsdlURL, moduleName);
		wsdl2js.parseXml(xsd);
		wsdl2js.printJs(js);
	}
}
