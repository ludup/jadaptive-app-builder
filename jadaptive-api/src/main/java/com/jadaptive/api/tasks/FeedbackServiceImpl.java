package com.jadaptive.api.tasks;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;

import org.springframework.stereotype.Service;

import com.jadaptive.api.app.I18N;

@Service
public class FeedbackServiceImpl implements FeedbackService {

	Map<String,ArrayBlockingQueue<FeedbackMessage>> queues = new HashMap<>();
	
	@Override
	public void automationCreated(String executionId) {
		queues.put(executionId, new ArrayBlockingQueue<>(999));
	}
	
	@Override
	public void automationStarted(String executionId, String bundle, String message, Object... args) {

		setMessage(executionId, FeedbackStatus.INFO, bundle, message, args);
	}
	
	private void setMessage(String executionId, FeedbackStatus status, String bundle, String message, Object... args) {
		ArrayBlockingQueue<FeedbackMessage> queue = queues.get(executionId);
		if(Objects.nonNull(queue)) {
			queue.add(new FeedbackMessage(status, I18N.getResource(bundle, message, args), false));
		}
	}

	@Override
	public void automationFinished(String executionId, FeedbackStatus status, String bundle, String message, Object... args) {
		
		ArrayBlockingQueue<FeedbackMessage> queue = queues.get(executionId);
		if(Objects.nonNull(queue)) {
			queue.add(new FeedbackMessage(status, I18N.getResource(bundle, message, args), true));
		}
		
	}
	
	@Override
	public void info(String executionId, String bundle, String message, Object... args) {
		
		setMessage(executionId, FeedbackStatus.INFO, bundle, message, args);
	}
	
	@Override
	public void success(String executionId, String bundle, String message, Object... args) {
		
		setMessage(executionId, FeedbackStatus.SUCCESS, bundle, message, args);
	}
	
	@Override
	public void warning(String executionId, String bundle, String message, Object... args) {
		
		setMessage(executionId, FeedbackStatus.WARNING, bundle, message, args);
	}
	
	@Override
	public void error(String executionId, String bundle, String message, Object... args) {
		
		setMessage(executionId, FeedbackStatus.ERROR, bundle, message, args);
	}
	
	@Override
	public FeedbackMessage nextMessage(String executionId) throws InterruptedException {
		return queues.get(executionId).take();
	}

	@Override
	public void startProgress(String executionId, long start, long length) {
		ArrayBlockingQueue<FeedbackMessage> queue = queues.get(executionId);
		if(Objects.nonNull(queue)) {
			queue.add(new FeedbackMessage(ProgressStatus.START, 0));
		}
	}

	@Override
	public void progress(String executionId, long count) {
		ArrayBlockingQueue<FeedbackMessage> queue = queues.get(executionId);
		if(Objects.nonNull(queue)) {
			queue.add(new FeedbackMessage(ProgressStatus.UPDATE, count));
		}
	}

	@Override
	public void endProgress(String executionId) {
		
		ArrayBlockingQueue<FeedbackMessage> queue = queues.get(executionId);
		if(Objects.nonNull(queue)) {
			queue.add(new FeedbackMessage(ProgressStatus.END, 100));
		}
	}
}
