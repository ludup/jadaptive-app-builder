package com.jadaptive.app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.jadaptive.app.auth.DefaultTenantInterceptor;
import com.jadaptive.app.auth.SessionInterceptor;
import com.jadaptive.app.auth.quota.QuotaInterceptor;

@Configuration
@ServletComponentScan(basePackages = { "com.jadaptive.app" })
public class WebMvcConfig implements WebMvcConfigurer {

	@Autowired
	private DefaultTenantInterceptor tenantInterceptor;
	
	@Autowired
	private SessionInterceptor sessionInterceptor;

	@Autowired
	private QuotaInterceptor quotaInterceptor;

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(tenantInterceptor);
		registry.addInterceptor(sessionInterceptor);
		registry.addInterceptor(quotaInterceptor);
	}

}