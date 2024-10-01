package com.jadaptive.api.ui.pages.ext;

import org.springframework.stereotype.Component;

@Component
public class DefaultBootstrapThemeResolver implements BootstrapThemeResolver {

	@Override
	public BootstrapTheme getTheme() {
		return Footer.getThemeFromCookie(BootstrapTheme.DEFAULT);
	}

}
