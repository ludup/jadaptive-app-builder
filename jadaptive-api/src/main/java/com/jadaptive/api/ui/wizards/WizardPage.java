package com.jadaptive.api.ui.wizards;

import java.io.FileNotFoundException;

import org.jsoup.nodes.Document;

import com.jadaptive.api.ui.HtmlPage;
import com.jadaptive.api.ui.UriRedirect;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public abstract class WizardPage extends HtmlPage {

	WizardState state;
	
	public WizardState getState() {
		return state;
	}

	@Override
	protected void processPost(Document document, String uri, HttpServletRequest request, HttpServletResponse response)
			throws FileNotFoundException {
		state.moveNext();
		throw new UriRedirect("/app/ui/wizard/setupWizard");
	}
}
