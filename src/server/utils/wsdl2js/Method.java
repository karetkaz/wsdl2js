package server.utils.wsdl2js;
import java.util.ArrayList;


public class Method {
	protected ArrayList<Parameter> parameters = new ArrayList<Parameter>();
	protected AbstractDatatype returnType;
	protected String name;

	public String getName() {
		return name;
	}

	public Method(String name) {
		super();
		this.name = name;
	}

	public ArrayList<Parameter> getParameters() {
		return parameters;
	}

	public AbstractDatatype getReturnType() {
		return returnType;
	}

	public void addParameter(Parameter p) {
		parameters.add(p);
	}

	public void setReturnType(AbstractDatatype returnType) {
		this.returnType = returnType;
	}
}
