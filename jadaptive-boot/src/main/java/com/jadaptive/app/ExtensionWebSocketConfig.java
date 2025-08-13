package com.jadaptive.app;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class ExtensionWebSocketConfig implements WebSocketConfigurer {

	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
		ExtensionWebSocketHandler h = new ExtensionWebSocketHandler();
		registry.addHandler(h, "/socket/*")
				.addInterceptors(h);
	}

}