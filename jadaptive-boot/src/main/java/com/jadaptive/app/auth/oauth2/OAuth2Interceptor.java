package com.jadaptive.app.auth.oauth2;

import java.text.MessageFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.jadaptive.api.app.App;
import com.jadaptive.api.auth.oauth2.OAuth2Requirement;
import com.jadaptive.api.auth.oauth2.OAuth2Response;
import com.jadaptive.api.auth.oauth2.OAuth2Scope;
import com.jadaptive.api.auth.oauth2.OAuth2Token;
import com.jadaptive.api.auth.oauth2.OAuth2TokenService;
import com.jadaptive.api.auth.oauth2.ResponseEntityException;
import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.api.permissions.PermissionService.UncheckedCloseable;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class OAuth2Interceptor implements HandlerInterceptor {
	
	private final static Logger LOG = LoggerFactory.getLogger(OAuth2Interceptor.class);

	@Autowired
	private OAuth2TokenService oauth2TokenService;

	@Autowired
	private PermissionService permissionService;

	@Autowired
	private App applicationService;

	@Autowired
	private AllApiAccessScope allApiAccessScope;
	
	private final ThreadLocal<UncheckedCloseable> userContext = new ThreadLocal<>();

	@Override
	public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler)
			throws Exception {
		if (handler instanceof HandlerMethod) {

			var method = (HandlerMethod) handler;
			var acAnnotation = method.getMethodAnnotation(OAuth2Requirement.class);
			if (acAnnotation != null) {
				OAuth2Scope scope = null;

				if (method.getBean() instanceof OAuth2Scope) {
					throw new IllegalStateException(MessageFormat.format(
							"The {0} annotation is no longer supported with {1} that are also controllers.",
							OAuth2Requirement.class.getName(), OAuth2Scope.class.getName()));
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
					authHdr = request.getHeader("Authentication");
					if(authHdr != null) {
						LOG.warn("This client is using `Authentication` header as opposed to `Authorization`. This is deprecated.");
					}
				}
				if(authHdr == null) {
					if(acAnnotation.required()) {
						if(acAnnotation.response()) {
							OAuth2Response.authenticateResponse(response, "JAD");
							return false;
						}
						else {
							throw new IllegalStateException("No authorization provided.");
						}
					}
				}
				
				try {
					if(authHdr != null) {
						var tkn = oauth2TokenService.authenticateRequest(request, response, authHdr, scope);
						OAuth2Token.set(tkn);
						if(acAnnotation.asUser()) {
							userContext.set(permissionService.userContext());
						}
					}
				}
				catch(ResponseEntityException e) {
					/* We can't send an entity here, so just send the header and the response code */
					if(acAnnotation.required()) {
						response.sendError(e.getEntity().getStatusCode().value());
						return false;
					}
				}
			}
		}

		return true;
	}

	@Override
	public void postHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler,
			@Nullable ModelAndView modelAndView) throws Exception {
		OAuth2Token.clear();
		var uc = userContext.get();
		if(uc != null) {
			try {
				uc.close();
			}
			finally { 
				userContext.remove();
			}
		}
	}

}