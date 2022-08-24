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

import com.codesmith.webbits.Request;
import com.codesmith.webbits.Response;
import com.jadaptive.api.app.ApplicationProperties;
import com.jadaptive.api.entity.ObjectNotFoundException;
import com.jadaptive.api.permissions.PermissionService;
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

	public Session getActiveSession(Request<?> request) {
		Session session = null;

		permissionService.setupSystemContext();

		try {
			if (request.hasParameter(SESSION_COOKIE)) {
				session = sessionService.getSession(request.parameterOrFail(SESSION_COOKIE));
			} else if (request.hasHeader(SESSION_COOKIE)) {
				session = sessionService.getSession(request.header(SESSION_COOKIE));
			}

			if (session != null && sessionService.isLoggedOn(session, true)) {
				return session;
			}

			if (request.attr(AUTHENTICATED_SESSION) != null) {
				session = request.attr(AUTHENTICATED_SESSION);
				if (sessionService.isLoggedOn(session, true)) {
					return session;
				}
			}

			if (request.session().httpSession().getAttribute(AUTHENTICATED_SESSION) != null) {
				session = request.session().attr(AUTHENTICATED_SESSION);
				if (sessionService.isLoggedOn(session, true)) {
					return session;
				}
			}

			String cookieValue = request.cookieValue(SESSION_COOKIE);
			if(cookieValue != null) {
				session = sessionService.getSession(cookieValue);
				if (session != null && sessionService.isLoggedOn(session, false)) {
					return session;
				}
			}

		} catch (ObjectNotFoundException e) {

		} finally {
			permissionService.clearUserContext();
		}

		return null;
	}

	@Deprecated
	public Session getActiveSession(HttpServletRequest request) {

		Session session = null;

		permissionService.setupSystemContext();

		try {
			if (request.getParameterMap().containsKey(SESSION_COOKIE)) {
				session = sessionService.getSession(request.getParameter(SESSION_COOKIE));
			} else if (request.getHeader(SESSION_COOKIE) != null) {
				session = sessionService.getSession((String) request.getHeader(SESSION_COOKIE));
			}

			if (session != null && sessionService.isLoggedOn(session, true)) {
				return session;
			}

			if (request.getAttribute(AUTHENTICATED_SESSION) != null) {
				session = (Session) request.getAttribute(AUTHENTICATED_SESSION);
				if (sessionService.isLoggedOn(session, true)) {
					return session;
				}
			}

			if (request.getSession().getAttribute(AUTHENTICATED_SESSION) != null) {
				session = (Session) request.getSession().getAttribute(AUTHENTICATED_SESSION);
				if (sessionService.isLoggedOn(session, true)) {
					return session;
				}
			}

			if (Objects.nonNull(request.getCookies())) {
				for (Cookie c : request.getCookies()) {
					if (c.getName().equals(SESSION_COOKIE)) {
						session = sessionService.getSession(c.getValue());
						if (session != null && sessionService.isLoggedOn(session, true)) {
							return session;
						}
					}
				}
			}

		} catch (ObjectNotFoundException e) {

		} finally {
			permissionService.clearUserContext();
		}

		return null;
	}

	public Session touchSession(Request<?> request, Response<?> response)
			throws UnauthorizedException, SessionTimeoutException {

		Session session = null;

		if (request.underlyingRequest().getSession().getAttribute(AUTHENTICATED_SESSION) == null) {
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
			session = request.session().attr(AUTHENTICATED_SESSION);
			if (!sessionService.isLoggedOn(session, true)) {
				throw new UnauthorizedException();
			}
		}

		// Preserve the session for future lookups in this request and session
		request.attr(AUTHENTICATED_SESSION, session);
		request.session().attr(AUTHENTICATED_SESSION, session);

		addSessionCookies(request, response, session);

		return session;

	}

	public Session getSession(Request<?> request) throws UnauthorizedException, SessionTimeoutException {

		/**
		 * This method SHOULD NOT touch the session.
		 */
		Session session = null;

		if (request.hasParameter(SESSION_COOKIE)) {
			session = sessionService.getSession(request.parameterOrFail(SESSION_COOKIE));
		} else if (request.header(SESSION_COOKIE) != null) {
			session = sessionService.getSession(request.header(SESSION_COOKIE));
		}

		if (session != null && sessionService.isLoggedOn(session, false)) {
			return session;
		}

		if (request.hasAttr(AUTHENTICATED_SESSION)) {
			session = request.attr(AUTHENTICATED_SESSION);
			if (sessionService.isLoggedOn(session, false)) {
				return session;
			}
		}

		if (request.session().hasHttpSessionAttr(AUTHENTICATED_SESSION)) {
			session = request.session().attr(AUTHENTICATED_SESSION);
			if (sessionService.isLoggedOn(session, false)) {
				return session;
			}
		}

		String cookieValue = request.cookieValue(SESSION_COOKIE);
		if(cookieValue != null) {
			session = sessionService.getSession(cookieValue);
			if (session != null && sessionService.isLoggedOn(session, false)) {
				return session;
			}
		}

		throw new UnauthorizedException();
	}

	@Deprecated
	public Session getSession(HttpServletRequest request) throws UnauthorizedException, SessionTimeoutException {

		/**
		 * This method SHOULD NOT touch the session.
		 */
		Session session = null;

		if (request.getParameterMap().containsKey(SESSION_COOKIE)) {
			session = sessionService.getSession(request.getParameter(SESSION_COOKIE));
		} else if (request.getHeader(SESSION_COOKIE) != null) {
			session = sessionService.getSession((String) request.getHeader(SESSION_COOKIE));
		}

		if (session != null && sessionService.isLoggedOn(session, false)) {
			return session;
		}

		if (request.getAttribute(AUTHENTICATED_SESSION) != null) {
			session = (Session) request.getAttribute(AUTHENTICATED_SESSION);
			if (sessionService.isLoggedOn(session, false)) {
				return session;
			}
		}

		if (request.getSession().getAttribute(AUTHENTICATED_SESSION) != null) {
			session = (Session) request.getSession().getAttribute(AUTHENTICATED_SESSION);
			if (sessionService.isLoggedOn(session, false)) {
				return session;
			}
		}

		if (request.getCookies() != null) {
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

	public void verifySameSiteRequest(HttpServletRequest request, HttpServletResponse response, Session session)
			throws UnauthorizedException {

		String requestToken = request.getHeader("X-Csrf-Token");
		if (requestToken == null) {
			requestToken = request.getParameter("token");
			if (requestToken == null) {
				log.warn(String.format("CSRF token missing from %s", request.getRemoteAddr()));
				debugRequest(request);
				throw new UnauthorizedException();
			}
		}

		if (!session.getCsrfToken().equals(requestToken)) {
			log.warn(String.format("CSRF token mistmatch from %s", request.getRemoteAddr()));
			debugRequest(request);
			throw new UnauthorizedException();
		}

	}

	protected void debugRequest(HttpServletRequest request) {
		if (debugCSRF) {
			log.warn(String.format("The request URI was %s, and contained the following parameters :-",
					request.getRequestURI()));
			for (Map.Entry<String, String[]> en : request.getParameterMap().entrySet()) {
				log.warn(String.format("  %s = %s", en.getKey(), String.join(",", en.getValue())));
			}
			log.warn("And the following headers :-");
			for (Enumeration<String> hdrEnum = request.getHeaderNames(); hdrEnum.hasMoreElements();) {
				String hdr = hdrEnum.nextElement();
				log.warn(String.format("  %s = %s", hdr, request.getHeader(hdr)));
			}
		}
	}

	public boolean isValidCORSRequest(HttpServletRequest request, HttpServletResponse response, Properties properties) {

		String requestOrigin = request.getHeader("Origin");

		if (Objects.nonNull(requestOrigin) && log.isInfoEnabled()) {
			log.info("CORS request for origin {}", requestOrigin);
		} else if (Objects.isNull(requestOrigin)) {
			return false;
		}

		boolean pathAllowsCORS = Boolean.parseBoolean(properties.getProperty("cors.enabled", "true"));
		if (!pathAllowsCORS) {
			if (log.isInfoEnabled()) {
				log.info("Security properties of URI {} explicitly deny CORS", request.getRequestURI());
			}
			return false;
		}

		List<String> origins = new ArrayList<>();
		origins.addAll(Arrays.asList(properties.getProperty("cors.origins", "").split(",")));

		if (origins.size() > 0 && log.isInfoEnabled()) {
			log.info("Security properties allows origins {}", Utils.csv(origins));
		}

		if (ApplicationProperties.getValue("cors.enabled", false) && !Objects.isNull(requestOrigin)) {

			List<String> tmp = new ArrayList<>();
			tmp.addAll(Arrays.asList(ApplicationProperties.getValue("cors.origins", "").split(",")));

			if (tmp.size() > 0 && log.isInfoEnabled()) {
				log.info("Global configuration allows origins {}", Utils.csv(tmp));
			}

			origins.addAll(tmp);
		}

		if (origins.contains(requestOrigin)) {
			if (log.isInfoEnabled()) {
				log.info("Origin {} allowed for URI {}", requestOrigin, request.getRequestURI());
			}
			/**
			 * Allow CORS to this realm. We must allow credentials as the API will be
			 * useless without them.
			 */
			response.addHeader("Access-Control-Allow-Credentials", "true");
			response.addHeader("Access-Control-Allow-Origin", requestOrigin);

			return true;
		}

		return false;
	}

	public void addSessionCookies(Request<?> request, Response<?> response, Session session) {

		Cookie cookie = response.cookie(SESSION_COOKIE, session.getUuid(),
				(session.getSessionTimeout() > 0 ? 60 * session.getSessionTimeout() : Integer.MAX_VALUE));
		cookie.setHttpOnly(true);
		cookie.setPath("/");

		cookie = response.cookie(CSRF_TOKEN, session.getCsrfToken(),
				(session.getSessionTimeout() > 0 ? 60 * session.getSessionTimeout() : Integer.MAX_VALUE));
		cookie.setPath("/");

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
			return new Locale((String) request.getSession().getAttribute(USER_LOCALE));
		}

	}

	public void setLocale(HttpServletRequest request, HttpServletResponse response, String locale) {

		request.getSession().setAttribute(USER_LOCALE, locale);

		Cookie cookie = new Cookie(LOCALE_COOKIE, locale);
		cookie.setMaxAge(Integer.MAX_VALUE);
		cookie.setPath("/");
		if (request.getProtocol().equalsIgnoreCase("https")) {
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

	public boolean hasActiveSession(Request<?> request) {
		try {
			return getSession(request) != null;
		} catch (ObjectNotFoundException | UnauthorizedException | SessionTimeoutException e) {
			return false;
		}
	}

}
