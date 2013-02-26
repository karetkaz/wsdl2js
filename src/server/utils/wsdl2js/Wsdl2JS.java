package server.utils.wsdl2js;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import server.utils.wsdl2js.RawNode.NodeType;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Wsdl2JS {

	public Wsdl2JS(String publishURL, String moduleName) {
		this.url = publishURL;
		this.module = moduleName;
	}

	public void parseXml(final InputStream input) throws ParserConfigurationException, SAXException, IOException {

		//get the factory
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

		//Using factory get an instance of document builder
		DocumentBuilder db = dbf.newDocumentBuilder();

		//parse using builder to get DOM representation of the XML file
		Document dom = db.parse(new InputStream() {
			// we don't want newlines in the document
			@Override
			public int read() throws IOException {
				int result = -1;
				for (; ; ) {
					result = input.read();
					if (result == '\n')
						continue;
					if (result == '\r')
						continue;
					//if (result == '\t')
					//continue;
					break;
				}
				return result;
			}
		});

		//get the root element
		Element docEle = dom.getDocumentElement();

		//get a nodelist of elements
		NodeList nl = docEle.getElementsByTagName("xs:element");
		Map<String, RawNode> elements = new HashMap<String, RawNode>();

		if (nl != null && nl.getLength() > 0) {
			for (int i = 0; i < nl.getLength(); i++) {
				Element el = (Element) nl.item(i);
				elements.put(el.getAttribute("name"), new RawNode(el, NodeType.type));
			}
		}

		//get a nodelist of elements
		nl = docEle.getElementsByTagName("xs:complexType");
		Map<String, RawNode> complexTypes = new HashMap<String, RawNode>();
		Map<String, RawNode> methods = new HashMap<String, RawNode>();
		Map<String, RawNode> methodResponses = new HashMap<String, RawNode>();
		Map<String, RawNode> exceptions = new HashMap<String, RawNode>();

		if (nl != null && nl.getLength() > 0) {
			for (int i = 0; i < nl.getLength(); i++) {
				RawNode rn = new RawNode(nl.item(i), NodeType.type);
				if (elements.containsKey(rn.getName())) {
					if (rn.getName().endsWith("Response")) {
						rn.setNodeType(NodeType.methodresponse);
						methodResponses.put(rn.getName(), rn);
					} else {
						rn.setNodeType(NodeType.method);
						methods.put(rn.getName(), rn);
					}
				} else {
					rn.setNodeType(NodeType.type);
					complexTypes.put(rn.getName(), rn);
				}
			}
		}

		String[] ks = methods.keySet().toArray(new String[methods.keySet().size()]);
		for (int i = 0; i < ks.length; i++) {
			if (!methodResponses.containsKey(ks[i] + "Response")) {
				RawNode rn = methods.get(ks[i]);
				rn.setNodeType(NodeType.methodresponse);
				exceptions.put(ks[i], rn);
				methods.remove(ks[i]);
			}
		}

		Map<String, AbstractDatatype> datatypes = new HashMap<String, AbstractDatatype>();
		ArrayList<Method> methodList = new ArrayList<Method>();

		// methods
		for (RawNode rn : methods.values()) {
			Element el = (Element) rn.getNode();
			nl = rn.getNode().getChildNodes();
			Method m = new Method(el.getAttribute("name"));
			methodList.add(m);
			if (nl != null && nl.getLength() == 1) {
				nl = nl.item(0).getChildNodes();
				if (nl != null && nl.getLength() > 0) {

					for (int i = 0; i < nl.getLength(); i++) {
						Element el2 = (Element) nl.item(i);
						AbstractDatatype dt = null;
						if (el2.getAttribute("type").startsWith("tns:")) {
							String typename = el2.getAttribute("type").substring(el2.getAttribute("type").indexOf(":") + 1);

							if (datatypes.containsKey(typename)) {
								dt = datatypes.get(typename);
							} else {
								dt = new ComplexDatatype(typename);
								datatypes.put(typename, dt);
							}
						} else if (el2.getAttribute("type").startsWith("xs:")) {
							String typename = el2.getAttribute("type").substring(el2.getAttribute("type").indexOf(":") + 1);
							if (datatypes.containsKey(typename)) {
								dt = datatypes.get(typename);
							} else {
								dt = new SimpleDatatype(typename);
								datatypes.put(typename, dt);
							}
						}
						Parameter p = new Parameter(el2.getAttribute("name"), dt);
						m.addParameter(p);
					}
				}
			}
		}

		// complex types
		for (RawNode rn : complexTypes.values()) {
			Element el = (Element) rn.getNode();
			ComplexDatatype ct;
			if (datatypes.containsKey(el.getAttribute("name"))) {
				ct = (ComplexDatatype) datatypes.get(el.getAttribute("name"));
			} else {
				ct = new ComplexDatatype(el.getAttribute("name"));
				datatypes.put(el.getAttribute("name"), ct);
			}
			nl = rn.getNode().getChildNodes();
			if (nl != null && nl.getLength() == 1) {
				nl = nl.item(0).getChildNodes();
				if (nl != null && nl.getLength() > 0) {
					for (int i = 0; i < nl.getLength(); i++) {
						Element el2 = (Element) nl.item(i);
						AbstractDatatype dt = null;
						if (el2.getAttribute("type").startsWith("tns:")) {
							String typename = el2.getAttribute("type").substring(el2.getAttribute("type").indexOf(":") + 1);

							if (datatypes.containsKey(typename)) {
								dt = datatypes.get(typename);
							} else {
								dt = new ComplexDatatype(typename);
								datatypes.put(typename, dt);
							}
						} else if (el2.getAttribute("type").startsWith("xs:")) {
							String typename = el2.getAttribute("type").substring(el2.getAttribute("type").indexOf(":") + 1);
							if (datatypes.containsKey(typename)) {
								dt = datatypes.get(typename);
							} else {
								dt = new SimpleDatatype(typename);
								datatypes.put(typename, dt);
							}

						}
						Parameter p = new Parameter(el2.getAttribute("name"), dt);
						ct.addElement(p);

					}
				}
			}
		}

		for (RawNode rn : methodResponses.values()) {
			Element el = (Element) rn.getNode();
			nl = rn.getNode().getChildNodes();
			if (nl != null && nl.getLength() == 1) {
				nl = nl.item(0).getChildNodes();
				if (nl != null && nl.getLength() > 0) {
					for (int i = 0; i < nl.getLength(); i++) {
						Element el2 = (Element) nl.item(i);
						String typename = el2.getAttribute("type").substring(el2.getAttribute("type").indexOf(":") + 1);
						AbstractDatatype dt = datatypes.get(typename);
						for (Method m : methodList) {
							if ((m.getName() + "Response").equals(el.getAttribute("name"))) {
								m.setReturnType(dt);
								break;
							}
						}
					}
				}
			}
		}

		xmlns = dom.getDocumentElement().getAttribute("xmlns:tns");
		this.methods = methodList;
	}

	public void printJs(OutputStream output) throws IOException {
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(output));
		bw.append(module).append(" = {\n");
		bw.append("	module: {\n");
		bw.append("		url: '").append(url).append("',\n");
		bw.append("		xmlns: '").append(xmlns).append("',\n");
		bw.append("		poststr: '<?xml version=\"1.0\" encoding=\"utf-8\"?>'\n");
		bw.append("			+'<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">'\n");
		bw.append("			+	'<soap:Body>{SOAP_BODY}</soap:Body>'\n");
		bw.append("			+'</soap:Envelope>',\n");
		bw.append("		xpath: {\n");
		for (Method m : methods) {
			bw.append("			").append(m.getName()).append(": '/S:Envelope/S:Body/ns2:").append(m.getName()).append("Response/return',\n");
		}
		bw.append("		}\n");
		bw.append("	}\n");
		bw.append("};\n");

		for (Method m : methods) {
			bw.append(module).append(".").append(m.getName()).append(" = function(");
			int params = m.getParameters().size();
			for (int i = 0; i < params; i++) {
				Parameter p = m.getParameters().get(i);
				if (i != 0)
					bw.append(", ");
				bw.append(p.getName());
			}
			if (params > 0) {
				bw.append(", ");
			}
			bw.append("callBack) {\n");
			bw.append("	return WsdlUtils.WsdlInvoke(").append(module).append(".module, '").append(m.getName()).append("', {");
			for (int i = 0; i < m.getParameters().size(); i++) {
				Parameter p = m.getParameters().get(i);
				if (i != 0)
					bw.write(", ");
				bw.append(p.getName()).append(":").append(p.getName());
			}
			bw.append("}, callBack);\n");
			bw.append("};\n");
		}
		bw.close();
	}

	private String url = "";
	private String xmlns = "";
	private String module = "";
	private ArrayList<Method> methods;

}
