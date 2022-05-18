package com.jadaptive.api.setup;

import org.jsoup.nodes.Document;

import com.jadaptive.api.wizards.WizardState;

public abstract class SetupSection extends WizardSection {

	public SetupSection(String bundle, String name, String resource) {
		super(bundle, name, resource);
	}
	
	public boolean isSystem() {
		return false;
	}

	@Override
	public void processReview(Document document, WizardState state) {
			
	}
	
	public void finish(WizardState state) {
		
	}
}
