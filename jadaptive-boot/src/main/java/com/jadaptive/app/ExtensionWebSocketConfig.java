package com.jadaptive.app;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

@Configuration
@EnableWebSocket
public class ExtensionWebSocketConfig implements WebSocketConfigurer {

	@Override
	public void registerWebSocketHandlers(@NonNull WebSocketHandlerRegistry registry) {
		ExtensionWebSocketHandler h = new ExtensionWebSocketHandler();
		registry.addHandler(h, "/socket/*")
				.addInterceptors(h);
	}

    @Bean
    public ServletServerContainerFactoryBean createWebSocketContainer() {
        ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
        // Set to 512KB to handle video frames and audio chunks comfortably
        container.setMaxBinaryMessageBufferSize(524288);
        container.setMaxTextMessageBufferSize(524288);
        return container;
    }
	
}