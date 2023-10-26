package com.jadaptive.api.auth.reset;

import java.util.Arrays;
import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.jadaptive.api.app.ApplicationService;
import com.jadaptive.api.ui.wizards.AbstractWizard;
import com.jadaptive.api.ui.wizards.WizardSection;

@Component
public class PasswordResetPolicyWizard extends AbstractWizard {

	public static final String RESOURCE_KEY = "passwordReset";
	
	@Autowired
	private ApplicationService applicationService; 
	
	@Override
	public String getResourceKey() {
		return RESOURCE_KEY;
	}

	@Override
	protected Class<? extends WizardSection> getSectionClass() {
		return PasswordWizardSection.class;
	}

	@Override
	protected Collection<? extends WizardSection> getDefaultSections() {
		return Arrays.asList(applicationService.autowire(new AuthenticationModulesSection()));
	}

}
