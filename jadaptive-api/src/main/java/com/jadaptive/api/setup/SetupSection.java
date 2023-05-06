package com.jadaptive.api.setup;

import java.io.IOException;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.jadaptive.api.ui.Page;
import com.jadaptive.api.ui.wizards.DefaultWizardSection;
import com.jadaptive.api.ui.wizards.WizardSection;
import com.jadaptive.api.ui.wizards.WizardState;

public class SetupSection extends DefaultWizardSection {

	public SetupSection(String bundle) {
		super(bundle);
	}
	
	public SetupSection(String bundle, String name, String resource) {
		super(bundle, name, resource);
	}
	
	public boolean isSystem() {
		return false;
	}

	@Override
	public void processReview(Document document, WizardState state) {
			
	}
	
	@Override
	protected void processSection(Document document, Element element, Page page) throws IOException {
	}
	

}
