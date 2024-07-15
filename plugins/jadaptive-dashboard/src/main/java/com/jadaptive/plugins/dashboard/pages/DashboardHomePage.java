package com.jadaptive.plugins.dashboard.pages;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.jadaptive.api.ui.HomePageResolver;
import com.jadaptive.api.ui.Page;
import com.jadaptive.plugins.dashboard.DashboardInitialiser;

@Component
public class DashboardHomePage implements HomePageResolver {

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
		return Optional.of(Dashboard.class);
	}

}
