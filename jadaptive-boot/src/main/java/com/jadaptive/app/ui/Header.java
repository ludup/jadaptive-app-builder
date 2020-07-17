package com.jadaptive.app.ui;

import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;

import com.codesmith.webbits.In;
import com.codesmith.webbits.Out;
import com.codesmith.webbits.ParentView;
import com.codesmith.webbits.Resource;
import com.codesmith.webbits.View;
import com.codesmith.webbits.Widget;
import com.jadaptive.api.servlet.Request;
import com.jadaptive.api.session.SessionUtils;
import com.jadaptive.api.ui.AbstractPage;

@Widget
@View(contentType = "text/html")
@Resource
public class Header {

	@Autowired
	private SessionUtils sessionUtils;
	
	boolean loggedOn;

    public boolean isLoggedOn() {
		return sessionUtils.hasActiveSession(Request.get());
	}

	@Out
    public Elements service(@In Elements contents, @ParentView Object page) {
    	
		if(!isLoggedOn()) {
			contents.select("script").remove();
			contents.select("#searchForm").remove();
			contents.select("#logoff").remove();
		} else if(page instanceof AbstractPage) {
			if(((AbstractPage)page).isModal()) {
				contents.select("script").remove();
				contents.select("#searchForm").remove();
			}
		}
		
		return contents;
    }
}
