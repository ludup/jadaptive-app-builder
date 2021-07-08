package com.jadaptive.api.ui;

import org.pf4j.ExtensionPoint;

public interface QuickSetupItem extends ExtensionPoint {

	String getBundle();
	
	String getI18n();
	
	String getLink();
}
