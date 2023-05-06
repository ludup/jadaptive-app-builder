package com.jadaptive.api.ui;

import java.io.IOException;
import java.util.Objects;

import org.apache.commons.lang.StringUtils;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.jadaptive.api.servlet.Request;

@Component
@RequestPage(path = "message")
@ModalPage
@PageDependencies(extensions = { "jquery", "bootstrap", "fontawesome", "jadaptive-utils" } )
@PageProcessors(extensions = { "i18n"} )
public class MessagePage extends HtmlPage {

static Logger log = LoggerFactory.getLogger(ErrorPage.class);
	
	private static final String RETURN_TO = "returnTo";
	
	private static final String BUNDLE = "bundle";
	private static final String TITLE = "title";
	private static final String MESSAGE = "message";
	private static final String ICON = "icon";
	
	private static String MESSAGE_URI = "/app/ui/message";
	
	public MessagePage() {
	}
	
	public MessagePage(String bundle, String title, String message, String icon) {
		Request.get().getSession().setAttribute(BUNDLE, bundle);
		Request.get().getSession().setAttribute(TITLE, title);
		Request.get().getSession().setAttribute(MESSAGE, message);
		Request.get().getSession().setAttribute(ICON, icon);
	}
	
	public MessagePage(String bundle, String title, String message, String icon, String returnTo) {
		this(bundle, title, message, icon);
		Request.get().getSession().setAttribute(RETURN_TO, returnTo);
	}
	
	public MessagePage(String bundle, String title, String message, String icon, Page returnTo) {
		this(bundle, title, message, icon);
		Request.get().getSession().setAttribute(RETURN_TO, returnTo);
	}
	
	public static final String generatePageNotFoundURI(String returnTo) {
		Request.get().getSession().setAttribute(BUNDLE, "userInterface");
		Request.get().getSession().setAttribute(TITLE, "title.pageNotFound");
		Request.get().getSession().setAttribute(MESSAGE, "message.pageNotFound");
		Request.get().getSession().setAttribute(ICON, "fa-file-circle-exclamation");	
		Request.get().getSession().setAttribute(RETURN_TO, returnTo);
		return MESSAGE_URI;
	}
	
	public static final String generateForbiddenURI(String returnTo) {
		Request.get().getSession().setAttribute(BUNDLE, "userInterface");
		Request.get().getSession().setAttribute(TITLE, "title.accessDenied");
		Request.get().getSession().setAttribute(MESSAGE, "message.accessDenied");
		Request.get().getSession().setAttribute(ICON, "fa-file-lock");	
		Request.get().getSession().setAttribute(RETURN_TO, returnTo);
		
		return MESSAGE_URI;
	}

	@Override
	public String getUri() {
		return "message";
	}

	@Override
	protected void generateContent(Document document) throws IOException {
		super.generateContent(document);
		
		Object returnTo = Request.get().getSession().getAttribute(RETURN_TO);
		
		String bundle = (String) Request.get().getSession().getAttribute(BUNDLE);		
		String title = (String) Request.get().getSession().getAttribute(TITLE);	
		String message = (String) Request.get().getSession().getAttribute(MESSAGE);	
		String icon = (String) Request.get().getSession().getAttribute(ICON);	
		
		document.selectFirst("#title")
				.appendChild(Html.i18n(bundle, title));
		document.selectFirst("#icon").appendChild(Html.i("far", icon));
		document.selectFirst("#iconLarge").appendChild(Html.i("fa-solid fa-4x", icon));
		document.selectFirst("#message").appendChild(Html.i18n(bundle, message));
		
		if(Objects.isNull(returnTo) || StringUtils.isBlank(returnTo.toString())) {
			document.selectFirst("#returnTo").remove();
		} else {
			if(returnTo instanceof Page) {
				document.selectFirst("#returnTo").attr("href", PageCache.getPageURL((Page)returnTo));
			} else if(returnTo instanceof String) {
				document.selectFirst("#returnTo").attr("href", returnTo.toString());
			} else {
				log.warn("Unexpected object in return to parameter [{}]", returnTo.getClass().getName());
				document.selectFirst("#returnTo").remove();
			}
		}
	}
}