package com.jadaptive.api.ui.pages.auth;

import java.io.FileNotFoundException;
import java.util.Objects;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.jadaptive.api.auth.AuthenticationModule;
import com.jadaptive.api.auth.AuthenticationPolicy;
import com.jadaptive.api.auth.AuthenticationPolicyService;
import com.jadaptive.api.auth.AuthenticationService;
import com.jadaptive.api.auth.AuthenticationState;
import com.jadaptive.api.db.TenantAwareObjectDatabase;
import com.jadaptive.api.entity.ObjectNotFoundException;
import com.jadaptive.api.permissions.AccessDeniedException;
import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.api.servlet.Request;
import com.jadaptive.api.tenant.TenantService;
import com.jadaptive.api.ui.AuthenticationPage;
import com.jadaptive.api.ui.Feedback;
import com.jadaptive.api.ui.Html;
import com.jadaptive.api.ui.PageCache;
import com.jadaptive.api.ui.PageDependencies;
import com.jadaptive.api.ui.PageProcessors;
import com.jadaptive.api.ui.PageRedirect;
import com.jadaptive.api.ui.UriRedirect;
import com.jadaptive.api.ui.pages.auth.Login.LoginForm;
import com.jadaptive.api.user.FakeUser;
import com.jadaptive.api.user.User;

@Component
@PageDependencies(extensions = { "jquery", "bootstrap", "fontawesome", "jadaptive-utils"} )
@PageProcessors(extensions = { "i18n"} )
public class Login extends AuthenticationPage<LoginForm> {

	static Logger log = LoggerFactory.getLogger(Login.class);
	
	@Autowired
	private TenantService tenantService; 
	
	@Autowired
	private AuthenticationPolicyService policyService;
	
	@Autowired
	private PermissionService permissionService;;

	@Autowired
	private PageCache pageCache;
	
	@Autowired
	private  TenantAwareObjectDatabase<AuthenticationModule> moduleDatabase;
	
	public Login() {
		super(LoginForm.class);
	}
	
	@Override
	protected void beforeProcess(String uri, HttpServletRequest request, HttpServletResponse response)
			throws FileNotFoundException {
		if(tenantService.isSetupMode()) {
			throw new UriRedirect("/app/ui/wizards/setup");
		}
	}

	@Override
	protected void doGenerateContent(Document doc) throws FileNotFoundException {
		
		AuthenticationState state = authenticationService.getCurrentState();
		if(state.hasFinished()) {
			authenticationService.clearAuthenticationState();
		}
		state = authenticationService.getCurrentState();
		if(!state.getCurrentPage().equals(Login.class)) {
			throw new PageRedirect(pageCache.resolvePage(state.getCurrentPage()));
		}
		
		doc.selectFirst("#authenticationHeader").appendChild(Html.i18n(state.getPolicy().getBundle(), String.format("%s.name", state.getPolicy().getResourceKey())));
		
		if(!state.getPolicy().getPasswordOnFirstPage()) {
			doc.selectFirst("#passwordDiv").remove();
		}
	}
	
	@Override
	public String getBundle() {
		return "userInterface";
	}
	
	protected boolean doForm(Document document, AuthenticationState state, LoginForm form) {
		
		try {
			
			authenticationService.assertLoginThesholds(form.getUsername(), state.getRemoteAddress());
			
			state.setAttemptedUsername(form.getUsername());
			User user; 
			try {
				user = userService.getUser(form.getUsername());
				
			} catch(ObjectNotFoundException e) {
				user = new FakeUser(form.getUsername());
			}
			state.setUser(user);
			boolean passwordRequired = state.getPolicy().getPasswordOnFirstPage() && state.getPolicy().getPasswordRequired();
			boolean passwordVerified = false;
			
			if(StringUtils.isNotBlank(Request.get().getParameter("password"))) {
				if(userService.verifyPassword(state.getUser(), Request.get().getParameter("password").toCharArray())) {
					state.setAttribute(AuthenticationService.PASSWORD, Request.get().getParameter("password"));
					passwordVerified = true;
				} 
			}
			
			AuthenticationPolicy assigned = policyService.getAssignedPolicy(user, 
					Request.getRemoteAddress(), 
					state.getPolicy().getClass());
			
			if(Objects.isNull(assigned)) {
				assigned = state.getPolicy();
			}
			
			authenticationService.changePolicy(state, assigned, passwordVerified);
			
			if(passwordVerified || !passwordRequired) {
				return true;
			}

    	} catch(AccessDeniedException e) {
    		throw e;
    	} catch(ObjectNotFoundException e) {
    		log.warn("{} not found", e);
    		throw e;
    	} catch(Throwable e) {
    		log.error("Error in login", e);
    	}
    	
    	Request.response().setStatus(HttpStatus.FORBIDDEN.value());
    	Feedback.error("default", "error.invalidCredentials");

		return false;
	}
	
	@Override
	public String getUri() {
		return "login";
	}
	
	public interface LoginForm {
		String getUsername();
	}
}
