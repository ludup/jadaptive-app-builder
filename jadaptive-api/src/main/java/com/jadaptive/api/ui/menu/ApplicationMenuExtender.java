package com.jadaptive.api.ui.menu;

import org.pf4j.ExtensionPoint;

public interface ApplicationMenuExtender extends ExtensionPoint {

	boolean isExtending(ApplicationMenu menu);
	boolean isVisible(ApplicationMenu menu);
}
