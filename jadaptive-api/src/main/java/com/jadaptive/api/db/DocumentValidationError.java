package com.jadaptive.api.db;

public class DocumentValidationError {

	String formVariable;
	
	String error;
	
	public DocumentValidationError() { }

	public DocumentValidationError(String formVariable, String error) {
		super();
		this.formVariable = formVariable;
		this.error = error;
	}

	public String getFormVariable() {
		return formVariable;
	}

	public void setFormVariable(String formVariable) {
		this.formVariable = formVariable;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}
}
