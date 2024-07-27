package com.jadaptive.api.ui.pages.ext;

import org.pf4j.Extension;

@Extension(ordinal = 100)
public class DefaultBootstrapThemeResolver implements BootstrapThemeResolver {

	@Override
	public BootstrapTheme getTheme() {
		return Footer.getThemeFromCookie(BootstrapTheme.DEFAULT);
	}

}
