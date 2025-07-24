package com.jadaptive.api.ui;

import java.io.FileNotFoundException;

import com.jadaptive.api.app.App;

public class PageRedirect extends Redirect {

	private static final long serialVersionUID = -8712697700670101012L;
	
	Page page;
	
	public PageRedirect(Page page) {
		this.page = page;
	}

	public PageRedirect(Class<? extends Page> clz) throws FileNotFoundException {
		this(App.bean(PageCache.class).resolvePage(clz));
	}

	@Override
	public String getUri() {
		return String.format("/app/ui/%s", page.getUri());
	}

}
