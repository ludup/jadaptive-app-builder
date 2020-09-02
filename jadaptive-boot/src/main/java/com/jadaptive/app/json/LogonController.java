package com.jadaptive.app.json;

import java.util.Objects;
import java.util.Properties;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.RequestParam;

import com.codesmith.webbits.AppSession;
import com.codesmith.webbits.Content;
import com.codesmith.webbits.HTTPMethod;
import com.codesmith.webbits.Out;
import com.codesmith.webbits.Request;
import com.codesmith.webbits.Response;
import com.codesmith.webbits.View;
import com.codesmith.webbits.api.Api;
import com.jadaptive.api.app.SecurityPropertyService;
import com.jadaptive.api.auth.AuthenticationService;
import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.api.session.Session;
import com.jadaptive.api.session.SessionService;
import com.jadaptive.api.session.SessionTimeoutException;
import com.jadaptive.api.session.SessionUtils;
import com.jadaptive.api.session.UnauthorizedException;
import com.jadaptive.api.tenant.TenantService;
import com.jadaptive.app.session.SessionFilter;

@Api
@View(contentType = "application/json", paths = "/app/api")
public class LogonController {

	static Logger log = LoggerFactory.getLogger(LogonController.class);
	
	@Autowired
	private TenantService tenantService; 
	
	@Autowired
	private SessionUtils sessionUtils;
	
	@Autowired
	private PermissionService permissionService; 
	
	@Autowired
	private SecurityPropertyService securityService; 
	
	@Autowired
	private AuthenticationService authenticationService; 
	
	@Autowired
	private SessionService sessionService; 
	
	@View(paths = "logon/basic", contentType = "application/json")
	@Out(methods = HTTPMethod.POST)
	@Content(statusCode = HttpServletResponse.SC_OK)
	public SessionStatus logonUser(Request<?> request, Response<?> response, 
			@RequestParam String username, @RequestParam String password)  {

		permissionService.setupSystemContext();
		
		try {
			Properties properties = securityService.resolveSecurityProperties(request.underlyingRequest().getRequestURI());
			
			if("true".equalsIgnoreCase(properties.getProperty("logon.basic.disabled"))) {
				return new SessionStatus("Permission denied");
			}
			
			Session session = authenticationService.logonUser(username, password,
					tenantService.getCurrentTenant(), 
					request.underlyingRequest().getRemoteAddr(), 
					request.underlyingRequest().getHeader(HttpHeaders.USER_AGENT));
			
			sessionUtils.addSessionCookies(request, response, session);
			
			String homePage = AppSession.get().attr(SessionFilter.PRE_LOGON_ORIGINAL_URL);
			if(Objects.isNull(homePage)) {
				homePage = properties.getProperty("authentication.homePage");
			}
			if(log.isInfoEnabled()) {
				log.info("Logged user {} on with home {}", username, StringUtils.defaultIfBlank(homePage, "default"));
			}
			return new SessionStatus(session, homePage);
		} catch (Throwable e) {
			if(log.isErrorEnabled()) {
				log.error("POST api/logon/basic", e);
			}
			return new SessionStatus(e.getMessage());
		} finally {
			permissionService.clearUserContext();
		}
	}
	
	@View(paths = "logoff", contentType = "application/json")
	@Out(methods = HTTPMethod.GET)
	@Content(statusCode = HttpServletResponse.SC_OK)
	public RequestStatus logoff(Request<?> request, Response<?> response)  {

		try {
			Session session = sessionUtils.getSession(request);
			if(session.isClosed()) {
				return new RequestStatus(false, "Session already closed");
			}
			
			sessionService.closeSession(session);
			
			return new RequestStatus(true, "Session closed");
		} catch(UnauthorizedException | SessionTimeoutException e) {
			return new RequestStatus(false, e.getMessage());
		}
	}
	
	@View(paths = "session/touch", contentType = "application/json")
	@Out(methods = HTTPMethod.GET)
	@Content(statusCode = HttpServletResponse.SC_OK)
	public RequestStatus touchSession(Request<?> request, Response<?> response)  {

		try {
			Session session = sessionUtils.getSession(request);
			if(!sessionService.isLoggedOn(session, true)) {
				return new RequestStatus(false, "Session closed");
			}
			
			return new RequestStatus(true, "");
		} catch(UnauthorizedException | SessionTimeoutException e) {
			return new RequestStatus(false, e.getMessage());
		}
	}
}
