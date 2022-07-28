package com.jadaptive.api.session;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.jadaptive.api.app.ApplicationProperties;
import com.jadaptive.api.entity.ObjectNotFoundException;
import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.api.servlet.Request;
import com.jadaptive.api.user.User;
import com.jadaptive.utils.Utils;

@Component
public class SessionUtils {

	static Logger log = LoggerFactory.getLogger(SessionUtils.class);
	
	static boolean debugCSRF = "true".equals(System.getProperty("jadaptive.csrfDebugRequests"));

	public static final String AUTHENTICATED_SESSION = "authenticatedSession";
	public static final String SESSION_COOKIE = "JADAPTIVE_SESSION";
	public static final String CSRF_TOKEN = "JADAPTIVE_CSRF_TOKEN";

	public static final String USER_LOCALE = "userLocale";
	public static final String LOCALE_COOKIE = "JADAPTIVE_LOCALE";

	static ThreadLocal<User> threadUsers = new ThreadLocal<>();
	
	@Autowired
	private SessionService sessionService;; 
	
	@Autowired
	private PermissionService permissionService; 
	
	public User getCurrentUser() {
		return permissionService.getCurrentUser();
	}
	
	public Session getActiveSession(HttpServletRequest request) {
		
		Session session = null;
		
		permissionService.setupSystemContext();
		
		try {

			if (request.getAttribute(AUTHENTICATED_SESSION) != null) {
				session = (Session) request.getAttribute(AUTHENTICATED_SESSION);
				if(sessionService.isLoggedOn(session, true)) {
					return verifySameSiteRequest(request, session);
				}
			}
			
			if (request.getSession().getAttribute(AUTHENTICATED_SESSION) != null) {
				session = (Session) request.getSession().getAttribute(
						AUTHENTICATED_SESSION);
				if(sessionService.isLoggedOn(session, true)) {
					return verifySameSiteRequest(request, session);
				}
			}
			
			if(request.getParameterMap().containsKey(SESSION_COOKIE)) {
				session = sessionService.getSession(request.getParameter(SESSION_COOKIE));
			} else if(request.getHeader(SESSION_COOKIE) != null) {
				session = sessionService.getSession((String)request.getHeader(SESSION_COOKIE));
			}
			
			if (session != null && sessionService.isLoggedOn(session, true)) {
				return verifySameSiteRequest(request, session);
			}
			
			if(Objects.nonNull(request.getCookies())) {
				for (Cookie c : request.getCookies()) {
					if (c.getName().equals(SESSION_COOKIE)) {
						session = sessionService.getSession(c.getValue());
						if (session != null && sessionService.isLoggedOn(session, true)) {
							return verifySameSiteRequest(request, session);
						}
					}
				}
			}
			
		} catch(UnauthorizedException | ObjectNotFoundException e) { 

		} finally {
			permissionService.clearUserContext();
		}

		return null;
	}

	public Session touchSession(HttpServletRequest request,
			HttpServletResponse response, Session session) {

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
			session = sessionService.getSession(request.getParameter(SESSION_COOKIE));
		} else if(request.getHeader(SESSION_COOKIE) != null) {
			session = sessionService.getSession((String)request.getHeader(SESSION_COOKIE));
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
		
		if(request.getCookies()!=null) {
			for (Cookie c : request.getCookies()) {
				if (c.getName().equals(SESSION_COOKIE)) {
					session = sessionService.getSession(c.getValue());
					if (session != null && sessionService.isLoggedOn(session, false)) {
						return session;
					}
				}
			}
		}
		
		throw new UnauthorizedException();
	}

	
	public Session verifySameSiteRequest(HttpServletRequest request, Session session) {
		
//		String requestToken = request.getHeader("X-Csrf-Token");
//		if(requestToken==null) {
//			requestToken = request.getParameter("token");
//			if(requestToken==null) {
//				log.warn(String.format("CSRF token missing from %s", request.getRemoteAddr()));
//				debugRequest(request);
//				return null;
//			}
//		}
//		
//		if(!session.getCsrfToken().equals(requestToken)) {
//			log.warn(String.format("CSRF token mistmatch from %s", request.getRemoteAddr()));
//			debugRequest(request);
//			return null;
//		}
		
		touchSession(request, Request.response(), session);

		return session;
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

	public boolean isValidCORSRequest(HttpServletRequest request, HttpServletResponse response, Properties properties) {
		
		String requestOrigin = request.getHeader("Origin");
		
		if(Objects.nonNull(requestOrigin)) {
			if(log.isDebugEnabled()) {
				log.debug("CORS request for origin {}", requestOrigin);
			}
		} else if(Objects.isNull(requestOrigin)) {
			return false;
		}
		
		boolean pathAllowsCORS = Boolean.parseBoolean(properties.getProperty("cors.enabled", "true"));
		if(!pathAllowsCORS) {
			if(log.isDebugEnabled()) {
				log.debug("Security properties of URI {} explicitly deny CORS", request.getRequestURI());
			}
			return false;
		}
		
		List<String> origins = new ArrayList<>();
		origins.addAll(Arrays.asList(properties.getProperty("cors.origins", "").split(",")));
		
		if(origins.size() > 0) {
			if(log.isDebugEnabled()) {
				log.debug("Security properties allows origins {}", Utils.csv(origins));
			}
		}
		
		if(ApplicationProperties.getValue("cors.enabled", false) && !Objects.isNull(requestOrigin)) {
			
			List<String> tmp =  new ArrayList<>();
			tmp.addAll(Arrays.asList(ApplicationProperties.getValue("cors.origins", "").split(",")));
			
			if(tmp.size() > 0) {
				if(log.isDebugEnabled()) {
					log.debug("Global configuration allows origins {}", Utils.csv(tmp));
				}
			}	
			
			origins.addAll(tmp);
		}

		if(origins.contains(requestOrigin)) {
			if(log.isDebugEnabled()) {
				log.debug("Origin {} allowed for URI {}", requestOrigin, request.getRequestURI());
			}
			/**
			 * Allow CORS to this realm. We must allow credentials as the
			 * API will be useless without them.
			 */
			response.addHeader("Access-Control-Allow-Credentials", "true");
			response.addHeader("Access-Control-Allow-Origin", requestOrigin);
			
			return true;
		}
		
		return false;
	}

	public void addSessionCookies(HttpServletRequest request,
			HttpServletResponse response, Session session) {

		if(Boolean.TRUE.equals(request.getAttribute("processedCookies"))) {
			return;
		}
		
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
		
		request.setAttribute("processedCookies", Boolean.TRUE);
	
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
			return getActiveSession(request)!=null;
		} catch (ObjectNotFoundException e) {
			return false;
		}
	}

	public User getCurrentUser(HttpServletRequest request) throws UnauthorizedException, SessionTimeoutException {
		return getSession(request).getUser();
	}

}
