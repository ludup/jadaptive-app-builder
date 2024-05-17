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
		return getRemoteAddress(Request.get());
	}
	
	public static String getRemoteAddress(HttpServletRequest req) {
		String xForwardedFor = req.getHeader("X-Forwarded-For");
		if(StringUtils.isNotBlank(xForwardedFor)) {
			String[] results = xForwardedFor.split(",");
			return results[0];
		}
		return req.getRemoteAddr();
	}

	public static String getThisHost() {
		return getThisHost(Request.get());
	}
	
	public static String getThisHostname(HttpServletRequest req) {
		String hostAndPort = getThisHost(req);
		int idx = hostAndPort.indexOf(':');
		return idx == -1 ? hostAndPort : hostAndPort.substring(0, idx);
	}
	
	public static int getThisPort(HttpServletRequest req) {
		String hostAndPort = getThisHost(req);
		int idx = hostAndPort.indexOf(':');
		return idx == -1 ? ( req.isSecure() ? 443 : 80 ) : Integer.parseInt(hostAndPort.substring(idx + 1));
	}
	
	public static String getThisHost(HttpServletRequest req) {
		String host = req.getHeader("X-Forwarded-Host");
		if(host == null || host.length() == 0)
			host = req.getHeader("X-Host");
		
		if(host!=null) {
			int idx;
			if ((idx = host.indexOf(":")) > -1) {
				host = host.substring(0, idx);
			}
		}
		return host;
	}

	public static String generateBaseUrl(HttpServletRequest request) {
		StringBuffer b = new StringBuffer();
		b.append(request.getScheme());
		b.append("://");
		b.append(request.getHeader("Host"));
		return b.toString();
	}
}
