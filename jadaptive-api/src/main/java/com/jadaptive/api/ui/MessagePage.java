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
@PageDependencies(extensions = { "jquery", "bootstrap", "fontawesome", "jadaptive-utils", "i18n"} )
@PageProcessors(extensions = { "i18n"} )
public class MessagePage extends HtmlPage {

static Logger log = LoggerFactory.getLogger(ErrorPage.class);
	
	private static final String RETURN_TO = "returnTo";
	
	String title;
	String message;
	String icon;
	
	public MessagePage() {
		
	}
	
	public MessagePage(String title, String message, String icon) {
		this.title = title;
		this.message = message;
		this.icon = icon;
	}
	
	public MessagePage(String title, String message, String icon, String returnTo) {
		this(title, message, icon);
		Request.get().getSession().setAttribute(RETURN_TO, returnTo);
	}
	
	public MessagePage(String title, String message, String icon, Page returnTo) {
		this(title, message, icon);
		Request.get().getSession().setAttribute(RETURN_TO, returnTo);
	}

	@Override
	public String getUri() {
		return "message";
	}

	@Override
	protected void generateContent(Document document) throws IOException {
		super.generateContent(document);
		
		Object returnTo = Request.get().getSession().getAttribute(RETURN_TO);
		
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