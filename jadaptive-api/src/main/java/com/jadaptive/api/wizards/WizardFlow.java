package com.jadaptive.api.wizards;

import java.io.FileNotFoundException;

import javax.servlet.http.HttpServletRequest;

import org.jsoup.nodes.Document;
import org.pf4j.ExtensionPoint;

public interface WizardFlow extends ExtensionPoint {

	String getResourceKey();

	WizardState getState(HttpServletRequest request) throws FileNotFoundException;

	void processReview(Document document, WizardState state);
	
	
}
