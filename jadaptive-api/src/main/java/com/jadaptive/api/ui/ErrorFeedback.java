package com.jadaptive.api.ui;

public class ErrorFeedback extends Feedback {

	public ErrorFeedback(String message) {
		super("fa-square-exclamation", message, "alert-danger");
	}
	
	public ErrorFeedback(String bundle, String i18n, Object[] args) {
		super("fa-square-exclamation", bundle, i18n, "danger", args);
	}

}
