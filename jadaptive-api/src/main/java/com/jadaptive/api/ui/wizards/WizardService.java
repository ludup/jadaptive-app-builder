package com.jadaptive.api.ui.wizards;

import javax.servlet.http.HttpServletRequest;

public interface WizardService {

	WizardFlow getWizard(String resourceKey);

	void clearState(String resourceKey, HttpServletRequest request);

}
