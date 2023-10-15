package com.jadaptive.api.auth.reset;

import com.jadaptive.api.ui.wizards.DefaultWizardSection;

public class PasswordWizardSection extends DefaultWizardSection {

	public PasswordWizardSection(String bundle, Integer weight) {
		super(bundle, weight);
	}

	public PasswordWizardSection(String bundle, String name, Integer weight) {
		super(bundle, name, weight);
	}

	public PasswordWizardSection(String bundle, String name, String resource, Integer weight) {
		super(bundle, name, resource, weight);
	}

}
