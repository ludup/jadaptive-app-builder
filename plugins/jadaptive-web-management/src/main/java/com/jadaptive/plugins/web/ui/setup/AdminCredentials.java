package com.jadaptive.plugins.web.ui.setup;

import org.pf4j.Extension;

import com.jadaptive.api.ui.AbstractPageExtension;

@Extension
public class AdminCredentials extends SetupSection {

	@Override
	public String getName() {
		return "setup-credentials";
	}

}
