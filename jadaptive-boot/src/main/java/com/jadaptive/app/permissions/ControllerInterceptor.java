package com.jadaptive.app.permissions;

import java.io.FileNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.jadaptive.api.permissions.AccessDeniedException;
import com.jadaptive.api.permissions.AuthenticatedContext;
import com.jadaptive.api.permissions.AuthenticatedController;
import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.api.session.Session;
import com.jadaptive.api.session.UnauthorizedException;
import com.jadaptive.api.ui.Redirect;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class ControllerInterceptor implements HandlerInterceptor {

	static Logger log = LoggerFactory.getLogger(ControllerInterceptor.class);
	
	@Autowired
	private PermissionService permissionService; 
	
	@Override
	public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler)
			throws Exception {
		
		if (handler instanceof HandlerMethod) {

			var method = (HandlerMethod) handler;

			var acAnnotation = method.getMethodAnnotation(AuthenticatedContext.class);
			if (acAnnotation != null) {
				checkMethod(method);

				var contrl = (AuthenticatedController) method.getBean();
				if (acAnnotation.preferActive() && Session.getOr(request).isPresent()) {
					if (acAnnotation.system())
						contrl.setupSystemContext();
					else
						contrl.setupUserContext();
				} else if(acAnnotation.user()) { 
					contrl.setupUserContext();
				} else if (acAnnotation.system()) {
					contrl.setupSystemContext();
				} else {
					contrl.setupUserContext();
				}
			}
		}

		return true;

	}

	protected void checkMethod(HandlerMethod method) {
		if (!(method.getBean() instanceof AuthenticatedController)) {
			if (log.isErrorEnabled()) {
				log.error(
						"Use of @AuthenticationRequired and @AuthenticatedContext annotation is restricted to subclass of AuthenticatedController");
			}
			throw new IllegalArgumentException(
					"Use of @AuthenticationRequired and @AuthenticatedContext annotation is restricted to subclass of AuthenticatedController. " + method.getBean().getClass() + " is not.");
		}
	}

	@Override
	public void postHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler,
			@Nullable ModelAndView modelAndView) throws Exception {

		var clearContext = false;
		if (handler instanceof HandlerMethod) {
			HandlerMethod method = (HandlerMethod) handler;
			clearContext = method.getMethodAnnotation(AuthenticatedContext.class) != null;
		}

		if (clearContext) {
			if (permissionService.hasUserContext()) {
				permissionService.clearUserContext();
			} else {
				if (log.isInfoEnabled()) {
					log.info(
							"{} {} was expecting to have a context to clear, but there was none. This suggests a coding error.",
							request.getMethod(), request.getRequestURI());
				}
			}
		} 
	}

	@Override
	public void afterCompletion(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler, @Nullable Exception ex)
			throws Exception {
		Throwable thrw = ex;
		if(thrw== null) {
			thrw = (Throwable)request.getAttribute(DispatcherServlet.EXCEPTION_ATTRIBUTE);
		}
		if(thrw != null && !(thrw instanceof Redirect)) {
			if(thrw instanceof FileNotFoundException || thrw instanceof NoResourceFoundException) {
				log.warn("Resource not found {}", request.getRequestURI());
				return;
			} else if(thrw instanceof AccessDeniedException) {
				log.warn("Access denied for {} URL {}", request.getMethod(), request.getRequestURL().toString());
				response.sendError(HttpStatus.FORBIDDEN.value());
				return;
			} else if(thrw instanceof UnauthorizedException) {
				log.warn("Unauthorised request {} URL {}", request.getMethod(), request.getRequestURL().toString());
				response.sendError(HttpStatus.UNAUTHORIZED.value());
				return;
			}
			log.error("Request failure on {} URL {}", request.getMethod(), request.getRequestURL().toString(), thrw);
		}
	}
}
