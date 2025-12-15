package com.jadaptive.api.ui.pages;

import com.jadaptive.api.ui.pages.ProcessingJob.UncheckedRunnable;

public interface ProcessingService {

	String setupJob(String bundle, String title, String message, String feedbackSuccess, String feedbackError,
			String returnURL, UncheckedRunnable task);

}
