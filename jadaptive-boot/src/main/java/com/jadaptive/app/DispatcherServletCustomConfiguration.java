package com.jadaptive.app;
import org.springframework.boot.autoconfigure.web.servlet.DispatcherServletPath;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DispatcherServletCustomConfiguration {
    
    @Bean
    public DispatcherServletPath dispatcherServletPath() {
    	return new DispatcherServletPath() {
			@Override
			public String getPath() {
				return "/app/";
			}
		};
    }
}