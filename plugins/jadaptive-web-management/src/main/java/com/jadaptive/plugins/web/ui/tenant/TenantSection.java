package com.jadaptive.plugins.web.ui.tenant;

import com.jadaptive.api.setup.WizardSection;

public class TenantSection extends WizardSection {

	public TenantSection(String bundle, String name, String resource) {
		super(bundle, name, resource);
	}

	@Override
	public boolean isSystem() {
		return false;
	}

}
