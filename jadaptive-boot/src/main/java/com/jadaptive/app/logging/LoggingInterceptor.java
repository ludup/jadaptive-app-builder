package com.jadaptive.app.logging;

import java.time.Instant;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class LoggingInterceptor implements HandlerInterceptor {

	final static String TIME_STARTED_ATTR = "logging.request.timeStarted";

	@Autowired
	private NCSARequestLog requestLog;
	
	@PostConstruct
	private void setup() {

		if(!requestLog.isStarted()) {
			requestLog.setFilename(System.getProperty("jadaptive.requestLogPath", "logs/request.log"));
			requestLog.setRetainDays(30);
			requestLog.setAppend(true);
			requestLog.setLogDispatch(true);
			requestLog.setLogLatency(true);
			requestLog.setPreferProxiedForAddress(true);
			try {
				requestLog.start();
			} catch (Exception e) {
				throw new IllegalStateException("Failed to start NCSA request log.", e);
			}
		}
	}

	@Override
	public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) {
		request.setAttribute(TIME_STARTED_ATTR, Instant.now());
		return true;
	}

	@Override
	public void afterCompletion(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler,
			@Nullable Exception ex) {
		requestLog.log(request, response);
	}
}
