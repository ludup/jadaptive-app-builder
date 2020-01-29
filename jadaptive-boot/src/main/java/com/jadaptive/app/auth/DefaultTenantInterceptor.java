package com.jadaptive.app.auth;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.jadaptive.api.tenant.TenantService;

@Component
public class DefaultTenantInterceptor extends HandlerInterceptorAdapter {

	@Autowired
	private TenantService tenantService;
	
	
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		tenantService.setCurrentTenant(request);
		return super.preHandle(request, response, handler);
	}
	
	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
		tenantService.clearCurrentTenant();
		super.postHandle(request, response, handler, modelAndView);
	}
}
