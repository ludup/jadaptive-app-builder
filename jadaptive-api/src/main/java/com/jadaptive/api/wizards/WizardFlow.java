package com.jadaptive.api.wizards;

import java.io.FileNotFoundException;

import javax.servlet.http.HttpServletRequest;

import org.jsoup.nodes.Document;
import org.pf4j.ExtensionPoint;

import com.jadaptive.api.ui.Page;

public interface WizardFlow extends ExtensionPoint {

	String getResourceKey();

	WizardState getState(HttpServletRequest request) throws FileNotFoundException;

	void processReview(Document document, WizardState state);

	Page getCompletePage() throws FileNotFoundException;

	void finish();

	void clearState(HttpServletRequest request);
	
	
}