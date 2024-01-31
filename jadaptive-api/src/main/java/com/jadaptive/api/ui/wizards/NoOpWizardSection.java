package com.jadaptive.api.ui.wizards;

public final class NoOpWizardSection extends WizardSection {

	public NoOpWizardSection(String bundle, Integer weight) {
		super(bundle, weight);
	}

	public NoOpWizardSection(String bundle, String name, Integer weight) {
		super(bundle, name, weight);
	}

	public NoOpWizardSection(String bundle, String name, String resource, Integer weight) {
		super(bundle, name, resource, weight);
	}

}
