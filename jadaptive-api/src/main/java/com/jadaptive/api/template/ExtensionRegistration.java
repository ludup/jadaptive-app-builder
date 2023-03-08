package com.jadaptive.api.template;

public class ExtensionRegistration {

	ObjectExtension e;
	
	public ExtensionRegistration(ObjectExtension e) {
		this.e = e;
	}
	
	public String resourceKey() {
		return e.resourceKey();
	}
	
	public String bundle() {
		return e.bundle();
	}
	
	public String extend() {
		return e.extend();
	}
	
	public Class<?> extendingInterface() {
		return e.extendingInterface();
	}
}
