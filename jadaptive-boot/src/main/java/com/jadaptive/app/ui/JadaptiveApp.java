package com.jadaptive.app.ui;

import org.jsoup.nodes.Document;

import com.codesmith.webbits.App;
import com.codesmith.webbits.In;
import com.codesmith.webbits.Out;
import com.codesmith.webbits.Page;
import com.codesmith.webbits.Redirect;
import com.codesmith.webbits.View;
import com.codesmith.webbits.extensions.Absolutify;
import com.codesmith.webbits.extensions.Enablement;
import com.codesmith.webbits.extensions.I18N;
import com.codesmith.webbits.extensions.PageResources;
import com.codesmith.webbits.extensions.Relativize;
import com.jadaptive.api.ui.AuthenticatedPage;

@Page
@App({ Relativize.class, PageResources.class, Absolutify.class, Enablement.class, I18N.class })
@View(contentType = "text/html", paths = "")
public class JadaptiveApp extends AuthenticatedPage {

	@Out()
	Document service(@In Document template) {
		throw new Redirect(Dashboard.class);
	}

	@Override
	protected void onCreated() {

	}
}
