package com.jadaptive.api.ui.pages;

import org.pf4j.Extension;

import com.jadaptive.api.ui.HtmlPage;

@Extension
public class Ping extends HtmlPage {

	public Ping() {
	}

	@Override
	public String getUri() {
		return "ping";
	}

}
