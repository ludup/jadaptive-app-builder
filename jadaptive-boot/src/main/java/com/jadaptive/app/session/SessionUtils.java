package com.jadaptive.app.session;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.jadaptive.api.app.ApplicationProperties;
import com.jadaptive.api.entity.EntityNotFoundException;
import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.api.session.Session;
import com.jadaptive.api.session.SessionService;
import com.jadaptive.api.session.SessionTimeoutException;
import com.jadaptive.api.session.UnauthorizedException;
import com.jadaptive.api.tenant.Tenant;

@Component
public class SessionUtils {

	static Logger log = LoggerFactory.getLogger(SessionUtils.class);
	
	static boolean debugCSRF = "true".equals(System.getProperty("jadaptive.csrfDebugRequests"));

	public static final String AUTHENTICATED_SESSION = "authenticatedSession";
	public static final String SESSION_COOKIE = "JADAPTIVE_SESSION";
	public static final String CSRF_TOKEN = "JADAPTIVE_CSRF_TOKEN";

	public static final String USER_LOCALE = "userLocale";
	public static final String LOCALE_COOKIE = "JADAPTIVE_LOCALE";

	@Autowired
	SessionService sessionService;; 
	
	@Autowired
	PermissionService permissionService; 
	
	
	public Session getActiveSession(HttpServletRequest request) {
		
		Session session = null;
		
		permissionService.setupSystemContext();
		
		try {
			if(request.getParameterMap().containsKey(SESSION_COOKIE)) {
				session = sessionService.get(request.getParameter(SESSION_COOKIE));
			} else if(request.getHeader(SESSION_COOKIE) != null) {
				session = sessionService.get((String)request.getHeader(SESSION_COOKIE));
			}
			
			if (session != null && sessionService.isLoggedOn(session, true)) {
				return session;
			}
			
			if (request.getAttribute(AUTHENTICATED_SESSION) != null) {
				session = (Session) request.getAttribute(AUTHENTICATED_SESSION);
				if(sessionService.isLoggedOn(session, true)) {
					return session;
				}
			}
			
			if (request.getSession().getAttribute(AUTHENTICATED_SESSION) != null) {
				session = (Session) request.getSession().getAttribute(
						AUTHENTICATED_SESSION);
				if(sessionService.isLoggedOn(session, true)) {
					return session;
				}
			}
			
			if(Objects.nonNull(request.getCookies())) {
				for (Cookie c : request.getCookies()) {
					if (c.getName().equals(SESSION_COOKIE)) {
						session = sessionService.get(c.getValue());
						if (session != null && sessionService.isLoggedOn(session, true)) {
							return session;
						}
					}
				}
			}
			
		} catch(EntityNotFoundException e) { 

		} finally {
			permissionService.clearUserContext();
		}

		return null;
	}

	/**
	 * Use AuthenticatedService.getCurrentPrincipal instead ensuring that a valid
	 * principal context has been setup previously.
	 */
	public Tenant getCurrentTenant(HttpServletRequest request)
			throws UnauthorizedException {
		Session session = getActiveSession(request);
		if (session == null)
			throw new UnauthorizedException();
		return session.getCurrentTenant();
	}

	public Session touchSession(HttpServletRequest request,
			HttpServletResponse response) throws UnauthorizedException,
			SessionTimeoutException {
		return touchSession(request, response, true);
	}
	
	public Session touchSession(HttpServletRequest request,
			HttpServletResponse response, boolean performCsrfCheck) throws UnauthorizedException,
			SessionTimeoutException {

		Session session = null;
		
		if (request.getSession().getAttribute(AUTHENTICATED_SESSION) == null) {
			if (log.isDebugEnabled()) {
				log.debug("Session object not attached to HTTP session");
			}
			session = getActiveSession(request);
			if (session == null) {
				if (log.isDebugEnabled()) {
					log.debug("No session attached to request");
				}
				throw new UnauthorizedException();
			}
			if (!sessionService.isLoggedOn(session, true)) {
				throw new SessionTimeoutException();
			}
		} else {
			session = (Session) request.getSession().getAttribute(
					AUTHENTICATED_SESSION);
			if (!sessionService.isLoggedOn(session, true)) {
				throw new UnauthorizedException();
			}
		}

		if(performCsrfCheck) {
			verifySameSiteRequest(request, session);
		}
		// Preserve the session for future lookups in this request and session
		request.setAttribute(AUTHENTICATED_SESSION, session);
		request.getSession().setAttribute(AUTHENTICATED_SESSION, session);

		addSessionCookies(request, response, session);

		return session;

	}

