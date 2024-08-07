package com.jadaptive.api.ui.pages.auth;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

import javax.servlet.http.Cookie;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.jadaptive.api.auth.AuthenticationModule;
import com.jadaptive.api.auth.AuthenticationService;
import com.jadaptive.api.auth.AuthenticationState;
import com.jadaptive.api.auth.PostAuthenticatorPage;
import com.jadaptive.api.db.TenantAwareObjectDatabase;
import com.jadaptive.api.servlet.Request;
import com.jadaptive.api.ui.AuthenticationPage;
import com.jadaptive.api.ui.Html;
import com.jadaptive.api.ui.HtmlPage;
import com.jadaptive.api.ui.Page;
import com.jadaptive.api.ui.PageCache;
import com.jadaptive.api.ui.PageDependencies;
import com.jadaptive.api.ui.PageProcessors;
import com.jadaptive.api.ui.PageRedirect;

@Component
@PageDependencies(extensions = { "jquery", "bootstrap", "fontawesome", "jadaptive-utils"} )
@PageProcessors(extensions = { "i18n"} )
public class BackupAuthenticationPage extends HtmlPage implements PostAuthenticatorPage {

	@Autowired
	private PageCache pageCache;
	
	@Autowired
	private AuthenticationService authenticationService; 
	
	@Autowired
	private TenantAwareObjectDatabase<AuthenticationModule> moduleDatabase;
	
	
	@Override
	public String getUri() {
		return "2fa-backup";
	}

	@Override
	public boolean requiresProcessing(AuthenticationState state) {
		
		try {
			if(state.getOptionalRequired() == 0) {
				return false;
			}
			var pages = new ArrayList<AuthenticationPage<?>>();
			int modulesWithCredentials = 0;
			for(AuthenticationModule m : state.getOptionalAuthentications()) {
				Class<? extends Page> pageClass = authenticationService.getAuthenticationPage(m.getAuthenticatorKey());
				AuthenticationPage<?> page = (AuthenticationPage<?>)pageCache.resolvePage(pageClass);
				if(!page.canAuthenticate(state)) {
					pages.add(page);
				} else {
					modulesWithCredentials++;
				}
			}
			
			int requiredModules = Math.min(state.getPolicy().getOptionalRequired() + state.getPolicy().getBackupRequired(), state.getOptionalAuthentications().size());
			return requiredModules - modulesWithCredentials > 0;
		} catch(FileNotFoundException e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
	}

	
	@Override
	protected void generateContent(Document doc) throws IOException {
		
		
		var state = authenticationService.getCurrentState();
		
		if(!state.isRequiredAuthenticationComplete()) {
			throw new PageRedirect(pageCache.resolvePage(Login.class));
		}
		
		if(!state.isOptionalComplete()) {
			throw new PageRedirect(pageCache.resolvePage(OptionalAuthentication.class));
		}
		
		var pages = new ArrayList<AuthenticationPage<?>>();
		int modulesWithCredentials = 0;
		for(AuthenticationModule m : state.getOptionalAuthentications()) {
			Class<? extends Page> pageClass = authenticationService.getAuthenticationPage(m.getAuthenticatorKey());
			AuthenticationPage<?> page = (AuthenticationPage<?>)pageCache.resolvePage(pageClass);
			if(!page.canAuthenticate(state)) {
				pages.add(page);
			} else {
				modulesWithCredentials++;
			}
		}

		if(state.getOptionalCompleted() > 0) {
			
			int requiredModules = Math.min(state.getPolicy().getOptionalRequired() + state.getPolicy().getBackupRequired(), state.getOptionalAuthentications().size());
			
			int remaining = requiredModules - modulesWithCredentials;
			
			doc.selectFirst("#message")
					.attr("arg0", String.valueOf(remaining));
			if(remaining > 1) {
				doc.selectFirst("#small")
						.attr("jad:i18n", "backupToContinue.more")
						.attr("jad:arg0", String.valueOf(remaining));
			} else {
				doc.selectFirst("#small")
					.attr("jad:i18n", "backupToContinue.last");
			}
		}

		Element authenticators = doc.selectFirst("#authenticators");
		for(AuthenticationPage<?> page : pages) {
			
				authenticators.appendChild(Html.div("card my-3")
					.appendChild(Html.div("card-body")
						.appendChild(new Element("h6")
								.addClass("card-title mb-1")
								.appendChild(Html.i(page.getIconGroup(), page.getIcon(), "me-2"))
								.appendChild(Html.i18n(page.getBundle(), "verifyIdentity.title")))
						.appendChild(new Element("span")
								.addClass("card-text")
								.appendChild(Html.i18n(page.getBundle(), "verifyIdentity.body")
										.addClass("small")))
						.appendChild(Html.a("#").addClass("select stretched-link float-end")
								.attr("data-authenticator", page.getAuthenticatorUUID()))
				));

		}
		super.generateContent(doc);
	}

	
}
