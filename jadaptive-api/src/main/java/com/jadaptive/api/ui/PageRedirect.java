package com.jadaptive.api.ui;

public class PageRedirect extends Redirect {

	private static final long serialVersionUID = -8712697700670101012L;
	
	Page page;
	
	public PageRedirect(Page page) {
		this.page = page;
	}

	@Override
	public String getUri() {
		return String.format("/app/ui/%s", page.getUri());
	}

}
