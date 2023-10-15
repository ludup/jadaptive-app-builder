package com.jadaptive.api.ui.wizards;

import java.io.FileNotFoundException;

import javax.servlet.http.HttpServletRequest;

import org.pf4j.ExtensionPoint;

import com.jadaptive.api.ui.Page;

public interface WizardFlow extends ExtensionPoint {

	String getResourceKey();

	default String getBundle() { return getResourceKey(); };
	
	WizardState getState(HttpServletRequest request);

	void clearState(HttpServletRequest request);
	
	Page getCompletePage() throws FileNotFoundException;

	void finish(WizardState wizardState);

	boolean requiresUserSession();

	
	
	
}
