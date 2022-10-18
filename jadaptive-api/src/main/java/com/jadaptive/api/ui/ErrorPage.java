package com.jadaptive.api.ui;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Objects;

import org.apache.commons.lang.StringUtils;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.jadaptive.api.servlet.Request;

@Component
@RequestPage(path = "error")
@ModalPage
@PageDependencies(extensions = { "jquery", "bootstrap", "fontawesome", "jadaptive-utils"} )
@PageProcessors(extensions = { "i18n"} )
public class ErrorPage extends HtmlPage {

	static Logger log = LoggerFactory.getLogger(ErrorPage.class);
	
	private static final String THROWABLE = "lastError";
	private static final String RETURN_TO = "returnTo";
	
	private static final String ERROR_URI = "/app/ui/error";
	
	public ErrorPage() {
		
	}
	
	public ErrorPage(Throwable e) {
		Request.get().getSession().setAttribute(THROWABLE, e);
	}
	
	public ErrorPage(Throwable e, String returnTo) {
		Request.get().getSession().setAttribute(THROWABLE, e);
		Request.get().getSession().setAttribute(RETURN_TO, returnTo);
	}
	
	public ErrorPage(Throwable e, Page returnTo) {
		Request.get().getSession().setAttribute(THROWABLE, e);
		Request.get().getSession().setAttribute(RETURN_TO, returnTo);
	}
	
	public static String generateErrorURI(Throwable e, Page returnTo) {
		Request.get().getSession().setAttribute(THROWABLE, e);
		Request.get().getSession().setAttribute(RETURN_TO, returnTo);
		return ERROR_URI;
	}
	
	public static String generateErrorURI(Throwable e, String returnTo) {
		Request.get().getSession().setAttribute(THROWABLE, e);
		Request.get().getSession().setAttribute(RETURN_TO, returnTo);
		return ERROR_URI;
	}
	
	public static String generateErrorURI(Throwable e) {
		Request.get().getSession().setAttribute(THROWABLE, e);
		return ERROR_URI;
	}
	
	

	@Override
	public String getUri() {
		return "error";
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
		Throwable e = (Throwable) Request.get().getSession().getAttribute(THROWABLE);
		if(Objects.isNull(e)) {
			throw new UriRedirect("dashboard");
		}
		
		document.selectFirst("#message").text(StringUtils.defaultString(e.getMessage()));
		try(StringWriter w = new StringWriter()) {
			try(PrintWriter pw = new PrintWriter(w)) {
				e.printStackTrace(pw);
				document.selectFirst("#stacktrace").text(w.toString());
			}
		}
	}

	
}
