package com.jadaptive.api.ui;

public enum BasicDashboardTypes implements DashboardType {

	SERVER_INFORMATION,
	INSIGHTS;

	@Override
	public int weight() {
		return ordinal() * 100;
	}

	@Override
	public String resourceKey() {
		switch(this) {
		case SERVER_INFORMATION:
			return "home";
		default:
			return DashboardType.super.resourceKey();
		}
	}

	@Override
	public String bundle() {
		return "userInterface";
	}
}
