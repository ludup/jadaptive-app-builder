package com.jadaptive.app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.jadaptive.json.SessionInterceptor;

@Configuration
@ServletComponentScan(basePackages = { "com.jadaptive" })
public class WebMvcConfig implements WebMvcConfigurer {
 
	@Autowired
	SessionInterceptor sessionInterceptor;
	
   @Override
   public void addInterceptors(InterceptorRegistry registry) {
      registry.addInterceptor(sessionInterceptor);
   }
 
}