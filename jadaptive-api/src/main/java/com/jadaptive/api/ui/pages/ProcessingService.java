package com.jadaptive.api.ui.pages;

public interface ProcessingService {

	String setupJob(String bundle, String title, String message, String feedbackSuccess, String feedbackError,
			String returnURL, Runnable task);

}
