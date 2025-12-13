package com.jadaptive.api.ui.menu;

public interface PageMenuFilter {

	default boolean isVisible(ApplicationMenu menu) {
		return true;
	}

	default boolean isEnabled(ApplicationMenu menu) {
		return true;
	}
}