	public Session getSession(HttpServletRequest request)
			throws UnauthorizedException, SessionTimeoutException {

		/**
		 * This method SHOULD NOT touch the session.
		 */
		Session session = null;
		
		if(request.getParameterMap().containsKey(SESSION_COOKIE)) {
			session = sessionService.get(request.getParameter(SESSION_COOKIE));
		} else if(request.getHeader(SESSION_COOKIE) != null) {
			session = sessionService.get((String)request.getHeader(SESSION_COOKIE));
		}
		
		if (session != null && sessionService.isLoggedOn(session, false)) {
			return session;
		}
		
		if (request.getAttribute(AUTHENTICATED_SESSION) != null) {
			session = (Session) request.getAttribute(AUTHENTICATED_SESSION);
			if(sessionService.isLoggedOn(session, false)) {
				return session;
			}
		}
		
		if (request.getSession().getAttribute(AUTHENTICATED_SESSION) != null) {
			session = (Session) request.getSession().getAttribute(
					AUTHENTICATED_SESSION);
			if(sessionService.isLoggedOn(session, false)) {
				return session;
			}
		}
		for (Cookie c : request.getCookies()) {
			if (c.getName().equals(SESSION_COOKIE)) {
				session = sessionService.get(c.getValue());
				if (session != null && sessionService.isLoggedOn(session, false)) {
					return session;
				}
			}
		}

		throw new UnauthorizedException();
	}
	
	private void verifySameSiteRequest(HttpServletRequest request, Session session) throws UnauthorizedException {
		

		if(isValidCORSRequest(request)) {
			return;
		}
		
		if(!ApplicationProperties.getValue("security.enableCSRFProtection", true)) {
			return;
		}
		
		String requestToken = request.getHeader("X-Csrf-Token");
		if(requestToken==null) {
			requestToken = request.getParameter("token");
			if(requestToken==null) {
				log.warn(String.format("CSRF token missing from %s", request.getRemoteAddr()));
				debugRequest(request);
				throw new UnauthorizedException();
			}
		}
		
		if(!session.getCsrfToken().equals(requestToken)) {
			log.warn(String.format("CSRF token mistmatch from %s", request.getRemoteAddr()));
			debugRequest(request);
			throw new UnauthorizedException();
		}

	}

	protected void debugRequest(HttpServletRequest request) {
		if(debugCSRF) {
			log.warn(String.format("The request URI was %s, and contained the following parameters :-",request.getRequestURI()));
			for(Map.Entry<String, String[]> en : request.getParameterMap().entrySet()) {
				log.warn(String.format("  %s = %s", en.getKey(), String.join(",", en.getValue())));
			}
			log.warn("And the following headers :-");
			for(Enumeration<String> hdrEnum = request.getHeaderNames(); hdrEnum.hasMoreElements(); ) {
				String hdr = hdrEnum.nextElement();
				log.warn(String.format("  %s = %s", hdr, request.getHeader(hdr)));
			}
		}
	}

	public boolean isValidCORSRequest(HttpServletRequest request) {
		
		String requestOrigin = request.getHeader("Origin");
		
		if(ApplicationProperties.getValue("cors.enabled", false) && !Objects.isNull(requestOrigin)) {
			
			List<String> origins = Arrays.asList(ApplicationProperties.getValue("cors.origins", "").split(","));
		
			if(log.isInfoEnabled()) {
				log.info("CORS request for origin {}", requestOrigin);
			}
			if(origins.contains(requestOrigin)) {
				return true;
			}
		}

		return false;
	}

	public void addSessionCookies(HttpServletRequest request,
			HttpServletResponse response, Session session) {

		Cookie cookie = new Cookie(SESSION_COOKIE, session.getUuid());
		cookie.setMaxAge((session.getSessionTimeout() > 0 ? 60 * session.getSessionTimeout() : Integer.MAX_VALUE));
		if(request.getProtocol().equalsIgnoreCase("https")) {
			cookie.setSecure(true);
		} else {
			cookie.setSecure(false);
			cookie.setHttpOnly(true);
		}
		cookie.setPath("/");
		response.addCookie(cookie);
		
		cookie = new Cookie(CSRF_TOKEN, session.getCsrfToken());
		cookie.setMaxAge((session.getSessionTimeout() > 0 ? 60 * session.getSessionTimeout() : Integer.MAX_VALUE));
		cookie.setSecure(request.getProtocol().equalsIgnoreCase("https"));
		cookie.setPath("/");
		response.addCookie(cookie);
	
	}

	public Locale getLocale(HttpServletRequest request) {

		if (request.getSession().getAttribute(USER_LOCALE) == null) {

			Cookie[] cookies = request.getCookies();
			for (Cookie c : cookies) {
				if (c.getName().equals(LOCALE_COOKIE)) {
					return new Locale(c.getValue());
				}
			}
			return Locale.getDefault();
		} else {
			return new Locale((String) request.getSession().getAttribute(
					USER_LOCALE));
		}

	}

	public void setLocale(HttpServletRequest request,
			HttpServletResponse response, String locale) {

		request.getSession().setAttribute(USER_LOCALE, locale);

		Cookie cookie = new Cookie(LOCALE_COOKIE, locale);
		cookie.setMaxAge(Integer.MAX_VALUE);
		cookie.setPath("/");
		if(request.getProtocol().equalsIgnoreCase("https")) {
			cookie.setSecure(true);
		} else {
			cookie.setSecure(false);
			cookie.setHttpOnly(true);
		}
		cookie.setDomain(request.getServerName());
		response.addCookie(cookie);

	}

	public void touchSession(Session session) throws SessionTimeoutException {

		if (!sessionService.isLoggedOn(session, true)) {
			throw new SessionTimeoutException();
		}
	}

	public boolean hasActiveSession(HttpServletRequest request) {
		try {
			return getSession(request)!=null;
		} catch (UnauthorizedException | SessionTimeoutException e) {
			return false;
		}
	}

}
