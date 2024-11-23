package com.jadaptive.app.auth.oauth2;

import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.jadaptive.api.auth.oauth2.OAuth2Requirement;
import com.jadaptive.api.auth.oauth2.OAuth2Scope;
import com.jadaptive.api.auth.oauth2.OAuth2Token;
import com.jadaptive.api.permissions.AccessDeniedException;
import com.jadaptive.api.servlet.Request;
import com.jadaptive.api.session.Session;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class OAuth2Interceptor implements HandlerInterceptor {

	
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		if (handler instanceof HandlerMethod) {

			var method = (HandlerMethod) handler;
			var token = Request.isAvailable() ? Session.getOr().map(s -> s.getAttribute(OAuth2Token.class)).orElse(null) : null;
			
			if(method.getBean() instanceof OAuth2Scope) {
				/* Legacy method. The controller IS an OAuth2Scope,
				 * i.e. it implements the interface. 
				 * 
				 * It will checking the token and scopes itself
				 */
				return true;
			}

			var acAnnotation = method.getMethodAnnotation(OAuth2Requirement.class);
			
			if(token == null) {
				if(acAnnotation != null && acAnnotation.required()) {
					throw new AccessDeniedException("Controller has @" + OAuth2Requirement.class + " with required = true, but this session is not authenticated.");
				}
			}
			else {
				if(token.getScopes().contains(AllApiAccessScope.ALL_API_ACCESS)) {
					/* Special case, this special scope has access to everything */
					return true;
				}
	
				if (acAnnotation != null) {
					for(var scope : token.getScopes()) {
						for(var mscope : acAnnotation.value()) {
							if(mscope.equals(scope)) {
								return true;
							}
						}
					}
					
					throw new AccessDeniedException("None of the required scopes are allowed for this authentication token.");
				}
			}
		}

		return true;
	}
	
	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
	}
}