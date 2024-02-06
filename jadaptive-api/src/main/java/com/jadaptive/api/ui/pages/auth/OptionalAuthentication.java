package com.jadaptive.api.ui.pages.auth;

import java.io.FileNotFoundException;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.jadaptive.api.auth.AuthenticationModule;
import com.jadaptive.api.auth.AuthenticationService;
import com.jadaptive.api.auth.AuthenticationState;
import com.jadaptive.api.db.TenantAwareObjectDatabase;
import com.jadaptive.api.permissions.AccessDeniedException;
import com.jadaptive.api.ui.AuthenticationPage;
import com.jadaptive.api.ui.Html;
import com.jadaptive.api.ui.Page;
import com.jadaptive.api.ui.PageCache;
import com.jadaptive.api.ui.PageDependencies;
import com.jadaptive.api.ui.PageProcessors;
import com.jadaptive.api.ui.PageRedirect;
import com.jadaptive.api.ui.pages.auth.OptionalAuthentication.OptionalAuthenticationForm;

@Component
@PageDependencies(extensions = { "jquery", "bootstrap", "fontawesome", "jadaptive-utils"} )
@PageProcessors(extensions = { "i18n"} )
public class OptionalAuthentication extends AuthenticationPage<OptionalAuthenticationForm> {

	@Autowired
	private PageCache pageCache;
	
	@Autowired
	private AuthenticationService authenticationService; 
	
	@Autowired
	private TenantAwareObjectDatabase<AuthenticationModule> moduleDatabase;
	
	public OptionalAuthentication() {
		super(OptionalAuthenticationForm.class);
	}

	public interface OptionalAuthenticationForm {
		String getAuthenticator();
	}

	@Override
	public String getUri() {
		return "select2fa";
	}

	@Override
	protected void doGenerateContent(Document doc) throws FileNotFoundException {
		
		
		var state = authenticationService.getCurrentState();
		
		if(!state.isAuthenticationComplete()) {
			throw new PageRedirect(pageCache.resolvePage(Login.class));
		}
		
		if(state.getOptionalRequired()==1 && state.getOptionalAuthentications().size()==1) {
			state.setSelectedPage(authenticationService.getAuthenticationPage(state.getOptionalAuthentications().iterator().next().getAuthenticatorKey()));
			throw new PageRedirect(pageCache.resolvePage(state.getCurrentPage()));
		}
		
		if(state.getOptionalCompleted() > 0) {
			int remaining = state.getOptionalRequired() - state.getOptionalCompleted();
			doc.selectFirst("#message")
					.attr("jad:i18n", "verifyIdentity.more");
			if(remaining > 1) {
				doc.selectFirst("#small")
						.attr("jad:i18n", "selectToContinue.more")
						.attr("jad:arg0", String.valueOf(remaining));
			} else {
				doc.selectFirst("#small")
					.attr("jad:i18n", "selectToContinue.last");
			}
		}
		
		Element authenticators = doc.selectFirst("#authenticators");
		for(AuthenticationModule module : state.getOptionalAuthentications()) {
			
			Class<? extends Page> pageClass = authenticationService.getAuthenticationPage(module.getAuthenticatorKey());
			AuthenticationPage<?> page = (AuthenticationPage<?>)pageCache.resolvePage(pageClass);
			
			if(!state.hasCompleted(pageClass)) {
				authenticators.appendChild(Html.div("card my-3")
					.appendChild(Html.div("card-body")
						.appendChild(new Element("h6")
								.addClass("card-title mb-1")
								.appendChild(Html.i(page.getIconGroup(), page.getIcon(), "me-2"))
								.appendChild(Html.i18n(page.getBundle(), "verifyIdentity.title")))
//						.appendChild(new Element("span")
//								.addClass("card-text")
//								.appendChild(Html.i18n(page.getBundle(), "verifyIdentity.body")
//										.addClass("small")))
						.appendChild(Html.a("#").addClass("select stretched-link float-end")
								.attr("data-authenticator", module.getUuid()))));
			}

		}
		super.doGenerateContent(doc);
	}

	@Override
	protected boolean isAllowFormExternalRedirect() {
		return true;
	}

	@Override
	protected boolean doForm(Document document, AuthenticationState state, OptionalAuthenticationForm form)
			throws AccessDeniedException, FileNotFoundException {

		if(!state.isAuthenticationComplete()) {
			throw new PageRedirect(pageCache.resolvePage(Login.class));
		}
		
		AuthenticationModule module = moduleDatabase.get(form.getAuthenticator(), AuthenticationModule.class);
		state.setSelectedPage(authenticationService.getAuthenticationPage(module.getAuthenticatorKey()));
		throw new PageRedirect(pageCache.resolvePage(state.getCurrentPage()));
		
	}

	@Override
	public String getBundle() {
		return "userInterface";
	}
}
