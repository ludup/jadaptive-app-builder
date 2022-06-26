package com.jadaptive.api.ui.wizards;

import java.io.FileNotFoundException;

import javax.servlet.http.HttpServletRequest;

import org.pf4j.ExtensionPoint;

import com.jadaptive.api.ui.Page;

public interface WizardFlow extends ExtensionPoint {

	String getResourceKey();

	WizardState getState(HttpServletRequest request) throws FileNotFoundException;

	void clearState(HttpServletRequest request);
	
	Page getCompletePage() throws FileNotFoundException;

	void finish(WizardState wizardState);
	
	
}
