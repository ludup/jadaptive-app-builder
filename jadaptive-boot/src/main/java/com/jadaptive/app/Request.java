package com.jadaptive.app;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class Request {

	static ThreadLocal<HttpServletRequest> requests = new ThreadLocal<>();
	static ThreadLocal<HttpServletResponse> responses = new ThreadLocal<>();
	
	
	public static HttpServletRequest request() {
		return requests.get();
	}
	
	public static HttpServletResponse response() {
		return responses.get();
	}
	
	public static void setup(HttpServletRequest request, HttpServletResponse response) {
		requests.set(request);
		responses.set(response);
	}
	
	public static void tearDown() {
		requests.remove();
		responses.remove();
	}
}
