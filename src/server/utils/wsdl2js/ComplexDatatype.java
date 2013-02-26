package server.utils.wsdl2js;

import java.util.ArrayList;


public class ComplexDatatype implements AbstractDatatype {
	protected ArrayList<Parameter> types = new ArrayList<Parameter>();
	protected String name;

	public ComplexDatatype(String name) {
		this.name = name;
	}

	public void addElement(Parameter type) {
		types.add(type);
	}

	@Override
	public String getType(String tabs) {
		if (types.size() > 0) {
			StringBuilder sb = new StringBuilder(name + " {\n");

			for (int i = 0; i < types.size(); i++) {
				if (i < types.size() - 1) {
					sb.append(tabs).append(types.get(i).getName()).append(" ").append(types.get(i).getType().getType(tabs + "\t")).append(", \n");
				} else {
					sb.append(tabs).append(types.get(i).getName()).append(" ").append(types.get(i).getType().getType(tabs + "\t")).append("\n").append(tabs + "}");
				}
			}
			return sb.toString();
		}
		return "";
	}

	@Override
	public String getName() {
		return name;
	}
}
