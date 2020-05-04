package com.jadaptive.app.ui;

import org.jsoup.nodes.Document;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.codesmith.webbits.App;
import com.codesmith.webbits.In;
import com.codesmith.webbits.Out;
import com.codesmith.webbits.Page;
import com.codesmith.webbits.Redirect;
import com.codesmith.webbits.View;
import com.codesmith.webbits.bootstrap.Bootstrapify;
import com.codesmith.webbits.extensions.Absolutify;
import com.codesmith.webbits.extensions.Enablement;
import com.codesmith.webbits.extensions.I18N;
import com.codesmith.webbits.extensions.PageResources;
import com.codesmith.webbits.extensions.PageResourcesElement;
import com.codesmith.webbits.extensions.Relativize;
import com.codesmith.webbits.fontawesome.FontAwesomeify;
import com.codesmith.webbits.spring.WebbitsPageScope;

@Page
@App({ Bootstrapify.class, FontAwesomeify.class, 
	Relativize.class, PageResources.class, 
	PageResourcesElement.class, Absolutify.class, 
	Enablement.class, I18N.class })
@View(contentType = "text/html", paths = "/")
@Component
@Scope(WebbitsPageScope.ID)
public class JadaptiveApp extends AuthenticatedView {

	@Out()
    Document service(@In Document template) {
		throw new Redirect(Dashboard.class);
    }

	@Override
	protected void onCreated() {
		
	}
}
