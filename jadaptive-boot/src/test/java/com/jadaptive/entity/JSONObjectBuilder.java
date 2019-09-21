package com.jadaptive.entity;

public class JSONObjectBuilder {

	StringBuffer buf = new StringBuffer();
	int indent = 0;
	boolean wasField;
	JSONObjectBuilder() {	
	}
	
	public JSONObjectBuilder startObject() {
		buf.append("{");
		indent += 3;
		newline();
		return this;
	}
	
	public JSONObjectBuilder textField(String name, String value) {
		if(wasField) {
			buf.append(",");
			newline();
		}
		buf.append("\"");
		buf.append(name);
		buf.append("\": \"");
		buf.append(value);
		buf.append("\"");
		
		wasField = true;
		return this;
		
	}
	
	public JSONObjectBuilder endObject() {
		
		indent -= 3;
		newline();
		buf.append("}");
		return this;
	}
	
	public String toString() {
		return buf.toString();
	}
	
	private void newline() {
		buf.append(System.lineSeparator());
		pad(indent);
	}
	
	private void pad(int spaces) {
		for(int i=0;i<spaces;i++) {
			buf.append(' ');
		}
	}
	
	public JSONObjectBuilder numberField(String name, Number l) {
		if(wasField) {
			buf.append(",");
			newline();
		}
		buf.append("\"");
		buf.append(name);
		buf.append("\": ");
		buf.append(l.toString());
		
		wasField = true;
		return this;
	}

	public JSONObjectBuilder booleanField(String name, Boolean b) {
		if(wasField) {
			buf.append(",");
			newline();
		}
		buf.append("\"");
		buf.append(name);
		buf.append("\": ");
		buf.append(b.toString());
		
		wasField = true;
		return this;
	}

}
