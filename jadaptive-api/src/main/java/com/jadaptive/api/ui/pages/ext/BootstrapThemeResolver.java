package com.jadaptive.api.ui.pages.ext;

import org.pf4j.ExtensionPoint;

public interface BootstrapThemeResolver extends ExtensionPoint {

	BootstrapTheme getTheme();
	
	Integer weight();

}
