package com.jadaptive.api.ui.renderers;

public class I18nOption {

	String bundle;
	String i18n;
	String value;
	
	public I18nOption(String bundle, String i18n, String value) {
		super();
		this.bundle = bundle;
		this.i18n = i18n;
		this.value = value;
	}
	
	public String getBundle() {
		return bundle;
	}
	public void setBundle(String bundle) {
		this.bundle = bundle;
	}
	public String getI18n() {
		return i18n;
	}
	public void setI18n(String i18n) {
		this.i18n = i18n;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	
	
}
