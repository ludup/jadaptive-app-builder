package com.jadaptive.app.json;

import java.io.IOException;
import java.util.Objects;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.jadaptive.api.app.SecurityPropertyService;
import com.jadaptive.api.auth.AuthenticationService;
import com.jadaptive.api.auth.AuthenticationService.LogonCompletedResult;
import com.jadaptive.api.json.RequestStatus;
import com.jadaptive.api.json.RequestStatusImpl;
import com.jadaptive.api.json.SessionStatus;
import com.jadaptive.api.permissions.AccessDeniedException;
import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.api.servlet.Request;
import com.jadaptive.api.session.Session;
import com.jadaptive.api.session.UnauthorizedException;
import com.jadaptive.api.tenant.TenantService;
import com.jadaptive.api.ui.PageCache;
import com.jadaptive.api.ui.PageRedirect;
import com.jadaptive.api.ui.pages.auth.OptionalAuthentication;
import com.jadaptive.app.session.SessionFilter;

@Controller
public class LogonController {

	static Logger log = LoggerFactory.getLogger(LogonController.class);
	
	@Autowired
	private TenantService tenantService; 
	
	@Autowired
	private PermissionService permissionService; 
	
	@Autowired
	private SecurityPropertyService securityService; 
	
	@Autowired
	private AuthenticationService authenticationService; 
	
	@Autowired
	private PageCache pageCache;
	
	@RequestMapping(value="/app/api/logon/basic", method = RequestMethod.POST, produces = {"application/json"})
	@ResponseBody
	@ResponseStatus(value=HttpStatus.OK)
	public SessionStatus logonUser(HttpServletRequest request, HttpServletResponse response, 
			@RequestParam String username, @RequestParam String password)  {

		permissionService.setupSystemContext();
		
		try {
			Properties properties = securityService.resolveSecurityProperties(request.getRequestURI());
			
			if("true".equalsIgnoreCase(properties.getProperty("logon.basic.disabled"))) {
				return new SessionStatus("Permission denied");
			}
			
			
			LogonCompletedResult result = authenticationService.logonUser(username, password,
					tenantService.getCurrentTenant(), 
					Request.getRemoteAddress(), 
					request.getHeader(HttpHeaders.USER_AGENT));
			
			Session.set(request, result);
			
			String homePage = (String) request.getSession().getAttribute(SessionFilter.PRE_LOGON_ORIGINAL_URL);
			if(Objects.isNull(homePage)) {
				homePage = properties.getProperty("authentication.homePage");
			}
			if(log.isInfoEnabled()) {
				log.info("Logged user {} on with home {}", username, StringUtils.defaultIfBlank(homePage, "default"));
			}
			return new SessionStatus(result.session().get(), homePage);
		} catch (Throwable e) {
			if(log.isErrorEnabled()) {
				log.error("POST api/logon/basic", e);
			}
			return new SessionStatus(e.getMessage());
		} finally {
			permissionService.clearUserContext();
		}
	}
	
	@RequestMapping(value="/app/api/logoff", method = RequestMethod.GET, produces = {"application/json"})
	@ResponseBody
	@ResponseStatus(value=HttpStatus.OK)
	public RequestStatus logoff(HttpServletRequest request, HttpServletResponse response)  {

		try {
			request.getSession().invalidate();
			return new RequestStatusImpl(true, "Session closed");
		} catch(UnauthorizedException e) {
			return new RequestStatusImpl(false, e.getMessage());
		}
	}
	
	@RequestMapping(value="/app/api/session/touch", method = RequestMethod.GET, produces = {"application/json"})
	@ResponseBody
	@ResponseStatus(value=HttpStatus.OK)
	public RequestStatus touchSession(HttpServletRequest request, HttpServletResponse response)  {

		var session = Session.getOr(request);
		if(session.isEmpty()) {
			return new RequestStatusImpl(false, "Session closed");
		}
		
		return new RequestStatusImpl(true, "");

	}
	
	@RequestMapping(value="/app/api/reset-login", method = { RequestMethod.GET }, produces = { "text/html"})
	public void startUserLogin(HttpServletRequest request, HttpServletResponse response) throws IOException, AccessDeniedException, UnauthorizedException {

		authenticationService.clearAuthenticationState();
		authenticationService.createAuthenticationState();
		
		throw new PageRedirect(pageCache.resolveDefault());
	}
	
	@RequestMapping(value="/app/api/change-auth", method = { RequestMethod.GET }, produces = { "text/html"})
	public void changeAuthenticator(HttpServletRequest request, HttpServletResponse response) throws IOException, AccessDeniedException, UnauthorizedException {

		authenticationService.getCurrentState().setSelectedPage(null);
		
		throw new PageRedirect(pageCache.resolvePage(OptionalAuthentication.class));
	}
	
}
