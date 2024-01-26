package com.jadaptive.app;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.jadaptive.api.app.ApplicationServiceImpl;
import com.jadaptive.app.permissions.ControllerInterceptor;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(ApplicationServiceImpl.getInstance().autowire(new ControllerInterceptor()));
    }
}