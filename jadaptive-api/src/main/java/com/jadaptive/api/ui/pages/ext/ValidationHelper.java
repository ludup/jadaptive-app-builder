package com.jadaptive.api.ui.pages.ext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.jadaptive.api.db.DocumentValidationError;


public class ValidationHelper {

	private static ThreadLocal<List<DocumentValidationError>> validationErrors = ThreadLocal.withInitial(ArrayList::new);
	
	static ThreadLocal<Boolean> multipleValidation = ThreadLocal.withInitial(()->Boolean.FALSE);
	
	public static void enableMultipleValidation() {
		multipleValidation.set(Boolean.TRUE);
	}
	
	public static boolean isMultipleValidationEnabled() {
		return multipleValidation.get();
	}
	
	public static void disableMultipleValidation() {
		multipleValidation.set(Boolean.FALSE);
	}
	
	public static boolean hasErrors() {
		return !validationErrors.get().isEmpty();
	}
	
	public static String getOffendingValue(String fieldName) {
		return validationErrors.get().stream().filter(e -> e.getFormVariable().equals(fieldName)).map(e -> e.getOffendingValue()).findFirst().orElse(null);
	}
	
	public static void addError(String offendingValue, String fieldName, String error) {
		validationErrors.get().add(new DocumentValidationError(offendingValue, fieldName, error));
	}
	
	public static Collection<DocumentValidationError> getErrors() {
		return Collections.unmodifiableCollection(validationErrors.get());
	}

	public static void clear() {
		validationErrors.get().clear();
	}

}
