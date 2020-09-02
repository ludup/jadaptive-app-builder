package com.jadaptive.app.ui;

import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;

import com.codesmith.webbits.Bound;
import com.codesmith.webbits.ClasspathResource;
import com.codesmith.webbits.In;
import com.codesmith.webbits.Request;
import com.codesmith.webbits.View;
import com.codesmith.webbits.ViewManager;
import com.codesmith.webbits.Widget;
import com.codesmith.webbits.bootstrap.widgets.Navbar;
import com.codesmith.webbits.extensions.Bind;
import com.jadaptive.api.session.SessionUtils;
import com.jadaptive.api.ui.AbstractPage;

@Widget(Bind.class)
@View(contentType = "text/html")
@ClasspathResource
public class Header {

	@Autowired
	private SessionUtils sessionUtils;
	
	@Bound
	Navbar navbar = Navbar.createTitled("JAdpative", "Home", "Features", "Pricing", "About");
	
	@Bound
	Class<?> titleLink = Dashboard.class;
	
	boolean loggedOn;

	@Bound
    public boolean isLoggedOn() {
		return sessionUtils.hasActiveSession(Request.get());
	}
    
    @Bound
    public boolean isSearchAvailable() {
    	return isLoggedOn() && (((AbstractPage)ViewManager.get().rootView()).isModal());
    }

//	@Out
    public Element service(@In Element contents) {
    	return contents;
    }
//    	, @ParentView Object page
//		if(!isLoggedOn()) {
//			contents.select("script").remove();
//			contents.select("#searchForm").remove();
//			contents.select("#logoff").remove();
//		} else if(page instanceof AbstractPage) {
//			if(((AbstractPage)page).isModal()) {
//				contents.select("script").remove();
//				contents.select("#searchForm").remove();
//			}
//		}
//		
//		return contents;
//    }
}
