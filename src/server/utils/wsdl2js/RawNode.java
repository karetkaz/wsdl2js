package server.utils.wsdl2js;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class RawNode {

	protected enum NodeType {
		type,
		method,
		methodresponse,
		//exception,
	}

	protected Node node;
	protected NodeType nodeType;

	public RawNode(Node node, NodeType nodeType) {
		super();
		this.node = node;
		this.nodeType = nodeType;
		if (node.getNodeName().endsWith("Response"))
			nodeType = NodeType.methodresponse;
	}

	public NodeType getNodeType() {
		return nodeType;
	}

	public void setNodeType(NodeType nodeType) {
		this.nodeType = nodeType;
	}

	public Node getNode() {
		return node;
	}

	public String getName() {
		Element e = (Element) node;
		return e.getAttribute("name");
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof RawNode) {
			return ((RawNode) obj).getName().equals(this.getName());
		}
		return false;
	}
}
