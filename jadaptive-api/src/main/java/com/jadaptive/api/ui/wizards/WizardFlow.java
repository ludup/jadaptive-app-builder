package com.jadaptive.api.ui.wizards;

import java.io.FileNotFoundException;

import org.pf4j.ExtensionPoint;

import com.jadaptive.api.ui.Page;
import com.jadaptive.api.ui.Redirect;
import com.jadaptive.api.ui.UriRedirect;

import jakarta.servlet.http.HttpServletRequest;

public interface WizardFlow extends ExtensionPoint {

	String getResourceKey();

	default String getBundle() { return getResourceKey(); };
	
	WizardState getState(HttpServletRequest request);

	void clearState(HttpServletRequest request);
	
	Page getCompletePage() throws FileNotFoundException;

	void finish(WizardState wizardState);

	boolean requiresUserSession();

	WizardState generateState(HttpServletRequest request, String uuid);

	default Redirect getCancelRedirect() { throw new UriRedirect("/app/ui"); }
}
