package com.jadaptive.plugins.web.ui;

import java.io.FileNotFoundException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.jadaptive.api.ui.HtmlPage;
import com.jadaptive.api.ui.UriRedirect;
import com.jadaptive.plugins.web.wizard.WizardState;

public abstract class WizardPage extends HtmlPage {

	WizardState state;
	
	public WizardState getState() {
		return state;
	}

	@Override
	protected void processPost(String uri, HttpServletRequest request, HttpServletResponse response)
			throws FileNotFoundException {
		state.moveNext();
		throw new UriRedirect("/app/ui/wizard/setupWizard");
	}
}
