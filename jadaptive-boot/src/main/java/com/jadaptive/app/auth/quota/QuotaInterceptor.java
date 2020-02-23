package com.jadaptive.app.auth.quota;

import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.jadaptive.api.app.SecurityPropertyService;
import com.jadaptive.api.permissions.AccessDeniedException;
import com.jadaptive.app.json.ResponseHelper;

@Component
public class QuotaInterceptor extends HandlerInterceptorAdapter {

	@Autowired
	private SecurityPropertyService securityService;
	
	@Autowired
	private QuotaService quotaService; 
	
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		
		Properties properties = securityService.resolveSecurityProperties(request.getRequestURI());
		
		if(Boolean.parseBoolean(properties.getProperty("quota.enabled", "false"))) {
			try {
				String group = properties.getProperty("quota.group", "defaultGroup");
				String key = request.getRemoteAddr();
				long quota = Long.parseLong(properties.getProperty("quota.threshold", "10000"));
				long period = Long.parseLong(properties.getProperty("quota.period", "86400"));
				
				quotaService.incrementQuota(group, key, quota, period);
			} catch(AccessDeniedException e) {
				ResponseHelper.send403Forbidden(request, response);
				return true;
			}
		}
		return super.preHandle(request, response, handler);
	}
	
	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {

		super.postHandle(request, response, handler, modelAndView);
	}
}
