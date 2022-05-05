package com.jadaptive.api.ui;

public class DarkFeedback extends Feedback {

	public DarkFeedback(String message) {
		super(message);
	}
	
	public DarkFeedback(String bundle, String i18n, Object[] args) {
		super("fa-square-exclamation", bundle, i18n, "alert-dark", args);
	}

}
