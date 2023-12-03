package com.jadaptive.api.tasks;

public interface FeedbackService {

	void automationStarted(String executionId, String bundle, String message, Object... args);

	void automationFinished(String executionId, FeedbackStatus status, String bundle, String message, Object... args);

	void info(String executionId, String bundle, String message, Object... args);
	
	void warning(String executionId, String bundle, String message, Object... args);
	
	void error(String executionId, String bundle, String message, Object... args);

	void automationCreated(String executionId);

	void success(String executionId, String bundle, String message, Object... args);
	
	FeedbackMessage nextMessage(String executionId) throws InterruptedException;

	void startProgress(String executionId, long start, long length);

	void progress(String executionId, long count);

	void endProgress(String executionId);


}
