package com.jadaptive.plugins.term.ui;

import org.jsoup.nodes.Document;
import org.pf4j.Extension;

import com.jadaptive.api.ui.AuthenticatedPage;
import com.jadaptive.api.ui.PageDependencies;
import com.jadaptive.api.ui.PageHelper;
import com.jadaptive.api.ui.PageProcessors;
import com.jadaptive.api.ui.RequestPage;

@Extension
@RequestPage(path = "term/{sessionId}")
@PageDependencies(extensions = { "jquery", "jquery-ui" } )
@PageProcessors(extensions = { "freemarker", "i18n"} )
public class Term extends AuthenticatedPage {

	@Override
	protected void documentComplete(Document document) {
		
		PageHelper.appendStylesheet(document, "/app/content/term/css/common.css");
		PageHelper.appendStylesheet(document, "/app/content/term/css/full-screen.css");

		PageHelper.appendScript(document, "/app/content/term/terminal.components.js");
		PageHelper.appendScript(document, "/app/content/term/client-emulation/terminal.components.js");
		PageHelper.appendScript(document, "/app/content/term/client-emulation/terminal.components.websocket.js");
		
		PageHelper.appendScript(document, "/app/content/term/lib/jquery.ui.scroller.js");
		PageHelper.appendScript(document, "/app/content/term/lib/jquery.mousewheel.js");
	}

	String sessionId;
	
	public String getUri() {
		return "term";
	}
	
	public String getSessionId() {
		return sessionId;
	}

}
