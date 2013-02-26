package server;

import javax.activation.DataSource;
import javax.annotation.Resource;
import javax.xml.ws.*;
import javax.xml.ws.handler.MessageContext;
import java.io.*;
import java.util.Collections;

@WebServiceProvider
@ServiceMode(value = Service.Mode.MESSAGE)
public class HttpEndPoint implements Provider<DataSource> {

	@Resource
	protected WebServiceContext wsContext;

	public DataSource invoke(DataSource ds) {
		MessageContext mc = wsContext.getMessageContext();
		String method = (String) mc.get(MessageContext.HTTP_REQUEST_METHOD);
		if (method.equals("GET")) {
			return get(mc);
		}
		if (method.equals("POST")) {
			return post(mc);
		}
		throw new WebServiceException("Unsupported HTTP method = " + method);
	}

	/**
	 * Handles HTTP GET.
	 */
	private DataSource get(final MessageContext mc) {
		String pi = (String) mc.get(MessageContext.PATH_INFO);
		if (pi == null)
			pi = "/index.html";
		else if (pi.equals("/"))
			pi = "index.html";

		final String path = pi;

		File f = new File(WSApplication.properties.getProperty("content.html") + pi);

		if (f.isDirectory()) {
			if (pi.endsWith("/")) {
				pi += "index.html";
			} else {
				pi += "/index.html";
			}
		}

		if (pi.contains("js/libs")) {
			mc.put(MessageContext.HTTP_RESPONSE_HEADERS, Collections.singletonMap("Cache-Control", Collections.singletonList("max-age=31536000,public")));
		}

		//return new FileDataSource(WSApplication.properties.getProperty(WSApplication.WS_CONTENT_ROOT) + path);
		return new DataSource() {
			String fileName = WSApplication.properties.getProperty(WSApplication.WS_CONTENT_ROOT) + path;

			public InputStream getInputStream() {
				InputStream is = null;
				try {
					is = new FileInputStream(fileName);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
				return is;
			}

			public OutputStream getOutputStream() {
				return null;
			}

			public String getContentType() {
				if (fileName.endsWith(".html"))
					return "text/html";
				if (fileName.endsWith(".htm"))
					return "text/html";
				if (fileName.endsWith(".css"))
					return "text/css";
				if (fileName.endsWith(".js"))
					return "application/javascript";
				if (fileName.endsWith(".png"))
					return "image/png";
				if (fileName.endsWith(".gif"))
					return "image/gif";
				if (fileName.endsWith(".jpg"))
					return "image/jpg";
				return "text/plain";
			}

			public String getName() {
				return "";
			}
		};
	}

	/**
	 * Handles HTTP POST.
	 */
	private DataSource post(final MessageContext mc) {
		return null;
	}
}
