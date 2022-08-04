package com.jadaptive.api.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

public class Request {

	private static ThreadLocal<HttpServletRequest> threadRequests = new ThreadLocal<HttpServletRequest>();
	private static ThreadLocal<HttpServletResponse> threadResponses = new ThreadLocal<HttpServletResponse>();
	
	public static void setUp(HttpServletRequest request, HttpServletResponse response) {
		threadRequests.set(request);
		threadResponses.set(response);
	}
	
	public static HttpServletRequest get() {
		return threadRequests.get();
	}
	
	public static HttpServletResponse response() {
		return threadResponses.get();
	}
	
	public static boolean isAvailable() {
		return threadRequests.get()!=null;
	}
	
	public static void tearDown() {
		threadRequests.remove();
		threadResponses.remove();
	}
	
	public static String getRemoteAddress() {
		String xForwardedFor = Request.get().getHeader("X-Forwarded-For");
		if(StringUtils.isNotBlank(xForwardedFor)) {
			String[] results = xForwardedFor.split(",");
			return results[0];
		}
		return Request.get().getRemoteAddr();
	}

	public static String generateBaseUrl(HttpServletRequest request) {
		StringBuffer b = new StringBuffer();
		b.append(request.getScheme());
		b.append("://");
		b.append(request.getHeader("Host"));
		return b.toString();
	}
}
