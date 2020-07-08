package com.jadaptive.app.ui;

import org.jsoup.nodes.Document;

import com.codesmith.webbits.HTTPMethod;
import com.codesmith.webbits.In;
import com.codesmith.webbits.Out;
import com.codesmith.webbits.Page;
import com.codesmith.webbits.Resource;
import com.codesmith.webbits.View;
import com.codesmith.webbits.extensions.Bind;

@Page(value = Bind.class)
@View(contentType = "text/html", paths = { "/dashboard"})
@Resource
public class Dashboard extends AuthenticatedView {

	
    @Out(methods = HTTPMethod.POST)
    Document service(@In Document content) {
    	return content;
    }

	@Override
	protected void onCreated() {

	}

}
