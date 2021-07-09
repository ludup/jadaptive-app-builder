package com.jadaptive.plugins.web.ui.setup;

import java.io.IOException;

import org.jsoup.nodes.Document;
import org.pf4j.Extension;
import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.servlet.Request;
import com.jadaptive.api.ui.HtmlPage;
import com.jadaptive.api.ui.PageDependencies;
import com.jadaptive.api.ui.PageProcessors;
import com.jadaptive.api.ui.RequestPage;
import com.jadaptive.api.wizards.WizardService;
import com.jadaptive.api.wizards.WizardState;

@Extension
@RequestPage(path="setup-complete")
@PageDependencies(extensions = { "jquery", "bootstrap", "fontawesome", "jadaptive-utils"} )
@PageProcessors(extensions = { "i18n"} )
public class SetupComplete extends HtmlPage {

	@Autowired
	private WizardService wizardService; 
	
	@Override
	public String getUri() {
		return "setup-complete";
	}

	@Override
	protected void generateContent(Document document) throws IOException {
		
		WizardState state = wizardService.getWizard(SetupWizard.RESOURCE_KEY).getState(Request.get());
		
		if(!state.isFinished()) {
			throw new IllegalStateException("Incomplete setup wizard!");
		}

		wizardService.getWizard(SetupWizard.RESOURCE_KEY).clearState(Request.get());
		
		super.generateContent(document);
	}
}
