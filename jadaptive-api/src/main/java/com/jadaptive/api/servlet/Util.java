package com.jadaptive.api.servlet;

import javax.servlet.http.HttpServletRequest;

public class Util {


	public static String generateBaseUrl(HttpServletRequest request) {
		StringBuffer b = new StringBuffer();
		b.append(request.getProtocol());
		b.append("://");
		b.append(request.getHeader("Host"));
		return b.toString();
	}
}
