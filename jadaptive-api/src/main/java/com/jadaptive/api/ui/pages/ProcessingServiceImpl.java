package com.jadaptive.api.ui.pages;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.jadaptive.api.servlet.Request;
import com.jadaptive.api.ui.pages.ProcessingJob.UncheckedRunnable;

@Service
public class ProcessingServiceImpl implements ProcessingService {

	
	@Override
	public String setupJob(String bundle, String title, String message, 
			String feedbackSuccess, String feedbackError, String returnURL, 
			UncheckedRunnable task) {
		
		String uuid = UUID.randomUUID().toString();
		
		Request.get().getSession().setAttribute(
				uuid,
				new ProcessingJob(bundle,
						title,
						message, 
						feedbackSuccess, 
						feedbackError, 
						returnURL, 
						task));
		
		return uuid;
		
	}
}
