package com.jadaptive.app.ui;

import org.jsoup.nodes.Document;

import com.codesmith.webbits.ClasspathResource;
import com.codesmith.webbits.HTTPMethod;
import com.codesmith.webbits.In;
import com.codesmith.webbits.Out;
import com.codesmith.webbits.Page;
import com.codesmith.webbits.View;
import com.codesmith.webbits.extensions.Bind;
import com.jadaptive.api.ui.AuthenticatedPage;

@Page(value = Bind.class)
@View(contentType = "text/html", paths = { "/dashboard"})
@ClasspathResource
public class Dashboard extends AuthenticatedPage {

	
    @Out(methods = HTTPMethod.POST)
    Document service(@In Document content) {
    	return content;
    }

	@Override
	protected void onCreated() {

	}

}
