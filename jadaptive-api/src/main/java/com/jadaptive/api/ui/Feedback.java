package com.jadaptive.api.ui;

import java.util.Collection;
import java.util.Collections;

import com.jadaptive.api.db.DocumentValidationError;
import com.jadaptive.api.servlet.Request;

public class Feedback {

	String icon;
	String bundle;
	String i18n;
	String alert;
	Object[] args;
	boolean rawText = false;
	
	public Feedback(String icon, String bundle, String i18n, String alert, Object... args) {
		super();
		this.icon = icon;
		this.bundle = bundle;
		this.i18n = i18n;
		this.alert = alert;
		this.args = args;
	}
	
	public Feedback(String icon, String message, String alert) {
		super();
		this.i18n = message;
		this.icon = icon;
		this.alert = alert;
		this.rawText = true;
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
	
	public boolean isRawText() {
		return rawText;
	}

	public static boolean isSet() {
		return get() != null;
	}
	
	public static Feedback get() {
		return (Feedback)Request.get().getSession().getAttribute("feedback");
	}
	
	@SuppressWarnings("unchecked")
	public static Collection<DocumentValidationError> getValidationErrors() {
		Collection<DocumentValidationError> errs = (Collection<DocumentValidationError>)Request.get().getSession().getAttribute("validationErrors");
		return errs == null ? Collections.<DocumentValidationError>emptyList() : errs;
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
	
	public static void dark(String bundle, String i18n, Object...args) {
		Request.get().getSession().setAttribute("feedback", new DarkFeedback(bundle, i18n, args));
	}
	
	public String getAlert() {
		return alert;
	}
	
	public Object[] getArgs() {
		return args;
	}

	public static void error(String message) {
		Request.get().getSession().setAttribute("feedback", new ErrorFeedback(message));
	}

	public static void errorWithValidationErrors(String message, Collection<DocumentValidationError> collection) {
		error(message);
		Request.get().getSession().setAttribute("validationErrors", collection);
	}

	public static void clearValidation() {
		Request.get().getSession().removeAttribute("validationErrors");
	}

	public static void clear() {
		Request.get().getSession().removeAttribute("feedback");
	}
	
}
