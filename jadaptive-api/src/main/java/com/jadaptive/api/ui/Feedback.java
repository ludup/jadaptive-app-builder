package com.jadaptive.api.ui;

import com.jadaptive.api.servlet.Request;

public class Feedback {

	String icon;
	String bundle;
	String i18n;
	String alert;
	Object[] args;

	public Feedback(String icon, String bundle, String i18n, String alert, Object... args) {
		super();
		this.icon = icon;
		this.bundle = bundle;
		this.i18n = i18n;
		this.alert = alert;
		this.args = args;
	}
	
	public String getIcon() {
		return icon;
	}

	public String getBundle() {
		return bundle;
	}

	public String getI18n() {
		return i18n;
	}
	
	public static void success(String bundle, String i18n, Object...args) {
		Request.get().getSession().setAttribute("feedback", new SuccessFeedback(bundle, i18n, args));
	}
	
	public static void error(String bundle, String i18n, Object...args) {
		Request.get().getSession().setAttribute("feedback", new ErrorFeedback(bundle, i18n, args));
	}
	
	public static void info(String bundle, String i18n, Object...args) {
		Request.get().getSession().setAttribute("feedback", new InfoFeedback(bundle, i18n, args));
	}
	
	public static void warning(String bundle, String i18n, Object...args) {
		Request.get().getSession().setAttribute("feedback", new WarningFeedback(bundle, i18n, args));
	}

	public String getAlert() {
		return alert;
	}
	
	public Object[] getArgs() {
		return args;
	}
	
}
