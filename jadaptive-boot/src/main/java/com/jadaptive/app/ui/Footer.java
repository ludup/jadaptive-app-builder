package com.jadaptive.app.ui;

import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;

import com.codesmith.webbits.Bound;
import com.codesmith.webbits.ClasspathResource;
import com.codesmith.webbits.In;
import com.codesmith.webbits.Out;
import com.codesmith.webbits.Request;
import com.codesmith.webbits.View;
import com.codesmith.webbits.Widget;
import com.jadaptive.api.session.SessionUtils;

@Widget
@View(contentType = "text/html")
@ClasspathResource
public class Footer {
	@Autowired
	private SessionUtils sessionUtils;
	
	@Bound
	boolean isLoggedOn() {
		return sessionUtils.hasActiveSession(Request.get());
	}

	@Out
	public Element service(@In Element contents) {
		return contents;
	}
}