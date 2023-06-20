package com.jadaptive.api.setup;

import java.io.IOException;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.jadaptive.api.ui.Page;
import com.jadaptive.api.ui.wizards.DefaultWizardSection;
import com.jadaptive.api.ui.wizards.WizardState;

public class SetupSection extends DefaultWizardSection {

	int weight;
	
	public SetupSection(String bundle, int weight) {
		super(bundle, weight);
	}
	
	public SetupSection(String bundle, String name, String resource, int weight) {
		super(bundle, name, resource, weight);
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
