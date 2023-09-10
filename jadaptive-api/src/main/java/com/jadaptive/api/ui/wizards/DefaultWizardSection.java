package com.jadaptive.api.ui.wizards;

public class DefaultWizardSection extends WizardSection {

	public DefaultWizardSection(String bundle, Integer weight) {
		super(bundle, weight);
	}
	
	public DefaultWizardSection(String bundle, String name, Integer weight) {
		super(bundle, name, weight);
	}
	
	public DefaultWizardSection(String bundle, String name, String resource, Integer weight) {
		super(bundle, name, resource, weight);
	}
}
