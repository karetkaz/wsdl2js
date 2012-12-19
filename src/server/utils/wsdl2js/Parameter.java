package server.utils.wsdl2js;

public class Parameter {
	protected String name;
	protected AbstractDatatype type;

	public Parameter(String name, AbstractDatatype type) {
		super();
		this.name = name;
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public AbstractDatatype getType() {
		return type;
	}
}
