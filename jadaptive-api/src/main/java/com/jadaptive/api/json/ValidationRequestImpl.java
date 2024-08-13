package com.jadaptive.api.json;

import java.util.Collection;

import com.jadaptive.api.db.DocumentValidationError;

public class ValidationRequestImpl extends RequestStatusImpl {

	Collection<DocumentValidationError> errors;
	
	public ValidationRequestImpl(boolean success, String message, Collection<DocumentValidationError> errors) {
		super(success, message);
		this.errors = errors;
	}

	public Collection<DocumentValidationError> getErrors() {
		return errors;
	}

	public void setErrors(Collection<DocumentValidationError> errors) {
		this.errors = errors;
	}
}
