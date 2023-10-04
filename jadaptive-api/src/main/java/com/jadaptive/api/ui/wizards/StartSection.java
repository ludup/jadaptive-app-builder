package com.jadaptive.api.ui.wizards;

import java.io.IOException;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.jadaptive.api.ui.Html;
import com.jadaptive.api.ui.Page;

public class StartSection extends DefaultWizardSection {

	public StartSection(String bundle, String name) {
		super(bundle, name, 0);	
	}

	@Override
	protected void processSection(Document document, Element element, Page page) throws IOException {
		
		document.selectFirst("title").appendChild(Html.i18n(bundle, "wizard.name"));
		document.selectFirst("#startHeader").appendChild(Html.i18n(bundle, "wizard.name"));
		document.selectFirst("#startInfo").appendChild(Html.i18n(bundle, "wizard.desc"));
		
	}

}
