package com.jadaptive.api.ui.wizards;

import java.io.IOException;

import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.jadaptive.api.servlet.Request;
import com.jadaptive.api.ui.Html;
import com.jadaptive.api.ui.PageDependencies;
import com.jadaptive.api.ui.PageProcessors;
import com.jadaptive.api.ui.RequestPage;

@Component
@RequestPage(path = "wizard-complete/{resourceKey}")
@PageDependencies(extensions = { "jquery", "bootstrap", "fontawesome", "jadaptive-utils"} )
@PageProcessors(extensions = { "i18n"} )
public class WizardCompletePage extends WizardPage {
	
	@Autowired
	private WizardService wizardService;

	String resourceKey; 
	
	@Override
	public String getUri() {
		return "wizard-complete";
	}

	@Override
	protected void generateContent(Document document) throws IOException {
		
		super.generateContent(document);
		
		WizardState state = wizardService.getWizard(resourceKey).getState(Request.get());
		
		document.selectFirst("#completeHeader")
				.appendChild(Html.i("fa-solid fa-wand-magic"))
				.appendChild(Html.i18n(state.getBundle(), "wizard.complete.header"));

		document.selectFirst("#completeInfo").appendChild(Html.i18n(state.getBundle(), "wizard.complete.info"));
		
		wizardService.clearState(resourceKey, Request.get());
	}
	
	
	

}
