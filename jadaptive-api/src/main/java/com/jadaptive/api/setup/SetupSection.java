package com.jadaptive.api.setup;

import org.jsoup.nodes.Document;

import com.jadaptive.api.wizards.WizardState;

public class SetupSection extends WizardSection {

	public SetupSection(String bundle, String name, String resource, Integer position) {
		super(bundle, name, resource, position);
	}

	@Override
	public void processReview(Document document, WizardState state, Integer sectionIndex) {
			
	}

	@Override
	public void finish(WizardState state, Integer sectionIndex) {

	}
	
	
}
