package com.jadaptive.api.ui.pages;

public class ProcessingJob {

	String bundle;
	String title;
	String message;
	String feedbackSuccess;
	String feedbackError;
	String returnURL;
	Runnable task;
	
	
	public ProcessingJob(String bundle, String title, String message, String feedbackSuccess, String feedbackError,
			String returnURL, Runnable task) {
		super();
		this.bundle = bundle;
		this.title = title;
		this.message = message;
		this.feedbackSuccess = feedbackSuccess;
		this.feedbackError = feedbackError;
		this.returnURL = returnURL;
		this.task = task;
	}
	
	public String getBundle() {
		return bundle;
	}
	public void setBundle(String bundle) {
		this.bundle = bundle;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getFeedbackSuccess() {
		return feedbackSuccess;
	}
	public void setFeedbackSuccess(String feedbackSuccess) {
		this.feedbackSuccess = feedbackSuccess;
	}
	public String getFeedbackError() {
		return feedbackError;
	}
	public void setFeedbackError(String feedbackError) {
		this.feedbackError = feedbackError;
	}
	public String getReturnURL() {
		return returnURL;
	}
	public void setReturnURL(String returnURL) {
		this.returnURL = returnURL;
	}
	public Runnable getTask() {
		return task;
	}
	public void setTask(Runnable task) {
		this.task = task;
	}
	
	
}
