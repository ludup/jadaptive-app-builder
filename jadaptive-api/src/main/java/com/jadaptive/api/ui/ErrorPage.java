package com.jadaptive.api.ui;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.commons.lang.StringUtils;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

import com.jadaptive.api.servlet.Request;

@Component
@RequestPage(path = "error")
@ModalPage
@PageDependencies(extensions = { "jquery", "bootstrap", "fontawesome", "jadaptive-utils", "i18n"} )
@PageProcessors(extensions = { "i18n"} )
public class ErrorPage extends HtmlPage {

	private static final String THROWABLE = "lastError";
	private static final String RETURN_TO = "returnTo";
	
	public ErrorPage() {
		
	}
	
	public ErrorPage(Throwable e, String returnTo) {
		Request.get().getSession().setAttribute(THROWABLE, e);
		Request.get().getSession().setAttribute(RETURN_TO, returnTo);
	}

	@Override
	public String getUri() {
		return "error";
	}

	@Override
	protected void generateContent(Document document) throws IOException {
		super.generateContent(document);
		
		String returnTo = (String) Request.get().getSession().getAttribute(RETURN_TO);
		
		if(StringUtils.isBlank(returnTo)) {
			document.selectFirst("#returnTo").remove();
		} else {
			document.selectFirst("#returnTo").attr("href", returnTo);
		}
		Throwable e = (Throwable) Request.get().getSession().getAttribute(THROWABLE);
		document.selectFirst("#message").text(e.getMessage());
		
		try(StringWriter w = new StringWriter()) {
			try(PrintWriter pw = new PrintWriter(w)) {
				e.printStackTrace(pw);
				document.selectFirst("#stacktrace").text(w.toString());
			}
		}
	}

	
}
