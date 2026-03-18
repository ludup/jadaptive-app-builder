package com.jadaptive.api.db;

public class DocumentValidationError {

	private String formVariable;
	private String error;
	private String offendingValue;
	
	public DocumentValidationError() { }

	public DocumentValidationError(String offendingValue, String formVariable, String error) {
		super();
		this.formVariable = formVariable;
		this.error = error;
		this.offendingValue = offendingValue;
	}

	public String getFormVariable() {
		return formVariable;
	}

	public String getError() {
		return error;
	}

	public String getOffendingValue() {
		return offendingValue;
	}

}
