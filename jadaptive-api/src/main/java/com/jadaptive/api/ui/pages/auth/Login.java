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
import com.jadaptive.api.ui.PageDependencies;
import com.jadaptive.api.ui.PageProcessors;
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
		if(!state.getCurrentPage().equals(Login.class)) {
			authenticationService.clearAuthenticationState();
		}
		
		doc.selectFirst("#authenticationHeader").appendChild(Html.i18n(state.getPolicy().getBundle(), String.format("%s.name", state.getPolicy().getResourceKey())));
		
		if(!state.getPolicy().getPasswordOnFirstPage()) {
			doc.selectFirst("#passwordDiv").remove();
		}
		
		/**
		 * TODO: This needs to be HtmlExtender in password reset plugin
		 */
//		if(policyService.hasPasswordResetPolicy()
//				&& state.getScope()==AuthenticationScope.USER_LOGIN) {
//			doc.selectFirst("#actions")
//				.after(Html.a("/start-password-reset")
//						.addClass("text-decoration-none")
//						.appendChild(new Element("sup")
//								.appendChild(Html.i18n(AuthenticationPolicy.RESOURCE_KEY, "forgotPassword.text"))));
//		}
//		
//		if(state.getScope()==AuthenticationScope.PASSWORD_RESET) {
//			doc.selectFirst("#policyMessage").appendChild(Html.i18n(AuthenticationPolicy.RESOURCE_KEY, "forgotPassword.desc"));
//			doc.selectFirst("#policyDiv").removeClass("d-none");
//			
//			doc.selectFirst("#buttonName")
//				.attr("jad:bundle", "default")
//				.attr("jad:i18n", "start.name");
//		}
		
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
			AuthenticationPolicy assigned = policyService.getAssignedPolicy(user, Request.getRemoteAddress(), state.getPolicy().getClass());
			
			if(Objects.nonNull(assigned)) {
				authenticationService.processRequiredAuthentication(state, assigned);
				
				if(StringUtils.isNotBlank(Request.get().getParameter("password"))) {
					if(userService.verifyPassword(state.getUser(), Request.get().getParameter("password").toCharArray())) {
						
						permissionService.setupUserContext(user);
						
						try {
							
							state.setAttribute(AuthenticationService.PASSWORD, Request.get().getParameter("password"));
							state.getRequiredPages().remove(Password.class);
							AuthenticationModule passwordModule = moduleDatabase.get(
									AuthenticationService.PASSWORD_MODULE_UUID,
									AuthenticationModule.class);
							if(state.getOptionalAuthentications().contains(passwordModule)) {
								state.getOptionalAuthentications().remove(passwordModule);
								state.setOptionalCompleted(1);
							}
							return true;
						
						} finally {
							permissionService.clearUserContext();
						}
					} else {
				    	return false;
					}
				}
				
				
			
				return true;
			}
			
			if(log.isWarnEnabled()) {
				log.warn("A suitable policy could not be found for {}", user.getUsername());
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
