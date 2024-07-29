package com.jadaptive.api.ui.pages;

import org.springframework.stereotype.Component;

import com.jadaptive.api.ui.HtmlPage;

@Component
public class Ping extends HtmlPage {

	public Ping() {
	}

	@Override
	public String getUri() {
		return "ping";
	}

}
