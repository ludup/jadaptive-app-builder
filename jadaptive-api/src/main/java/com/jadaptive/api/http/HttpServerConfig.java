package com.jadaptive.api.http;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HttpServerConfig {

	@Value("${server.port:-0}")
	private int httpsPort;

	@Value("${server.http.port:-0}")
	private int httpPort;

	public int getHttpsPort() {
		return httpsPort;
	}

	public int getHttpPort() {
		return httpPort;
	}

}