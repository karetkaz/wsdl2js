package server;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Wsdl2Js {

	private static final Map<String, String> typeMap2Java = new HashMap<String, String>();
	static {
		typeMap2Java.put("xs:string", "String");
		typeMap2Java.put("xs:boolean", "boolean");
		typeMap2Java.put("xs:int", "int");
	}

	/*private static final Map<String, String> typeMap2Js = new HashMap<String, String>();
	static {
		typeMap2Js.put("xs:string", "String");
		typeMap2Js.put("xs:boolean", "Boolean");
		typeMap2Js.put("xs:int", "Number");
	}*/

	private static class WsElement {
		public final String name;
		public final String type;
		public final boolean isArray;
		public final boolean isObject;

		private WsElement(String name, String type, boolean array) {
			this.isObject = type.startsWith("tns:");
			this.name = name;
			this.type = type;
			this.isArray = array;
		}

		public String getType(Map<String, String> typeMap) {

			// basic type
			if (type.startsWith("xs:"))
				return typeMap.get(type);

			// declared type
			if (isObject)
				return type.substring(4);

			return type;
		}
	}

	private static class WsComplexType {
		public final String name;
		public final WsElement[] elements;

		public boolean printed = false;

		public WsComplexType(String name, WsElement[] elements) {
			this.name = name;
			this.elements = elements;
		}

		/*public String toString(boolean asArgs) {
			StringBuilder result = new StringBuilder();
			for (WsElement element : elements) {
				if (result.length() != 0) {
					result.append(", ");
				}
				result.append(element.getType(typeMap2Java));
				if (asArgs) {
					result.append(' ').append(element.name);
				}
			}
			return result.toString();
		}*/
	}

	private static class WsOperation {

		public final String name;
		public final WsComplexType input;
		public final WsComplexType output;
		public final WsComplexType fault;

		public WsOperation(String name, WsComplexType input, WsComplexType output, WsComplexType fault) {
			this.name = name;
			this.input = input;
			this.output = output;
			this.fault = fault;
		}
	}

	public Wsdl2Js(String publishURL, String moduleName) {
		this.url = publishURL;
		this.module = moduleName;
	}

	private String url = "";
	private String xmlns = "";
	private String module = "";
	final private List<WsOperation> operations = new ArrayList<WsOperation>();
	final private Map<String, WsComplexType> complexTypes = new HashMap<String, WsComplexType>();

	private Document readDocument(final InputStream input) throws ParserConfigurationException, IOException, SAXException {

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

		DocumentBuilder db = dbf.newDocumentBuilder();

		return db.parse(new InputStream() {
			// we don't want newlines in the document
			@Override
			public int read() throws IOException {
				int result = -1;
				for ( ; ; ) {
					result = input.read();
					if (result == '\n')
						continue;
					if (result == '\r')
						continue;
					break;
				}
				return result;
			}
		});
	}

	private String getAttribute(Element element, String attr) {
		if (element != null) {
			return element.getAttribute(attr);
		}
		return null;
	}

	private Element getFirstElementByTagName(Element root, String tag) {
		if (root != null) {
			NodeList elements = root.getElementsByTagName(tag);
			if (elements != null && elements.getLength() > 0) {
				return (Element)elements.item(0);
			}
		}
		return null;
	}



	private void printJavaTypes(PrintStream out, WsElement element) {
		WsComplexType type = complexTypes.get(element.getType(typeMap2Java));
		if (type != null && type.printed == false) {

			// print used classes first
			for (WsElement structure : type.elements) {
				printJavaTypes(out, structure);
			}

			// print the class
			out.println(String.format("class %s {", type.name));
			for (WsElement field : type.elements) {
				out.println(String.format("\t%s%s %s;", field.getType(typeMap2Java), field.isArray ? "[]" : "", field.name));
			}
			out.println("}");

			type.printed = true;
		}
	}

	private void printJavaArgs(PrintStream out, WsComplexType type, boolean typeOnly) {
		if (type != null && type.elements != null) {
			boolean first = true;
			for (WsElement el : type.elements) {
				if (!first) {
					out.print(", ");
				}
				else {
					first = false;
				}
				out.print(el.getType(typeMap2Java));
				if (el.isArray)
					out.print("[]");

				if (!typeOnly) {
					out.print(' ');
					out.print(el.name);
				}
				//printJavaTypes(out, el);
			}
		}
	}

	public void printJavaInterface(PrintStream out) {
		for (WsOperation operation : operations) {
			if (operation.output != null && operation.output.elements != null) {
				for (WsElement el : operation.output.elements) {
					printJavaTypes(out, el);
				}
			}
			if (operation.input != null && operation.input.elements != null) {
				for (WsElement el : operation.input.elements) {
					printJavaTypes(out, el);
				}
			}
			if (operation.fault != null && operation.fault.elements != null) {
				for (WsElement el : operation.fault.elements) {
					printJavaTypes(out, el);
				}
			}
		}

		for (WsComplexType type : complexTypes.values()) {
			type.printed = false;
		}

		out.println(String.format("class %s {", module));
		out.print("\tprivate String wsdlUrl = \"");
		out.print(this.url);
		out.println("\";");

		for (WsOperation operation : operations) {
			out.print("\t");
			printJavaArgs(out, operation.output, true);
			out.print(' ');
			out.print(operation.name);
			out.print('(');
			printJavaArgs(out, operation.input, false);
			out.println(");");
		}
		out.println("}");
	}

	public void printJsInterface(PrintStream out) throws IOException {
		BufferedWriter bw = new BufferedWriter(new PrintWriter(out));
		bw.append(module).append(" = {\n");
		bw.append("	module: {\n");
		bw.append("		url: '").append(url).append("',\n");
		bw.append("		xmlns: '").append(xmlns).append("',\n");
		bw.append("		poststr: '<?xml version=\"1.0\" encoding=\"utf-8\"?>'\n");
		bw.append("			+'<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">'\n");
		bw.append("			+	'<soap:Body>{SOAP_BODY}</soap:Body>'\n");
		bw.append("			+'</soap:Envelope>',\n");
		bw.append("		xpath: {\n");
		for (WsOperation operation : operations) {
			bw.append("			").append(operation.name).append(": '/S:Envelope/S:Body/ns2:").append(operation.name).append("Response/return',\n");
		}
		bw.append("		}\n");
		bw.append("	}\n");
		bw.append("};\n");

		for (WsOperation operation : operations) {
			bw.append(module).append(".").append(operation.name).append(" = function(");
			boolean first = true;
			if (operation.input != null && operation.input.elements != null) {
				for (WsElement arg : operation.input.elements) {
					if (!first) {
						bw.append(", ");
					}
					else {
						first = false;
					}
					bw.append(arg.name);
				}
			}

			if (!first) {
				bw.append(", ");
			}
			bw.append("callBack) {\n");
			bw.append("	return WsdlUtils.WsdlInvoke(").append(module).append(".module, '").append(operation.name).append("', {");
			if (operation.input != null && operation.input.elements != null) {
				first = true;
				for (WsElement arg : operation.input.elements) {
					if (!first) {
						bw.append(", ");
					}
					else {
						first = false;
					}
					bw.append(arg.name).append(":").append(arg.name);
				}
			}
			bw.append("}, callBack);\n");
			bw.append("};\n");
		}
		bw.flush();
	}

	public Wsdl2Js parseWsdl() throws ParserConfigurationException, SAXException, IOException {

		// TODO temporarily include ?xsd=1
		Document xsdDocument = readDocument(new URL(url + "?xsd=1").openStream());
		Document document = readDocument(new URL(url + "?wsdl").openStream());

		Element documentElement = xsdDocument.getDocumentElement();

		Map<String, WsElement> elementsByType = new HashMap<String, WsElement>();
		Map<String, List<WsElement>> _complexTypes = new HashMap<String, List<WsElement>>();

		NodeList nodeList = documentElement.getElementsByTagName("xs:element");
		if (nodeList != null) {
			for (int i = 0; i < nodeList.getLength(); i++) {
				Element element = (Element) nodeList.item(i);
				WsElement wsElement = new WsElement(
						element.getAttribute("name"),
						element.getAttribute("type"),
						//Boolean.valueOf(element.getAttribute("nillable")),
						"unbounded".equalsIgnoreCase(element.getAttribute("maxOccurs"))
				);
				if (element.getParentNode().isEqualNode(documentElement)) {
					elementsByType.put(wsElement.type, wsElement);
				}
				else {
					String name = ((Element)(element.getParentNode().getParentNode())).getAttribute("name");
					if (!_complexTypes.containsKey(name)) {
						_complexTypes.put(name, new ArrayList<WsElement>());
					}
					_complexTypes.get(name).add(wsElement);
				}
			}
		}

		// add types which are empty.
		nodeList = documentElement.getElementsByTagName("xs:complexType");
		if (nodeList != null) {
			for (int i = 0; i < nodeList.getLength(); i++) {
				Element element = (Element) nodeList.item(i);
				String name = element.getAttribute("name");
				if (!_complexTypes.containsKey(name)) {
					_complexTypes.put(name, new ArrayList<WsElement>());
				}
			}
		}

		for (String key : _complexTypes.keySet()) {
			this.complexTypes.put(key, new WsComplexType(key, _complexTypes.get(key).toArray(new WsElement[0])));
		}
		this.xmlns = xsdDocument.getDocumentElement().getAttribute("xmlns:tns");

		// parse wsdl
		//Map<String, WsElement> messages = new HashMap<String, WsElement>();
		//nodeList = documentElement.getElementsByTagName("message");

		documentElement = (Element) document.getElementsByTagName("portType").item(0);
		nodeList = documentElement.getElementsByTagName("operation");
		if (nodeList != null) {
			for (int i = 0; i < nodeList.getLength(); i++) {
				Element element = (Element) nodeList.item(i);

				WsElement input = elementsByType.get(getAttribute(getFirstElementByTagName(element, "input"), "message"));
				WsElement output = elementsByType.get(getAttribute(getFirstElementByTagName(element, "output"), "message"));
				WsElement fault = elementsByType.get(getAttribute(getFirstElementByTagName(element, "fault"), "message"));

				WsComplexType inputType = input == null ? null : complexTypes.get(input.name);
				WsComplexType outputType = output == null ? null : complexTypes.get(output.name);
				WsComplexType faultType = fault == null ? null : complexTypes.get(fault.name);

				WsOperation operation = new WsOperation(element.getAttribute("name"), inputType, outputType, faultType);

				operations.add(operation);
			}
		}

		this.xmlns = xsdDocument.getDocumentElement().getAttribute("xmlns:tns");
		return this;
	}

	//*
	public static void main(String[] args) throws Exception {
		String wsdlURL = "http://127.0.0.1:8089/wsdl/WSTest";
		Wsdl2Js wsdl2js = new Wsdl2Js(wsdlURL, "WSTest");
		wsdl2js.parseWsdl();
		//wsdl2js.printJsInterface(System.out);
		wsdl2js.printJavaInterface(System.out);
	}
	// */
}
