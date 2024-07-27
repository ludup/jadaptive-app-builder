package com.jadaptive.api.ui;

import org.pf4j.ExtensionPoint;

public interface QuickSetupItem extends ExtensionPoint {
	
	public enum Scope {
		USERS, ADMINISTRATOR, ANY
	}

	String getBundle();
	
	String getI18n();
	
	String getLink();
	
	boolean isEnabled();
	
	default Scope scope() {
		return Scope.ADMINISTRATOR;
	}
}
