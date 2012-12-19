package server.utils.wsdl2js;

public class SimpleDatatype implements AbstractDatatype {
	protected String type;
	public SimpleDatatype(String type) {
		this.type = type;
	}

	@Override
	public String getType(String tabs) {
		return type;
	}

	@Override
	public String getName() {
		return type;
	}
}
