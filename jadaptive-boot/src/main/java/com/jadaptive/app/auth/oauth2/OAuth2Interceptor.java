package com.jadaptive.app.auth.oauth2;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.jadaptive.api.app.ApplicationService;
import com.jadaptive.api.auth.oauth2.OAuth2Requirement;
import com.jadaptive.api.auth.oauth2.OAuth2Scope;
import com.jadaptive.api.auth.oauth2.OAuth2TokenService;
import com.jadaptive.api.auth.oauth2.ResponseEntityException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class OAuth2Interceptor implements HandlerInterceptor {

	@Autowired
	private OAuth2TokenService oauth2TokenService;

	@Autowired
	private ApplicationService applicationService;

	@Autowired
	private AllApiAccessScope allApiAccessScope;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		if (handler instanceof HandlerMethod) {

			var method = (HandlerMethod) handler;
			var acAnnotation = method.getMethodAnnotation(OAuth2Requirement.class);
			if (acAnnotation != null) {
				OAuth2Scope scope = null;

				if (method.getBean() instanceof OAuth2Scope) {
					/*
					 * Legacy method. The controller IS an OAuth2Scope, i.e. it implements the
					 * interface.
					 * 
					 * It will checking the token and scopes itself
					 */
					return true;
				}

				if (acAnnotation.value().length() != 0) {
					for (var regScope : applicationService.getBeans(OAuth2Scope.class)) {
						if (regScope.getId().equals(acAnnotation.value())) {
							scope = regScope;
							break;
						}
					}
				}
				
				if(scope == null) {
					scope = allApiAccessScope;
				}

				var authHdr = request.getHeader("Authorization");
				if(authHdr == null) {
					response.addHeader("WWW-Authenticate", "Bearer realm=\"JAD\"");
					response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
					return false;
				}
				
				try {
					oauth2TokenService.authenticateRequest(request, response, authHdr, scope);
				}
				catch(ResponseEntityException e) {
					/* We can't send an entity here, so just send the header and the response code */
					response.sendError(e.getEntity().getStatusCode().value());
					return false;
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