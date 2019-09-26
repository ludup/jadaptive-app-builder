package com.jadaptive.json;

import java.util.Objects;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.jadaptive.app.ApplicationProperties;
import com.jadaptive.session.Session;
import com.jadaptive.session.SessionService;
import com.jadaptive.session.SessionUtils;
import com.jadaptive.tenant.TenantService;

@Component
public class SessionInterceptor extends HandlerInterceptorAdapter {

	@Autowired
	TenantService tenantService; 
	
	@Autowired
	SessionUtils sessionUtils;
	
	@Autowired
	SessionService sessionService; 
	
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		
		
		Session session = sessionUtils.getActiveSession(request);
		if(!ApplicationProperties.getValue("enableAuthentication", false) && Objects.nonNull(session)) {
			tenantService.setCurrentTenant(session.getCurrentTenant());
			return super.preHandle(request, response, handler);			
		} else {
			response.sendError(403, "Authentication required");
		}

		return super.preHandle(request, response, handler);
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
		tenantService.clearCurrentTenant();
		super.postHandle(request, response, handler, modelAndView);
	}

	

}
