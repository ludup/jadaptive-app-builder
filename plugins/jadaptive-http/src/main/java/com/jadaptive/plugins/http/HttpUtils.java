package com.jadaptive.plugins.http;

public class HttpUtils {

	private static final HttpUtilsImpl instance = new HttpUtilsApacheImpl();
	
	public static HttpUtilsImpl defaultClient() {
		return instance;
	}
}
