package com.jadaptive.plugins.web.ui;

import org.pf4j.Extension;

import com.jadaptive.api.ui.AbstractPageExtension;

@Extension
public class DashboardWidgets extends AbstractPageExtension{

	@Override
	public String getName() {
		return "dashboardWidgets";
	}

}
