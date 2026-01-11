package com.jadaptive.app;

import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.jadaptive.api.app.ApplicationServiceImpl;
import com.jadaptive.app.auth.oauth2.OAuth2Interceptor;
import com.jadaptive.app.logging.LoggingInterceptor;
import com.jadaptive.app.permissions.ControllerInterceptor;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(@NonNull InterceptorRegistry registry) {
        registry.addInterceptor(ApplicationServiceImpl.getInstance().autowire(new ControllerInterceptor()));
        registry.addInterceptor(ApplicationServiceImpl.getInstance().autowire(new OAuth2Interceptor()));
        registry.addInterceptor(ApplicationServiceImpl.getInstance().autowire(new LoggingInterceptor()));
    }
}