package com.jadaptive.api.ui.wizards;

import java.io.FileNotFoundException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jsoup.nodes.Document;

import com.jadaptive.api.ui.HtmlPage;
import com.jadaptive.api.ui.UriRedirect;

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
