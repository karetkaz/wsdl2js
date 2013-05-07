package server;

import javax.activation.DataSource;
import javax.activation.FileDataSource;
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
		if (pi == null || pi.equals("/")) {
			pi = "/index.html";
		}
		if (!pi.startsWith("/")) {
			pi = "/" + pi;
		}

		final File file = new File(WSApplication.properties.getProperty(WSApplication.WS_CONTENT_HTML) + pi);

		if (file.getPath().startsWith(WSApplication.properties.getProperty(WSApplication.WS_CONTENT_CACHED))) {
			mc.put(MessageContext.HTTP_RESPONSE_HEADERS, Collections.singletonMap("Cache-Control", Collections.singletonList("max-age=31536000,public")));
		}

		return new DataSource() {

			public InputStream getInputStream() throws FileNotFoundException {
				return new FileInputStream(file);
			}

			public OutputStream getOutputStream() throws FileNotFoundException {
				return new FileOutputStream(file);
			}

			public String getContentType() {

				String fileName = file.getName();

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
				return file.getName();
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
