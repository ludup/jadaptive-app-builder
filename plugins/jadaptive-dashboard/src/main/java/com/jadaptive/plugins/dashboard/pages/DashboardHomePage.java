package com.jadaptive.plugins.dashboard.pages;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.jadaptive.api.app.ApplicationService;
import com.jadaptive.api.ui.HomePageResolver;
import com.jadaptive.api.ui.Page;
import com.jadaptive.plugins.dashboard.DashboardInitialiser;
import com.jadaptive.plugins.dashboard.DashboardWidget;

@Component
public class DashboardHomePage implements HomePageResolver {
	
	@Autowired
	private ApplicationService applicationService;

	@Override
	public int getWeight() {
		return 1000;
	}

	@Override
	public Collection<String> getPermissions() {
		return Arrays.asList(DashboardInitialiser.DASHBOARD_PERMISSION);
	}

	@Override
	public Optional<Class<? extends Page>> resolve() {
		return applicationService.getBeans(DashboardWidget.class).stream().filter(d -> d.wantsDisplay()).count() == 0 ? Optional.empty() : Optional.of(Dashboard.class);
	}

}
